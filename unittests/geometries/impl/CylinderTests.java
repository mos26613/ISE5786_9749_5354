package geometries.impl;

import java.util.List;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link Cylinder} class
 */
class CylinderTests {
    /**
     * Error message for wrong normal on edge
     */
    private static final String ERR_EDGE = "ERROR: getNormal() wrong result for edge";
    /**
     * Error message for wrong normal on base
     */
    private static final String ERR_BASE = "ERROR: getNormal() wrong result for point on base";
    /**
     * Error message for wrong normal on side
     */
    private static final String ERR_SIDE = "ERROR: getNormal() wrong result for point on side surface";
    /**
     * Error message for a wrong intersection result
     */
    private static final String ERR_WRONG = "ERROR: findIntersections() wrong result";
    /**
     * Error message when findIntersections must return null
     */
    private static final String ERR_NOT_NULL = "ERROR: findIntersections() must return null";
    /**
     * Error message for a rim/edge case, whose result must be either null or the rim point
     */
    private static final String ERR_RIM = "ERROR: findIntersections() rim/edge result must be null or the rim point";
    /**
     * Cylinder used across the findIntersections test cases: radius 1, axis along
     * the Z-axis from the world origin, height 3. The side surface is x²+y²=1 for
     * 0 &lt; z &lt; 3, the bottom base is the disc z=0 and the top base the disc z=3.
     */
    private static final Cylinder CYL = new Cylinder(1, new Ray(Point.ZERO, new Vector(0, 0, 1)), 3);
    /**
     * Unit direction along +X — perpendicular to the axis.
     */
    private static final Vector VX = new Vector(1, 0, 0);
    /**
     * Unit direction along +Z — parallel to the axis.
     */
    private static final Vector VZ = new Vector(0, 0, 1);
    /**
     * Unit direction along +Y — perpendicular to the axis (used for tangent cases).
     */
    private static final Vector VY = new Vector(0, 1, 0);

    /**
     * Default constructor for CylinderTest to satisfy Javadoc tool
     */
    public CylinderTests() {
    }

    /**
     * Test method for {@link Cylinder#Cylinder(double, Ray, double)}
     */
    @Test
    void testConstructor() {
        Ray ray = new Ray(Point.ZERO, new Vector(0, 0, 1));
        // =============== Boundary Values Tests ==================

        // BV01: Zero height (should be valid as it represents a degenerate cylinder)
        // This is a valid case because the ray is parallel to the XY plane, so the cylinder is essentially a disk.
        // The constructor should not throw an exception.
        assertDoesNotThrow(() -> new Cylinder(1, ray, 0));

        // BV02: Negative height (should throw an exception)
        assertThrows(IllegalArgumentException.class, () -> new Cylinder(1, ray, -1));
    }

