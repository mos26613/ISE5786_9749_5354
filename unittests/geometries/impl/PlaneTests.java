package geometries.impl;

import java.util.List;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link Plane} class
 */
class PlaneTests {

    /**
     * Delta for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;

    /**
     * A point on the plane
     */
    private static final Point PO = Point.ZERO;

    /**
     * the point (1,0,0)
     */
    private static final Point PX = new Point(1, 0, 0);

    /**
     * the point (0,1,0)
     */
    private static final Point PY = new Point(0, 1, 0);

    /**
     * the point (0,0,1)
     */
    private static final Point PZ = new Point(0, 0, 1);

    /**
     * A point on the xy plane
     */
    private static final Point P200 = new Point(2, 0, 0);

    /**
     * A point on the xy plane
     */
    private static final Point P020 = new Point(0, 2, 0);

    /**
     * A point that is on the same line as PO and P020, used for boundary value tests
     */
    private static final Point P110 = new Point(1, 1, 0);

    /**
     * A normal vector to the xy plane
     */
    private static final Vector V001 = new Vector(0, 0, 1);

    /**
     * A normal vector to the xy plane
     */
    private static final Vector V00N1 = new Vector(0, 0, -1);

    /**
     * A vector that is not orthogonal to the xy plane, used for EP tests
     */
    private static final Vector V101 = new Vector(1, 0, 1);

    /**
     * A vector that is not orthogonal to the xy plane, used for EP tests
     */
    private static final Vector V10N1 = new Vector(1, 0, -1);

    /**
     * A vector that is parallel to the xy plane, used for BVA tests
     */
    private static final Vector V100 = new Vector(1, 0, 0);

    /**
     * Error message for constructor not properly implemented
     */
    private static final String ERR_CONSTRUCTOR = "Constructor not properly implemented.";

    /**
     * Error message for constructor throwing exception on collinear points
     */
    private static final String COLLINEAR_POINTS_ERROR = "Constructor should throw an exception when given collinear points";

    /**
     * Error message for constructor throwing exception on identical points
     */
    private static final String IDENTICAL_POINTS_ERROR = "Constructor should throw an exception when given identical points";

    /**
     * Error message for constructor throwing exception on points on the same line
     */
    private static final String SAME_LINE_POINTS_ERROR = "Constructor should throw an exception when given points that are on the same line";

    /**
     * Error message for normal not being a unit vector
     */
    private static final String NORMAL_NOT_UNIT_ERROR = "Normal is not a unit vector";

    /**
     * Error message for normal not being orthogonal to the plane
     */
    private static final String NORMAL_NOT_ORTHOGONAL_ERROR = "Normal is not orthogonal to the plane";

    /**
     * Error message for wrong number of intersections
     */
    private static final String WRONG_INTERSECTIONS_COUNT_ERROR = "Wrong number of intersections";

    /**
     * Error message for ray intersecting at origin
     */
    private static final String RAY_INTERSECT_ORIGIN_ERROR = "Ray should intersect the plane at the origin";

    /**
     * Error message for ray not intersecting the plane
     */
    private static final String RAY_NOT_INTERSECT_ERROR = "Ray should not intersect the plane";



    /**
     * Default constructor for PlaneTests to satisfy Javadoc tool
     */
    public PlaneTests() {
    }

    /**
     * Test method for {@link Plane#Plane(Point, Vector)}
     *
     */
    @Test
    void testConstructor1() {
        Plane actual = new Plane(P200, V001);

        // ============ Equivalence Partitions Tests ==============

        // TC01 checks regular case
        assertEquals("Point: (2.0,0.0,0.0)\nNormal: (0.0,0.0,1.0)", actual.toString(),
                ERR_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Plane#Plane(Point, Point, Point)}
     *
     */
    @Test
    void testConstructor2() {
        Plane actual = new Plane(P200, P020, PO);

        // ============ Equivalence Partitions Tests ==============

        //EP01 checks regular case
        assertEquals("Point: (2.0,0.0,0.0)\nNormal: (0.0,0.0,1.0)",
                actual.toString(),
                ERR_CONSTRUCTOR);

        // ================= Boundary Values Tests ================

        //BV01 checks invalidity for 2 identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(P200, P200, PO),
                COLLINEAR_POINTS_ERROR);
        assertThrows(IllegalArgumentException.class, () -> new Plane(P200, P020, P200),
                COLLINEAR_POINTS_ERROR);
        assertThrows(IllegalArgumentException.class, () -> new Plane(P200, P020, P020),
                COLLINEAR_POINTS_ERROR);
        //BV02 checks invalidity for 3 identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(P200, P200, P200),
                IDENTICAL_POINTS_ERROR);
        //BV03 checks invalidity for 3 points on the same line
        assertThrows(IllegalArgumentException.class, () -> new Plane(P200, P020, P110),
                SAME_LINE_POINTS_ERROR);
    }

