package geometries.impl;

import java.util.List;

import org.junit.jupiter.api.Test;

import static java.lang.Math.sqrt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/** Test class for {@link Sphere} class */
class SphereTests {

    /** Default constructor for SphereTests to satisfy Javadoc tool */
    public SphereTests() {}

    /** Sphere used across findIntersections test cases: center (1,0,0), radius 1 */
    private static final Sphere SPHERE = new Sphere(new Point(1, 0, 0), 1);

    /** Sphere center point */
    private static final Point P100 = new Point(1, 0, 0);

    /** Far pole of SPHERE on the positive x-axis (on surface) */
    private static final Point P200 = new Point(2, 0, 0);

    /** Near pole of SPHERE on the positive x-axis (on surface) */
    private static final Point P000 = new Point(0, 0, 0);

    /** Top of SPHERE (on surface) */
    private static final Point P110 = new Point(1, 1, 0);

    /** Unit vector in the positive x direction */
    private static final Vector V100 = new Vector(1, 0, 0);

    /** Error message for wrong number of intersection points */
    private static final String WRONG_COUNT = "Wrong number of intersection points";

    /** Error message for wrong intersection point value */
    private static final String WRONG_POINT = "Wrong intersection point";

    /** Error message when no intersections were expected */
    private static final String NO_INTERSECTION = "Expected no intersections (null)";

    /**
     * Test method for {@link Sphere#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        Point center = Point.ZERO;
        Point point = new Point(1, 0, 0);
        Sphere sphere = new Sphere(center, 1);
        Vector actualNormal = sphere.getNormal(point);

        // ============ Equivalence Partitions Tests ==============

        // EP01: checks regular case
        assertEquals(point.subtract(center), actualNormal, "Normal vector is not correct.");
    }

    /**
     * Test method for {@link Sphere#findIntersections(Ray)}
     */
    @Test
    void testFindIntersections() {
        List<Point> result;

        // ============ Equivalence Partitions Tests ==============

        // EP01: Ray passes beside the sphere entirely — no real intersection (discriminant < 0)
        assertNull(SPHERE.findIntersections(new Ray(new Point(0, 2, 0), V100)),
                NO_INTERSECTION);

        // EP02: Ray crosses the sphere from outside, not through center — 2 intersection points
        result = SPHERE.findIntersections(new Ray(new Point(0, 0.5, 0), V100));
        assertEquals(2, result.size(), WRONG_COUNT);

        // EP03: Ray starts inside the sphere, not through center — 1 intersection point
        result = SPHERE.findIntersections(new Ray(new Point(1.5, 0.5, 0), V100));
        assertEquals(1, result.size(), WRONG_COUNT);

        // EP04: Sphere is entirely behind the ray - no intersection
        assertNull(SPHERE.findIntersections(new Ray(new Point(2, 2, 0), new Vector(1, 1, 0))),
                NO_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // ** Group 1: Ray starts at sphere surface, not through center **

        // BV11: Ray starts on the surface, goes inside — 1 intersection at the far side
        result = SPHERE.findIntersections(new Ray(P000, new Vector(1, 1, 0)));
        assertEquals(List.of(P110), result, WRONG_POINT);

        // BV12: Ray starts on the surface, goes outside — 0 intersections
        assertNull(SPHERE.findIntersections(new Ray(P000, new Vector(-1, 0, 0))),
                NO_INTERSECTION);

        // ** Group 2: Ray goes through center **

        // BV21: Ray starts before sphere, goes through center — 2 intersections (closer first)
        result = SPHERE.findIntersections(new Ray(new Point(-1, 0, 0), V100));
        assertEquals(List.of(P000, P200), result, WRONG_POINT);

        // BV22: Ray starts on the surface, goes through center — 1 intersection at far side
        result = SPHERE.findIntersections(new Ray(P000, V100));
        assertEquals(List.of(P200), result, WRONG_POINT);

        // BV23: Ray starts inside sphere, through center — 1 intersection at far side
        result = SPHERE.findIntersections(new Ray(new Point(0.5, 0, 0), V100));
        assertEquals(List.of(P200), result, WRONG_POINT);

        // BV24: Ray starts at sphere center — 1 intersection at far surface
        result = SPHERE.findIntersections(new Ray(P100, V100));
        assertEquals(List.of(P200), result, WRONG_POINT);

        // BV25: Ray starts on the surface, opposite direction from center — 0 intersections
        assertNull(SPHERE.findIntersections(new Ray(P200, V100)), NO_INTERSECTION);

        // BV26: Ray starts past sphere along center line, sphere entirely behind — 0 intersections
        assertNull(SPHERE.findIntersections(new Ray(new Point(3, 0, 0), V100)), NO_INTERSECTION);

        // ** Group 3: Ray tangent to sphere — all cases yield 0 intersections **

        // BV31: Tangent ray starts before the tangent point (1,1,0)
        assertNull(SPHERE.findIntersections(new Ray(new Point(0, 1, 0), V100)), NO_INTERSECTION);

        // BV32: Tangent ray starts exactly at the tangent point (1,1,0)
        assertNull(SPHERE.findIntersections(new Ray(P110, V100)), NO_INTERSECTION);

        // BV33: Tangent ray starts after the tangent point
        assertNull(SPHERE.findIntersections(new Ray(new Point(2, 1, 0), V100)), NO_INTERSECTION);

        // ** Group 4: Ray direction orthogonal to [ray-origin → center] **

        // BV41: Ray is outside sphere, direction perpendicular to [origin→center] — 0 intersections
        // origin=(-1,0,0), vec-to-center=(2,0,0), direction=(0,1,0): dot=0; distance=2 > radius
        assertNull(SPHERE.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(0, 1, 0))),
                NO_INTERSECTION);

        // BV42: Ray starts inside sphere, direction perpendicular to [origin→center] — 1 intersection
        // origin=(1,0.5,0), vec-to-center=(0,-0.5,0), direction=(1,0,0): dot=0; distance=0.5 < radius
        result = SPHERE.findIntersections(new Ray(new Point(1, 0.5, 0), V100));
        assertEquals(List.of(new Point(1 + sqrt(3/4.0), 0.5, 0)), result, WRONG_POINT);
    }
}