    /**
     * Test method for {@link Cylinder#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        Vector direction = new Vector(0, 0, 1);
        Cylinder cylinder = new Cylinder(1, new Ray(Point.ZERO, direction), 3);
        Vector nDirection = direction.scale(-1);

        // ============ Equivalence Partitions Tests ==============
        // EP01: point on the side surface (between the two bases)
        Vector n1 = cylinder.getNormal(new Point(1, 0, 1.5));
        Vector expected = new Vector(1, 0, 0);
        assertEquals(expected, n1, ERR_SIDE);

        // EP02: point on the bottom base (not the center)
        Vector n2 = cylinder.getNormal(new Point(0.5, 0, 0));
        assertEquals(nDirection, n2, ERR_BASE);

        // EP03: point on the top base (not the center)
        Vector n3 = cylinder.getNormal(new Point(0.5, 0, 3));
        assertEquals(direction, n3, ERR_BASE);

        // =============== Boundary Values Tests ==================
        // BV01: point on the edge between the side and the bottom base
        Vector n4 = cylinder.getNormal(new Point(1, 0, 0));
        assertTrue(n4.equals(nDirection) || n4.equals(new Vector(1, 0, 0)), ERR_EDGE);

        // BV02: point on the edge between the side and the top base
        Vector n5 = cylinder.getNormal(new Point(1, 0, 3));
        assertTrue(n5.equals(direction) || n5.equals(new Vector(1, 0, 0)), ERR_EDGE);

        // BV03: center of the bottom base
        Vector n6 = cylinder.getNormal(Point.ZERO);
        assertEquals(nDirection, n6, ERR_BASE);

        // BV04: center of the top base
        Vector n7 = cylinder.getNormal(new Point(0, 0, 3));
        assertEquals(direction, n7, ERR_BASE);
    }

    /**
     * Test method for {@link Cylinder#findIntersections(primitives.Ray)}.
     * Exercises {@code calcIntersectionsHelper} indirectly through the public NVI
     * entry point. {@link #CYL} is the finite cylinder of radius 1 with its axis on
     * the Z-axis and height 3 (bottom base z=0, top base z=3). Results are ordered
     * with the point closer to the ray origin first.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: perpendicular ray crossing the side surface twice — 2 points
        assertEquals(List.of(new Point(-1, 0, 1.5), new Point(1, 0, 1.5)),
                CYL.findIntersections(new Ray(new Point(-2, 0, 1.5), VX)), ERR_WRONG);

        // EP02: perpendicular ray starting inside, exits the side once — 1 point
        assertEquals(List.of(new Point(1, 0, 1.5)),
                CYL.findIntersections(new Ray(new Point(0, 0, 1.5), VX)), ERR_WRONG);

        // EP03: perpendicular ray missing beside the cylinder — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(-2, 2, 1.5), VX)), ERR_NOT_NULL);

        // EP04: perpendicular ray with the cylinder entirely behind it — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(2, 0, 1.5), VX)), ERR_NOT_NULL);

        // EP05: axis-parallel ray entering the bottom base and exiting the top base — 2 points
        assertEquals(List.of(new Point(0.5, 0, 0), new Point(0.5, 0, 3)),
                CYL.findIntersections(new Ray(new Point(0.5, 0, -2), VZ)), ERR_WRONG);

        // EP06: slanted ray crossing the side surface twice — 2 points
        assertEquals(List.of(new Point(-1, 0, 0.5), new Point(1, 0, 2.5)),
                CYL.findIntersections(new Ray(new Point(-2, 0, -0.5), new Vector(1, 0, 1))), ERR_WRONG);

        // EP07: slanted ray entering the side surface and exiting the top base — 2 points
        assertEquals(List.of(new Point(-1, 0, 1.5), new Point(0.5, 0, 3)),
                CYL.findIntersections(new Ray(new Point(-2, 0, 0.5), new Vector(1, 0, 1))), ERR_WRONG);

        // EP08: slanted ray entering the bottom base and exiting the side surface — 2 points
        assertEquals(List.of(new Point(0.5, 0, 0), new Point(1, 0, 0.5)),
                CYL.findIntersections(new Ray(new Point(-0.5, 0, -1), new Vector(1, 0, 1))), ERR_WRONG);

        // EP09: axis-parallel ray with the cylinder entirely behind it — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(0.5, 0, 5), VZ)), ERR_NOT_NULL);

        // EP10: slanted ray missing beside the cylinder — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(-2, 2, 0.5), new Vector(1, 0, 1))), ERR_NOT_NULL);

        // =============== Boundary Values Tests ==================

        // **** Group 1: perpendicular ray level with the (infinite) tube but off the finite body ****

        // BV11: line crosses the tube above the top base — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(-2, 0, 4), VX)), ERR_NOT_NULL);

        // BV12: line crosses the tube below the bottom base — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(-2, 0, -1), VX)), ERR_NOT_NULL);

        // BV13: ray lies in the bottom base plane (z=0), parallel to that disc — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(-2, 0, 0), VX)), ERR_NOT_NULL);

        // **** Group 2: axis-parallel rays through the bases ****

        // BV21: starts inside between the bases, exits the top base only — 1 point
        assertEquals(List.of(new Point(0.5, 0, 3)),
                CYL.findIntersections(new Ray(new Point(0.5, 0, 1), VZ)), ERR_WRONG);

        // BV22: parallel, outside the bases' radius — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(2, 0, -2), VZ)), ERR_NOT_NULL);

        // BV23: parallel, exactly on the rim of the bases (edge) — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(1, 0, -2), VZ)), ERR_NOT_NULL);

        // BV24: anti-parallel from above, entering the top base then the bottom base — 2 points
        assertEquals(List.of(new Point(0.5, 0, 3), new Point(0.5, 0, 0)),
                CYL.findIntersections(new Ray(new Point(0.5, 0, 5), new Vector(0, 0, -1))), ERR_WRONG);

        // **** Group 3: perpendicular ray (z=1.5) passing through the axis ****

        // BV31: starts on the side surface, aimed inward through the axis — 1 point
        assertEquals(List.of(new Point(1, 0, 1.5)),
                CYL.findIntersections(new Ray(new Point(-1, 0, 1.5), VX)), ERR_WRONG);

        // BV32: starts inside (between the axis and the surface) — 1 point
        assertEquals(List.of(new Point(1, 0, 1.5)),
                CYL.findIntersections(new Ray(new Point(-0.5, 0, 1.5), VX)), ERR_WRONG);

        // BV33: starts on the side surface, aimed outward — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(1, 0, 1.5), VX)), ERR_NOT_NULL);

        // **** Group 4: perpendicular ray (z=1.5) starting on the side, not through the axis ****

        // BV41: starts on the surface, inward chord — 1 point
        assertEquals(List.of(new Point(0, 1, 1.5)),
                CYL.findIntersections(new Ray(new Point(1, 0, 1.5), new Vector(-1, 1, 0))), ERR_WRONG);

        // BV42: starts on the surface, outward — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(1, 0, 1.5), new Vector(1, 1, 0))), ERR_NOT_NULL);

        // **** Group 5: perpendicular ray (z=1.5) tangent to the side surface (all 0 points) ****

        // BV51: tangent, starts before the touch point
        assertNull(CYL.findIntersections(new Ray(new Point(1, -1, 1.5), VY)), ERR_NOT_NULL);

        // BV52: tangent, starts at the touch point
        assertNull(CYL.findIntersections(new Ray(new Point(1, 0, 1.5), VY)), ERR_NOT_NULL);

        // BV53: tangent, starts after the touch point
        assertNull(CYL.findIntersections(new Ray(new Point(1, 1, 1.5), VY)), ERR_NOT_NULL);

        // **** Group 6: slanted ray starting on the surface (boundary positions) ****

        // BV61: starts on the bottom base, exits through the side surface — 1 point
        assertEquals(List.of(new Point(1, 0, 1.5)),
                CYL.findIntersections(new Ray(new Point(-0.5, 0, 0), new Vector(1, 0, 1))), ERR_WRONG);

        // BV62: starts on the side surface, aimed outward — 0 points
        assertNull(CYL.findIntersections(new Ray(new Point(1, 0, 1.5), new Vector(1, 0, 1))), ERR_NOT_NULL);

        // BV63: starts on the axis line at the bottom base, exits through the side surface — 1 point
        assertEquals(List.of(new Point(1, 0, 1)),
                CYL.findIntersections(new Ray(new Point(0, 0, 0), new Vector(1, 0, 1))), ERR_WRONG);

        // **** Group 7: ray exiting exactly at a rim (base–side edge) — lenient: null or the rim point ****

        // BV71: slanted ray exiting at the top rim corner (1,0,3)
        List<Point> rimTop = CYL.findIntersections(new Ray(new Point(-1, 0, 1), new Vector(1, 0, 1)));
        assertTrue(rimTop == null || rimTop.equals(List.of(new Point(1, 0, 3))), ERR_RIM);

        // BV72: slanted ray exiting at the bottom rim corner (1,0,0)
        List<Point> rimBottom = CYL.findIntersections(new Ray(new Point(-1, 0, 2), new Vector(1, 0, -1)));
        assertTrue(rimBottom == null || rimBottom.equals(List.of(new Point(1, 0, 0))), ERR_RIM);
    }
}