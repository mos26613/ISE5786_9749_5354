package geometries.impl;

import java.util.List;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class for {@link Tube} class
 */
class TubeTests {
    /**
     * Delta value for accuracy of double comparisons
     */
    private static final double DELTA = 1e-6;
    /**
     * Error message for wrong normal length
     */
    private static final String ERR_LENGTH = "ERROR: getNormal() result is not a unit vector";
    /**
     * Error message for wrong normal direction (not orthogonal to axis)
     */
    private static final String ERR_ORTHOGONAL = "ERROR: getNormal() result is not orthogonal to the tube axis";
    /**
     * Tube used across the findIntersections test cases: radius 1, axis along the
     * Z-axis (axis origin deliberately off the world origin), so the surface is
     * the infinite cylinder x²+y²=1.
     */
    private static final Tube TUBE = new Tube(1, new Ray(new Point(0, 0, 1), new Vector(0, 0, 1)));
    /**
     * Unit direction along +X — perpendicular to the axis (90°).
     */
    private static final Vector VX = new Vector(1, 0, 0);
    /**
     * Unit direction along +Y — perpendicular to the axis (90°).
     */
    private static final Vector VY = new Vector(0, 1, 0);
    /**
     * Unit direction along +Z — parallel to the axis (0°).
     */
    private static final Vector VZ = new Vector(0, 0, 1);
    /**
     * Direction rising with the axis (positive z component → acute angle to the axis).
     */
    private static final Vector V_UP = new Vector(1, 0, 1);
    /**
     * Direction descending against the axis (negative z component → obtuse angle to the axis).
     */
    private static final Vector V_DOWN = new Vector(1, 0, -1);
    /**
     * Half-chord length √0.75 — the |x| of the intersections on the line y = 0.5.
     */
    private static final double SQRT075 = Math.sqrt(0.75);
    /**
     * Error message for a wrong intersection result
     */
    private static final String ERR_WRONG = "ERROR: findIntersections() wrong result";
    /**
     * Error message when findIntersections must return null
     */
    private static final String ERR_NOT_NULL = "ERROR: findIntersections() must return null";

    /**
     * Default constructor for TubeTest to satisfy Javadoc tool
     */
    public TubeTests() {
    }

