package renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.isZero;

/**
 * Reusable beam / target-area infrastructure shared by every super-sampling effect
 * (antialiasing, depth of field, soft shadows, glossy / diffuse glass).
 * <p>
 * A BeamSampler distributes sample points evenly over a two-dimensional target area
 * and turns them into a beam of rays. The dimensionless sample <em>pattern</em> is
 * decided once at construction; the actual three-dimensional placement (center,
 * orthonormal basis, size) and the ray geometry (apex, direction, normal offset) are
 * supplied per call. A single immutable instance is therefore configured once and
 * reused for every pixel, shaded point, or hit point, with no per-call allocation of
 * the object itself.
 */
final class BeamSampler {

    /**
     * The distribution pattern of the samples over the unit target area.
     */
    enum Pattern {
        /**
         * A regular, deterministic lattice (computed once and cached).
         */
        GRID,
        /**
         * A regular lattice whose samples are randomly nudged inside their cell.
         */
        JITTERED,
        /**
         * Independent uniform-random samples over the whole area.
         */
        STOCHASTIC
    }

    /**
     * The shape of the target area.
     */
    enum Shape {
        /**
         * A full square (every grid sample is kept).
         */
        SQUARE,
        /**
         * A disk inscribed in the square (samples outside the radius are rejected).
         */
        CIRCLE
    }

    /**
     * An immutable dimensionless sample offset in the target area's local frame.
     * Each component lies in the range {@code [-0.5, 0.5]}.
     *
     * @param x the offset along the first basis vector
     * @param y the offset along the second basis vector
     */
    private record Point2D(double x, double y) {
    }

    /**
     * Squared radius of the unit disk inscribed in the target square; a sample at
     * dimensionless distance {@code d} from the center is inside the circle when
     * {@code d * d <= RADIUS_SQUARED}.
     */
    private static final double RADIUS_SQUARED = 0.5 * 0.5;
    /**
     * Maximum allowed absolute cosine between a direction and a reference axis when
     * deriving an orthonormal basis; above it the alternative axis is used, so the
     * cross-product is never near-zero.
     */
    private static final double AXIS_ALIGN_LIMIT = 0.9;

    /**
     * The sample distribution pattern.
     */
    private final Pattern _pattern;
    /**
     * The target-area shape.
     */
    private final Shape _shape;
    /**
     * The number of samples along one axis of the area (the grid edge).
     * A value of {@code 1} produces a single central sample, disabling the effect.
     */
    private final int _edge;
    /**
     * The dimensionless offsets for a {@link Pattern#GRID} pattern, computed once at
     * construction; {@code null} for the random patterns, which are regenerated on
     * every call.
     */
    private final List<Point2D> _gridCache;

    /**
     * Constructs a BeamSampler with the given sampling configuration.
     *
     * @param samplesPerAxis the number of samples along one axis of the target area
     *                       (e.g. {@code 9} yields up to 81 samples); a value below
     *                       {@code 2} disables super-sampling (single central ray)
     * @param shape          the shape of the target area
     * @param pattern        the sample distribution pattern
     */
    BeamSampler(int samplesPerAxis, Shape shape, Pattern pattern) {
        _edge = Math.max(1, samplesPerAxis); //
        _shape = shape;
        _pattern = pattern;
        _gridCache = pattern == Pattern.GRID ? buildOffsets() : null;
    }

    /**
     * Produces a beam of rays sampling a target area whose orthonormal basis is given
     * explicitly (used by antialiasing and depth of field, where the basis is the
     * camera's right/up vectors).
     *
     * @param center     the center of the target area in three-dimensional space
     * @param vX         the first basis vector of the area (unit length, orthogonal to {@code vY})
     * @param vY         the second basis vector of the area (unit length, orthogonal to {@code vX})
     * @param size       the full extent of the area (edge length for a square, diameter for a circle)
     * @param apex       the fixed point shared by every ray of the beam
     * @param fromPoints {@code true} to start each ray at its sample point and aim it at
     *                   {@code apex} (depth of field); {@code false} to start each ray at
     *                   {@code apex} and aim it through its sample point (every other effect)
     * @param normal     the surface normal used to offset secondary-ray origins, or
     *                   {@code null} for primary camera rays
     * @return the beam of rays (a single central ray when the effect is disabled)
     */
    List<Ray> beam(Point center, Vector vX, Vector vY, double size,
                   Point apex, boolean fromPoints, Vector normal) {
        List<Point> points = targetPoints(center, vX, vY, size);
        List<Ray> rays = new ArrayList<>(points.size());
        for (Point point : points) {
            Point from = fromPoints ? point : apex;
            Vector direction = (fromPoints ? apex : point).subtract(from);
            rays.add(normal == null ? new Ray(from, direction) : new Ray(from, direction, normal));
        }
        return rays;
    }

