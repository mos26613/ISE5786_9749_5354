package primitives;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Ray} class
 */
class RayTests {

    /**
     * Default constructor to satisfy JavaDoc generator
     */
    public RayTests() {}

    private final static String FIND_CLOSEST_POINT_ERR = "ERROR at findClosestPoint()";

    /**
     * Test method for {@link Ray#Ray(Point, Vector)}.
     * Verifies that the constructor correctly sets the origin and normalizes the direction vector.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // Test that the constructor correctly normalizes the direction vector
        Point origin = new Point(1, 2, 3);
        Vector direction = new Vector(4, 0, 0);
        Ray ray = new Ray(origin, direction);

        // EP01 Tests that constructor is implemented correctly
        assertEquals("Origin: (1.0,2.0,3.0)\nDirection: (1.0,0.0,0.0)", ray.toString(),
                "ERROR: Ray constructor does not set the origin and direction correctly");
        //EP02 Tests that the constructor normalizes the direction vector
        assertEquals(new Vector(1, 0, 0), ray.direction(),
                "ERROR: Ray constructor does not normalize the direction vector");
    }

    /**
     * Test for {@link Ray#getPoint(double)}.
     * Verifies that the method correctly calculates the point at a given distance along the ray.
     */
    @Test
    void testGetPoint() {
        Ray ray = new Ray(Point.ZERO, new Vector(0, 0, 1));

        // ====== Equivalence Partitions Tests ======

        // EP01: Case - t>0
        assertEquals(new Point(0, 0, 2), ray.getPoint(2),
                "ERROR: getPoint() does not return the correct point for t>0");

        // EP02: Case - t<0
        assertEquals(new Point(0, 0, -2), ray.getPoint(-2),
                "ERROR: getPoint() does not return the correct point for t<0");

        // ====== Boundary Values Tests ======

        // BV01: Case - t=0
        assertEquals(Point.ZERO, ray.getPoint(0),
                "ERROR: getPoint() does not return the origin point for t=0");
    }

    /**
     * Test for {@link Ray#findClosestPoint(List)}.
     */
    @Test
    void testFindClosestPoint() {
        Ray ray = new Ray(Point.ZERO, new Vector(1, 0, 0));
        Point p1 = new Point(2, 0, 0);
        Point p2 = new Point(3, 0, 0);
        Point p3 = new Point(4, 0, 0);

        // ====== Equivalence Partitions Tests ======

        // EP01: Case - A point in the middle of the list is closest to the ray

        assertEquals(p1, ray.findClosestPoint(List.of(p2, p1, p3)), FIND_CLOSEST_POINT_ERR);

        // ====== Boundary Values Tests ======

        //BV01 Case - Gets null (empty list)
        assertNull(ray.findClosestPoint(null), FIND_CLOSEST_POINT_ERR);

        //BV02 Case - A point in the beginning of the list is closest to the ray
        assertEquals(p1, ray.findClosestPoint(List.of(p1, p2, p3)), FIND_CLOSEST_POINT_ERR);

        //BV03 Case - A point in the end of the list is closest to the ray
        assertEquals(p1, ray.findClosestPoint(List.of(p3, p2, p1)), FIND_CLOSEST_POINT_ERR);
    }

}