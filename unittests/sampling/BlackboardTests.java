package sampling;

import java.util.List;

import org.junit.jupiter.api.Test;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Blackboard} super-sampling infrastructure.
 * The tests verify sample counts, the on/off boundary, that points stay within the
 * configured square / circular area and on its plane, and the two beam directions
 * ({@code fromPoints} true / false).
 */
class BlackboardTests {
    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;
    /**
     * Center of the target area used across tests.
     */
    private static final Point CENTER = new Point(0, 0, 5);
    /**
     * First basis vector of the target area.
     */
    private static final Vector VX = Vector.AXIS_X;
    /**
     * Second basis vector of the target area.
     */
    private static final Vector VY = Vector.AXIS_Y;
    /**
     * Full extent of the target area used across tests.
     */
    private static final double SIZE = 2;
    /**
     * Apex point shared by the rays of a beam.
     */
    private static final Point APEX = Point.ZERO;

    /**
     * Default constructor to satisfy the JavaDoc generator.
     */
    BlackboardTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Test method for {@link Blackboard#targetPoints(Point, Vector, Vector, double)}.
     * Verifies the number of generated points for a grid and the single-sample case.
     */
    @Test
    void testTargetPointsCount() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: a 5x5 square grid yields exactly 25 points
        Blackboard grid5 = new Blackboard(5, Blackboard.Shape.SQUARE, Blackboard.Pattern.GRID);
        assertEquals(25, grid5.targetPoints(CENTER, VX, VY, SIZE).size(),
                "ERROR: 5x5 grid must yield 25 points");

        // =============== Boundary Values Tests ==================

        // BV01: a single sample per axis yields one central point (effect disabled)
        Blackboard single = new Blackboard(1, Blackboard.Shape.SQUARE, Blackboard.Pattern.GRID);
        List<Point> points = single.targetPoints(CENTER, VX, VY, SIZE);
        assertEquals(1, points.size(), "ERROR: single-sample grid must yield 1 point");
        assertEquals(0, points.get(0).distance(CENTER), DELTA,
                "ERROR: the single sample must be the area center");
    }

    /**
     * Test method for {@link Blackboard#targetPoints(Point, Vector, Vector, double)}.
     * Verifies that every generated point lies inside the square area and on its plane.
     */
    @Test
    void testPointsInsideSquare() {
        Blackboard grid = new Blackboard(7, Blackboard.Shape.SQUARE, Blackboard.Pattern.JITTERED);
        Vector normal = VX.crossProduct(VY);

        // ============ Equivalence Partitions Tests ==============
        for (Point point : grid.targetPoints(CENTER, VX, VY, SIZE)) {
            try {
                Vector offset = point.subtract(CENTER);
                // EP01: every point stays within the square's half-extent on both axes
                assertTrue(Math.abs(offset.dotProduct(VX)) <= SIZE / 2 + DELTA,
                        "ERROR: point lies outside the square along vX");
                assertTrue(Math.abs(offset.dotProduct(VY)) <= SIZE / 2 + DELTA,
                        "ERROR: point lies outside the square along vY");
                // EP02: every point lies on the area's plane (no component along the normal)
                assertEquals(0, offset.dotProduct(normal), DELTA,
                        "ERROR: point is not on the target-area plane");
            } catch (IllegalArgumentException ignored) {
                // a sample coinciding with the center yields a zero offset vector — trivially inside
            }
        }
    }

    /**
     * Test method for {@link Blackboard#targetPoints(Point, Vector, Vector, double)}.
     * Verifies that every point of a circular area stays within its radius and that
     * corner samples are rejected.
     */
    @Test
    void testPointsInsideCircle() {
        Blackboard circle = new Blackboard(9, Blackboard.Shape.CIRCLE, Blackboard.Pattern.GRID);
        List<Point> points = circle.targetPoints(CENTER, VX, VY, SIZE);

        // ============ Equivalence Partitions Tests ==============

        // EP01: every point lies within the inscribed disk (radius = size / 2)
        for (Point point : points)
            assertTrue(point.distance(CENTER) <= SIZE / 2 + DELTA,
                    "ERROR: point lies outside the circular area");

        // EP02: rejection keeps fewer points than the enclosing 9x9 square
        assertTrue(points.size() < 81, "ERROR: a circle must reject the corner samples");
    }

    /**
     * Test method for {@link Blackboard#beam(Point, Vector, Vector, double, Point, boolean, Vector)}.
     * Verifies the apex-to-points beam shape (anti-aliasing style): one ray per sample,
     * all sharing the apex origin.
     */
    @Test
    void testBeamThroughPoints() {
        Blackboard grid = new Blackboard(5, Blackboard.Shape.SQUARE, Blackboard.Pattern.GRID);
        List<Ray> rays = grid.beam(CENTER, VX, VY, SIZE, APEX, false, null);

        // ============ Equivalence Partitions Tests ==============

        // EP01: one ray per sample point
        assertEquals(25, rays.size(), "ERROR: beam must have one ray per sample");

        // EP02: every ray starts at the shared apex
        for (Ray ray : rays)
            assertEquals(0, ray.origin().distance(APEX), DELTA,
                    "ERROR: through-points rays must share the apex origin");
    }

    /**
     * Test method for {@link Blackboard#beam(Point, Vector, Vector, double, Point, boolean, Vector)}.
     * Verifies the points-to-apex beam shape (depth-of-field style): every ray converges
     * on the apex.
     */
    @Test
    void testBeamFromPoints() {
        Blackboard grid = new Blackboard(5, Blackboard.Shape.SQUARE, Blackboard.Pattern.GRID);
        List<Ray> rays = grid.beam(CENTER, VX, VY, SIZE, APEX, true, null);

        // ============ Equivalence Partitions Tests ==============

        // EP01: the apex lies on every ray — its direction is parallel to (apex - origin),
        // so the cross product of the two is the zero vector and must throw
        for (Ray ray : rays) {
            Vector toApex = APEX.subtract(ray.origin());
            assertThrows(IllegalArgumentException.class,
                    () -> ray.direction().crossProduct(toApex),
                    "ERROR: from-points rays must aim at the apex");
        }
    }

    /**
     * Test method for {@link Blackboard#beam(Point, Vector, Vector, double, Point, boolean, Vector)}.
     * Verifies that a disabled Blackboard reproduces the single central ray.
     */
    @Test
    void testDisabledBeam() {
        Blackboard single = new Blackboard(1, Blackboard.Shape.SQUARE, Blackboard.Pattern.GRID);

        // =============== Boundary Values Tests ==================

        // BV01: a single-sample beam is exactly the central ray (apex through the center)
        List<Ray> rays = single.beam(CENTER, VX, VY, SIZE, APEX, false, null);
        assertEquals(1, rays.size(), "ERROR: a disabled beam must contain one ray");
        assertEquals(new Ray(APEX, CENTER.subtract(APEX)), rays.get(0),
                "ERROR: a disabled beam must be the central ray");
    }
}
