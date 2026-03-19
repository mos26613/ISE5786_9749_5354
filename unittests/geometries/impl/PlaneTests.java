package geometries.impl;

import primitives.Point;
import primitives.Vector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link Plane} class
 */
class PlaneTests {
    /** Delta for accuracy when comparing double values */
    private static final double DELTA = 1e-6;
    /** A point on the plane */
    private static final Point PO = Point.ZERO;
    /** A point on the plane */
    private static final Point PX = new Point(2, 0, 0);
    /** A point on the plane */
    private static final Point PY = new Point(0, 2, 0);
    /** A point that is on the same line as PO and PX, used for boundary value tests */
    private static final Point MIDDLE = new Point(1, 1, 0);
    /** A normal vector to the plane */
    private static final Vector NORMAL = new Vector(0, 0, 1);

    /**
     * Default constructor for PlaneTests to satisfy Javadoc tool
     */
    public PlaneTests() {}

    @Test
    void testConstructor1() {
        Plane actual = new Plane(PX, NORMAL);

        // ============ Equivalence Partitions Tests ==============

        // TC01 checks regular case
        assertEquals("Point: (2.0,0.0,0.0)\nNormal: (0.0,0.0,1.0)", actual.toString(),
                "Constructor not properly implemented.");
    }

    @Test
    void testConstructor2() {
        Plane actual  = new Plane(PX, PY, PO);

        // ============ Equivalence Partitions Tests ==============

        //EP01 checks regular case
        assertEquals("Point: (2.0,0.0,0.0)\nNormal: (0.0,0.0,1.0)",
                actual.toString(),
                "Constructor not properly implemented.");

        // ================= Boundary Values Tests ================

        //BV01 checks invalidity for 2 identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(PX, PX, PO),
                "Constructor should throw an exception when given collinear points");
        assertThrows(IllegalArgumentException.class, () -> new Plane(PX, PY, PX),
                "Constructor should throw an exception when given collinear points");
        assertThrows(IllegalArgumentException.class, () -> new Plane(PX, PY, PY),
                "Constructor should throw an exception when given collinear points");
        //BV02 checks invalidity for 3 identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(PX, PX, PX),
                "Constructor should throw an exception when given identical points");
        //BV03 checks invalidity for 3 points on the same line
        assertThrows(IllegalArgumentException.class, () -> new Plane(PX, PY, MIDDLE),
                "Constructor should throw an exception when given points that are on the same line");
    }
    @Test
    void testGetNormal() {
        Plane plane = new Plane(PX, PY, PO);


        // ============ Equivalence Partitions Tests ==============

        // EP01: point on the plane that is not a reference point
        Vector result = plane.getNormal(MIDDLE);

        // check that the normal length is 1
        assertEquals(1, result.length(), DELTA, "Normal is not a unit vector");
        // check that the normal is orthogonal to the plane edges
        assertEquals(0, result.dotProduct(PX.subtract(PO)), DELTA, "Normal is not orthogonal to the plane");
        assertEquals(0, result.dotProduct(PY.subtract(PO)), DELTA, "Normal is not orthogonal to the plane");

        // =============== Boundary Values Tests ==================

        // BV01: reference point of the plane
        result = plane.getNormal(PX);
        assertEquals(1, result.length(), DELTA, "Normal is not a unit vector");
        assertEquals(0, result.dotProduct(PX.subtract(PO)), DELTA, "Normal is not orthogonal to the plane");
        assertEquals(0, result.dotProduct(PY.subtract(PO)), DELTA, "Normal is not orthogonal to the plane");
    }
}