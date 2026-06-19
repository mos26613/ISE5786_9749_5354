package primitives;

import java.util.List;
import java.util.Objects;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Immutable axis-aligned bounding box (AABB), used as the Conservative Bounding
 * Region of an intersectable geometry.
 * <p>
 * The box is defined by its minimal and maximal corner coordinates; its faces are
 * parallel to the X, Y and Z axes. It exposes only a cheap boolean ray-vs-box test
 * ({@link #intersects(Ray)}) — no intersection points are produced — so it can be used
 * to early-reject rays before the expensive geometry intersection runs.
 * <p>
 * All coordinate arithmetic lives in this class (it sits in the {@code primitives}
 * package and can read point/ray coordinates), so geometries only hand it points and a
 * radius and never expose raw coordinates themselves.
 */
public class AABB {
    /**
     * The minimal corner of the box (smallest x, y and z).
     */
    private final Double3 _min;
    /**
     * The maximal corner of the box (largest x, y and z).
     */
    private final Double3 _max;

    /**
     * Constructs a box from its minimal and maximal corner points.
     *
     * @param min the minimal corner (smallest coordinates)
     * @param max the maximal corner (largest coordinates)
     */
    public AABB(Point min, Point max) {
        _min = min._xyz;
        _max = max._xyz;
    }

    /**
     * Private constructor from raw coordinate triads.
     *
     * @param min the minimal corner coordinates
     * @param max the maximal corner coordinates
     */
    private AABB(Double3 min, Double3 max) {
        _min = min;
        _max = max;
    }

    /**
     * Builds the tightest box enclosing all the given points.
     *
     * @param points the points to enclose (must contain at least one point)
     * @return the smallest axis-aligned box containing every point
     */
    public static AABB around(List<Point> points) {
        Double3 first = points.getFirst()._xyz;
        double minX = first._d1(), minY = first._d2(), minZ = first._d3();
        double maxX = minX, maxY = minY, maxZ = minZ;

        for (Point point : points) {
            Double3 c = point._xyz;
            double x = c._d1(), y = c._d2(), z = c._d3();
            if (x < minX) minX = x; else if (x > maxX) maxX = x;
            if (y < minY) minY = y; else if (y > maxY) maxY = y;
            if (z < minZ) minZ = z; else if (z > maxZ) maxZ = z;
        }
        return new AABB(new Double3(minX, minY, minZ), new Double3(maxX, maxY, maxZ));
    }

    /**
     * Builds the tightest box enclosing all the given points.
     *
     * @param points the points to enclose (at least one)
     * @return the smallest axis-aligned box containing every point
     */
    public static AABB around(Point... points) {
        return around(List.of(points));
    }

    /**
     * Returns a copy of this box grown by the given margin on every side.
     * Used by radius-based geometries (sphere, cylinder) to wrap a point box.
     *
     * @param margin the non-negative amount to grow the box by on each axis
     * @return a new, larger box
     */
    public AABB expand(double margin) {
        Double3 m = new Double3(margin, margin, margin);
        return new AABB(_min.subtract(m), _max.add(m));
    }

    /**
     * Returns the smallest box containing both this box and another one.
     *
     * @param other the other box
     * @return a new box covering both boxes
     */
    public AABB union(AABB other) {
        return new AABB(
                new Double3(Math.min(_min._d1(), other._min._d1()),
                        Math.min(_min._d2(), other._min._d2()),
                        Math.min(_min._d3(), other._min._d3())),
                new Double3(Math.max(_max._d1(), other._max._d1()),
                        Math.max(_max._d2(), other._max._d2()),
                        Math.max(_max._d3(), other._max._d3())));
    }

    /**
     * Boolean slab test: reports whether the given ray meets this box.
     * <p>
     * No intersection point is computed. The ray is intersected against the three
     * coordinate slabs, narrowing a running {@code [tMin, tMax]} parameter interval; the
     * ray meets the box iff that interval stays non-empty and the box is not entirely
     * behind the ray origin. A ray parallel to a slab whose origin lies outside that slab
     * misses immediately.
     *
     * @param ray the ray to test
     * @return {@code true} if the ray meets the box (conservatively), {@code false} if it
     * definitely misses
     */
    public boolean intersects(Ray ray) {
        Double3 o = ray.origin()._xyz;
        Double3 d = ray.direction()._xyz;

        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        // X slab
        double od = o._d1(), dd = d._d1();
        if (isZero(dd)) {
            if (alignZero(od - _min._d1()) < 0 || alignZero(od - _max._d1()) > 0) return false;
        } else {
            double t1 = (_min._d1() - od) / dd;
            double t2 = (_max._d1() - od) / dd;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;
            if (alignZero(tMin - tMax) > 0) return false;
        }

        // Y slab
        od = o._d2(); dd = d._d2();
        if (isZero(dd)) {
            if (alignZero(od - _min._d2()) < 0 || alignZero(od - _max._d2()) > 0) return false;
        } else {
            double t1 = (_min._d2() - od) / dd;
            double t2 = (_max._d2() - od) / dd;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;
            if (alignZero(tMin - tMax) > 0) return false;
        }

        // Z slab
        od = o._d3(); dd = d._d3();
        if (isZero(dd)) {
            if (alignZero(od - _min._d3()) < 0 || alignZero(od - _max._d3()) > 0) return false;
        } else {
            double t1 = (_min._d3() - od) / dd;
            double t2 = (_max._d3() - od) / dd;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;
            if (alignZero(tMin - tMax) > 0) return false;
        }

        // Reject the box only if it lies entirely behind the ray origin.
        return alignZero(tMax) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AABB aabb)) return false;
        return _min.equals(aabb._min) && _max.equals(aabb._max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_min, _max);
    }

    @Override
    public String toString() {
        return "AABB[" + _min + " -> " + _max + "]";
    }
}
