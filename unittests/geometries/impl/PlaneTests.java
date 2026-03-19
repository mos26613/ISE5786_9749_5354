package geometries.impl;

import primitives.Point;
import primitives.Vector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link Plane} class
 */
class PlaneTests {

    static final Point PO = Point.ZERO;
    static final Point PX = new Point(1, 0, 0);
    static final Point PY = new Point(0, 1, 0);
    static final Point MIDDLE = new Point(0.5, 0.5, 0);

    /**
     * Default constructor for PlaneTests to satisfy Javadoc tool
     */
    public PlaneTests() {}

    @Test
    void testConstructor1() {
        Point p1 = new Point(0, 0, 1);
        Vector v1 = new Vector(3,4,0);
        Plane actual = new Plane(p1, v1);
        assertEquals("Point: (0.0,0.0,1.0)\nNormal: (0.6,0.8,0.0)", actual.toString(),
                "Constructor not properly implemented.");
    }

    @Test
    void testConstructor2() {
        Plane actual  = new Plane(PX, PY, PO);

        // ============ Equivalence Partitions Tests ==============

        //EP01 checks regular case
        assertEquals("Point: (1.0,0.0,0.0)\nNormal: (0.0,0.0,1.0)",
                actual.toString(),
                "Constructor not properly implemented.");

        // ================= Boundary Values Tests ================

        //BV01 checks invalidity for 2 identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(PX, PX, PO),
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
        Point p = new Point(1, 2, 3);
        Vector v = new Vector(0.6, 0, 0.8);
        Plane actual = new Plane(p, new Vector(3, 0, 4));

        // ============ Equivalence Partitions Tests ==============

        //EP01 checks that getNormal() returns correct normal given a point which isn't the Plane's "point"
        assertEquals(v, actual.getNormal(p), "getNormal() not implemented properly");

        //BV01 ch
        assertEquals(v, actual.getNormal(p), "getNormal() not implemented properly");
    }
}