    /**
     * Test method for {@link Plane#getNormal(Point)}
     *
     */
    @Test
    void testGetNormal() {
        Plane plane = new Plane(P200, P020, PO);


        // ============ Equivalence Partitions Tests ==============

        // EP01: point on the plane that is not a reference point
        Vector result = plane.getNormal(P110);

        // check that the normal length is 1
        assertEquals(1, result.length(), DELTA, NORMAL_NOT_UNIT_ERROR);
        // check that the normal is orthogonal to the plane edges
        assertEquals(0, result.dotProduct(P200.subtract(PO)), DELTA,
                NORMAL_NOT_ORTHOGONAL_ERROR);
        assertEquals(0, result.dotProduct(P020.subtract(PO)), DELTA,
                NORMAL_NOT_ORTHOGONAL_ERROR);

        // =============== Boundary Values Tests ==================

        // BV01: reference point of the plane
        result = plane.getNormal(P200);
        assertEquals(1, result.length(), DELTA, NORMAL_NOT_UNIT_ERROR);
        assertEquals(0, result.dotProduct(P200.subtract(PO)), DELTA,
                NORMAL_NOT_ORTHOGONAL_ERROR);
        assertEquals(0, result.dotProduct(P020.subtract(PO)), DELTA,
                NORMAL_NOT_ORTHOGONAL_ERROR);
    }

    @Test
    void findIntersections() {
        Plane xyPlane = new Plane(PO, V001);
        Ray ray;
        List<Point> result;

        // =======  Equivalence Partitions Tests =======

        //EP01 checks regular case of ray intersecting the plane
        ray = new Ray(PZ, V10N1);
        result = xyPlane.findIntersections(ray);
        assertEquals(1, result.size(), WRONG_INTERSECTIONS_COUNT_ERROR);
        assertEquals(PX, result.getFirst(), RAY_INTERSECT_ORIGIN_ERROR);

        //EP02 checks regular case of ray not intersecting the plane
        ray = new Ray(PZ, V101);
        result = xyPlane.findIntersections(ray);
        assertNull(result, RAY_NOT_INTERSECT_ERROR);

        // ======= Boundary Values Tests =======

        //BV01 checks case of ray parallel to the plane and not included in the plane
        ray = new Ray(PZ, V100);
        result = xyPlane.findIntersections(ray);
        assertNull(result, RAY_NOT_INTERSECT_ERROR);

        //BV02 checks case of ray parallel to the plane and included in the plane
        ray = new Ray(PO, V100);
        result = xyPlane.findIntersections(ray);
        assertNull(result, RAY_NOT_INTERSECT_ERROR);

        //BV03 checks case of ray orthogonal to the plane and intersecting the plane
        ray = new Ray(PZ, V00N1);
        result = xyPlane.findIntersections(ray);
        assertEquals(1, result.size(), WRONG_INTERSECTIONS_COUNT_ERROR);
        assertEquals(PO, result.getFirst(), RAY_INTERSECT_ORIGIN_ERROR);

        //BV04 checks case of ray orthogonal to the plane and not intersecting the plane
        ray = new Ray(PZ, V001);
        result = xyPlane.findIntersections(ray);
        assertNull(result, RAY_NOT_INTERSECT_ERROR);

        //BV05 checks case of ray orthogonal to the plane and starting at the plane
        ray = new Ray(PO, V001);
        result = xyPlane.findIntersections(ray);
        assertNull(result, RAY_NOT_INTERSECT_ERROR);

        //BV06 checks case of ray neither parallel nor orthogonal to the plane and starting at the plane -
        // but not starting from the reference point
        ray = new Ray(PX, V101);
        result = xyPlane.findIntersections(ray);
        assertNull(result, RAY_NOT_INTERSECT_ERROR);

        //BV07 checks for ray neither parallel nor orthogonal to the plane and starting at the plane -
        // but starting from the reference point
        ray = new Ray(PO, V101);
        result = xyPlane.findIntersections(ray);
        assertNull(result, RAY_NOT_INTERSECT_ERROR);
    }
}