    /**
     * Produces a beam of rays sampling a target area perpendicular to {@code dir}
     * (used by soft shadows and glossy / diffuse surfaces). The orthonormal basis of
     * the area is derived here, so the calling effects never duplicate that logic.
     *
     * @param center     the center of the target area in three-dimensional space
     * @param dir        the unit direction the target area is perpendicular to
     * @param size       the full extent of the area (edge length for a square, diameter for a circle)
     * @param apex       the fixed point shared by every ray of the beam
     * @param fromPoints {@code true} to start each ray at its sample point and aim it at
     *                   {@code apex}; {@code false} to start each ray at {@code apex} and
     *                   aim it through its sample point
     * @param normal     the surface normal used to offset secondary-ray origins, or
     *                   {@code null} for primary camera rays
     * @return the beam of rays (a single central ray when the effect is disabled)
     */
    List<Ray> beam(Point center, Vector dir, double size,
                   Point apex, boolean fromPoints, Vector normal) {
        Vector reference = Math.abs(dir.dotProduct(Vector.AXIS_Y)) < AXIS_ALIGN_LIMIT
                ? Vector.AXIS_Y : Vector.AXIS_X;
        Vector vX = dir.crossProduct(reference).normalize();
        Vector vY = dir.crossProduct(vX).normalize();
        return beam(center, vX, vY, size, apex, fromPoints, normal);
    }

    /**
     * Maps the configured sample offsets onto a three-dimensional target area as
     * {@code center + (offsetX * size) * vX + (offsetY * size) * vY}. The offsets are
     * taken from the cache for a {@link Pattern#GRID} pattern, or regenerated for the
     * random patterns.
     *
     * @param center the center of the target area
     * @param vX     the first basis vector of the area
     * @param vY     the second basis vector of the area
     * @param size   the full extent of the area
     * @return the list of three-dimensional sample points
     */
    List<Point> targetPoints(Point center, Vector vX, Vector vY, double size) {
        // A zero-size target area disables the effect: every sample would collapse onto the
        // center anyway, so emit a single central point (one ray) regardless of pattern/edge.
        if (isZero(size)) return List.of(center);
        List<Point2D> offsets = _pattern == Pattern.GRID ? _gridCache : buildOffsets();
        List<Point> points = new ArrayList<>(offsets.size());
        for (Point2D offset : offsets) {
            Point point = center;
            double dx = offset.x() * size;
            double dy = offset.y() * size;
            if (!isZero(dx)) point = point.add(vX.scale(dx));
            if (!isZero(dy)) point = point.add(vY.scale(dy));
            points.add(point);
        }
        return points;
    }

    /**
     * Generates the dimensionless sample offsets for the configured shape and pattern.
     * Each offset lies in {@code [-0.5, 0.5]} on both axes; for a circular area,
     * samples falling outside the inscribed disk are rejected.
     *
     * @return the freshly generated list of offsets
     */
    private List<Point2D> buildOffsets() {
        // A single-sample beam disables the effect: emit exactly one central sample
        // (the ideal ray / area center) regardless of pattern or shape, so an edge of 1
        // is always a clean single-ray "off" switch and never yields an empty beam.
        if (_edge <= 1) return List.of(new Point2D(0, 0));
        List<Point2D> offsets = new ArrayList<>(_edge * _edge);
        ThreadLocalRandom random = _pattern == Pattern.GRID ? null : ThreadLocalRandom.current();
        double step = 1.0 / _edge;
        for (int i = 0; i < _edge; i++) {
            for (int j = 0; j < _edge; j++) {
                double x;
                double y;
                if (_pattern == Pattern.STOCHASTIC) {
                    x = random.nextDouble() - 0.5;
                    y = random.nextDouble() - 0.5;
                } else {
                    x = (i + 0.5) * step - 0.5;
                    y = (j + 0.5) * step - 0.5;
                    if (_pattern == Pattern.JITTERED) {
                        x += (random.nextDouble() - 0.5) * step;
                        y += (random.nextDouble() - 0.5) * step;
                    }
                }
                if (_shape == Shape.SQUARE || x * x + y * y <= RADIUS_SQUARED) {
                    offsets.add(new Point2D(x, y));
                }
            }
        }
        return offsets;
    }
}