    /**
     * Test method for {@link Tube#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        // Tube with axis along the Z-axis, origin at (0,0,0), radius = 1
        Vector direction = new Vector(0, 0, 1);
        Tube tube = new Tube(1, new Ray(Point.ZERO, direction));

        // ============ Equivalence Partitions Tests ==============

        // EP01: point on the tube surface in front of the ray origin (t > 0)
        Point pFront = new Point(1, 0, 2);
        Vector nFront = tube.getNormal(pFront);
        assertEquals(1, nFront.length(), DELTA, ERR_LENGTH);
        assertEquals(0, nFront.dotProduct(direction), DELTA, ERR_ORTHOGONAL);

        // EP02: point on the tube surface behind the ray origin (t < 0)
        Point pBehind = new Point(0, 1, -3);
        Vector nBehind = tube.getNormal(pBehind);
        assertEquals(1, nBehind.length(), DELTA, ERR_LENGTH);
        assertEquals(0, nBehind.dotProduct(direction), DELTA, ERR_ORTHOGONAL);

        // =============== Boundary Values Tests ==================

        // BV01: point on the tube surface at the ray origin level (t = 0,
        //       projection of point onto axis lands exactly at the origin)
        Point pLevel = new Point(1, 0, 0);
        Vector nLevel = tube.getNormal(pLevel);
        assertEquals(1, nLevel.length(), DELTA, ERR_LENGTH);
        assertEquals(0, nLevel.dotProduct(direction), DELTA, ERR_ORTHOGONAL);
    }

    /**
     * Test method for {@link Tube#findIntersections(primitives.Ray)}.
     * Exercises {@code calcIntersectionsHelper} indirectly through the public NVI
     * entry point. The tube's axis is the Z-axis (surface x²+y²=1); the angle
     * between each ray and the axis is set via the ray direction's z-component
     * (0 → 90°, positive → acute, negative → obtuse, none → parallel).
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // EP01 (90°): outside, crosses the tube (skew to the axis) — 2 points
        assertEquals(List.of(new Point(-SQRT075, 0.5, 5), new Point(SQRT075, 0.5, 5)),
                TUBE.findIntersections(new Ray(new Point(-2, 0.5, 5), VX)), ERR_WRONG);

        // EP02 (90°): starts inside the tube — 1 point
        assertEquals(List.of(new Point(SQRT075, 0.5, 5)),
                TUBE.findIntersections(new Ray(new Point(-0.5, 0.5, 5), VX)), ERR_WRONG);

        // EP03 (90°): outside, tube entirely behind the ray — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(2, 0.5, 5), VX)), ERR_NOT_NULL);

        // EP04 (90°): outside, ray misses beside the tube — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(-2, 2, 5), VX)), ERR_NOT_NULL);

        // EP05 (acute): outside, crosses the tube (skew) — 2 points
        assertEquals(List.of(new Point(-SQRT075, 0.5, 5 - SQRT075), new Point(SQRT075, 0.5, 5 + SQRT075)),
                TUBE.findIntersections(new Ray(new Point(-2, 0.5, 3), V_UP)), ERR_WRONG);

        // EP06 (acute): starts inside the tube — 1 point
        assertEquals(List.of(new Point(1, 0, 4.5)),
                TUBE.findIntersections(new Ray(new Point(-0.5, 0, 3), V_UP)), ERR_WRONG);

        // EP07 (acute): outside, tube entirely behind the ray — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(2, 0, 3), V_UP)), ERR_NOT_NULL);

        // EP08 (acute): outside, ray misses beside the tube — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(-2, 2, 3), V_UP)), ERR_NOT_NULL);

        // EP09 (obtuse): outside, crosses the tube (skew) — 2 points
        assertEquals(List.of(new Point(-SQRT075, 0.5, 5 + SQRT075), new Point(SQRT075, 0.5, 5 - SQRT075)),
                TUBE.findIntersections(new Ray(new Point(-2, 0.5, 7), V_DOWN)), ERR_WRONG);

        // EP10 (obtuse): starts inside the tube — 1 point
        assertEquals(List.of(new Point(1, 0, 5.5)),
                TUBE.findIntersections(new Ray(new Point(-0.5, 0, 7), V_DOWN)), ERR_WRONG);

        // EP11 (obtuse): outside, ray misses beside the tube — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(-2, 2, 7), V_DOWN)), ERR_NOT_NULL);

        // EP12 (obtuse): outside, tube entirely behind the ray — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(2, 0, 7), V_DOWN)), ERR_NOT_NULL);

        // EP13 (acute): starts inside the tube (skew) — 1 point
        assertEquals(List.of(new Point(SQRT075, 0.5, 3.5 + SQRT075)),
                TUBE.findIntersections(new Ray(new Point(-0.5, 0.5, 3), V_UP)), ERR_WRONG);

        // =============== Boundary Values Tests ==================

        // **** Group 1: 90°, ray's line passes through the axis ****

        // BV11: starts before the tube, through the axis — 2 points
        assertEquals(List.of(new Point(-1, 0, 5), new Point(1, 0, 5)),
                TUBE.findIntersections(new Ray(new Point(-2, 0, 5), VX)), ERR_WRONG);

        // BV12: starts on the surface, through the axis, inward — 1 point
        assertEquals(List.of(new Point(1, 0, 5)),
                TUBE.findIntersections(new Ray(new Point(-1, 0, 5), VX)), ERR_WRONG);

        // BV13: starts inside, through the axis — 1 point
        assertEquals(List.of(new Point(1, 0, 5)),
                TUBE.findIntersections(new Ray(new Point(-0.5, 0, 5), VX)), ERR_WRONG);

        // BV14: starts on the axis line — 1 point
        assertEquals(List.of(new Point(1, 0, 5)),
                TUBE.findIntersections(new Ray(new Point(0, 0, 5), VX)), ERR_WRONG);

        // BV15: starts on the surface, through the axis, outward — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(1, 0, 5), VX)), ERR_NOT_NULL);

        // BV16: starts past the tube on the axis line — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(2, 0, 5), VX)), ERR_NOT_NULL);

        // **** Group 2: 90°, ray starts on the surface, not through the axis ****

        // BV21: starts on the surface, inward chord — 1 point
        assertEquals(List.of(new Point(0, 1, 5)),
                TUBE.findIntersections(new Ray(new Point(1, 0, 5), new Vector(-1, 1, 0))), ERR_WRONG);

        // BV22: starts on the surface, outward — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(1, 0, 5), new Vector(1, 1, 0))), ERR_NOT_NULL);

        // **** Group 3: 90°, ray tangent to the tube (all 0 points) ****

        // BV31: tangent, starts before the touch point
        assertNull(TUBE.findIntersections(new Ray(new Point(1, -1, 5), VY)), ERR_NOT_NULL);

        // BV32: tangent, starts at the touch point
        assertNull(TUBE.findIntersections(new Ray(new Point(1, 0, 5), VY)), ERR_NOT_NULL);

        // BV33: tangent, starts after the touch point
        assertNull(TUBE.findIntersections(new Ray(new Point(1, 1, 5), VY)), ERR_NOT_NULL);

        // **** Group 4: 90°, direction orthogonal to the radial (origin→axis) direction ****

        // BV41: outside, direction orthogonal to the radial — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(-2, 0, 5), VY)), ERR_NOT_NULL);

        // BV42: inside, direction orthogonal to the radial — 1 point
        assertEquals(List.of(new Point(SQRT075, 0.5, 5)),
                TUBE.findIntersections(new Ray(new Point(0, 0.5, 5), VX)), ERR_WRONG);

        // **** Group 5: ray parallel to the axis (no side intersection — all 0 points) ****

        // BV51: parallel, outside the tube
        assertNull(TUBE.findIntersections(new Ray(new Point(2, 0, 5), VZ)), ERR_NOT_NULL);

        // BV52: parallel, inside the tube
        assertNull(TUBE.findIntersections(new Ray(new Point(0.5, 0, 5), VZ)), ERR_NOT_NULL);

        // BV53: parallel, on the surface
        assertNull(TUBE.findIntersections(new Ray(new Point(1, 0, 5), VZ)), ERR_NOT_NULL);

        // BV54: parallel, on the axis line
        assertNull(TUBE.findIntersections(new Ray(new Point(0, 0, 5), VZ)), ERR_NOT_NULL);

        // BV55: anti-parallel (180°), outside the tube
        assertNull(TUBE.findIntersections(new Ray(new Point(2, 0, 5), new Vector(0, 0, -1))), ERR_NOT_NULL);

        // **** Group 6: acute angle, boundary positions ****

        // BV61: starts on the surface, inward — 1 point
        assertEquals(List.of(new Point(-1, 0, 5)),
                TUBE.findIntersections(new Ray(new Point(1, 0, 3), new Vector(-1, 0, 1))), ERR_WRONG);

        // BV62: starts on the axis line — 1 point
        assertEquals(List.of(new Point(1, 0, 4)),
                TUBE.findIntersections(new Ray(new Point(0, 0, 3), V_UP)), ERR_WRONG);

        // BV63: tangent — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(1, -1, 3), new Vector(0, 1, 1))), ERR_NOT_NULL);

        // BV64: starts on the surface, outward — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(1, 0, 3), V_UP)), ERR_NOT_NULL);

        // **** Group 7: obtuse angle, boundary positions ****

        // BV71: starts on the surface, inward — 1 point
        assertEquals(List.of(new Point(-1, 0, 5)),
                TUBE.findIntersections(new Ray(new Point(1, 0, 7), new Vector(-1, 0, -1))), ERR_WRONG);

        // BV72: starts on the axis line — 1 point
        assertEquals(List.of(new Point(1, 0, 6)),
                TUBE.findIntersections(new Ray(new Point(0, 0, 7), V_DOWN)), ERR_WRONG);

        // BV73: tangent — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(1, 1, 7), new Vector(0, -1, -1))), ERR_NOT_NULL);

        // BV74: starts on the surface, outward — 0 points
        assertNull(TUBE.findIntersections(new Ray(new Point(1, 0, 7), V_DOWN)), ERR_NOT_NULL);
    }
}