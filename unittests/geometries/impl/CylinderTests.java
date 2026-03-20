package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link Cylinder} class
 */
class CylinderTests {
    /**
     * Default constructor for CylinderTest to satisfy Javadoc tool
     */
    public CylinderTests() {
    }

    /**
     * Precision for double comparisons
     */
    private static final double DELTA = 1e-6;
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

    @Test
    void testConstructor() {
        Ray ray = new Ray(Point.ZERO, new Vector(0, 0, 1));
        // =============== Boundary Values Tests ==================

        // BV01: Zero height (should be valid as it represents a degenerate cylinder)
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
}