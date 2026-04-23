package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for class {@link Geometries}
 */
class GeometriesTests {

    /**
     * Default constructor to satisfy JavaDoc generator
     */
    public GeometriesTests() {
    }

    /**
     * Test method for {@link Geometries#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        Point P00N2 = new Point(0, 0, -2);
        String errCount = "Wrong number of intersection points";
        String errEmpty = "Expected no intersection points";

        Plane plane = new Plane(Point.ZERO, new Vector(0, 0, 1));
        Sphere sphere = new Sphere(Point.ZERO, 1);
        Triangle triangle =
                new Triangle(new Point(0, 0, 2), new Point(1, 0, 2), new Point(0, 1, 2));

        Geometries geometries = new Geometries(plane, sphere, triangle);
        Ray ray;


        // ===== Equivalence Partitions Tests =====

        // EP01: Ray intersects some but not all and more than one geometry (3 points)
        ray = new Ray(P00N2, new Vector(0, 0, 1));
        assertEquals(3, geometries.findIntersections(ray).size(), errCount);

        // =====  Boundary Values Tests =====

        // BV01: Ray doesn't intersect any geometry (0 points)
        ray = new Ray(P00N2, new Vector(0, 1, 0));
        assertNull(geometries.findIntersections(ray), errEmpty);

        // BV02: Ray intersects exactly one geometry (1 point)
        ray = new Ray(P00N2, new Vector(5, 5, 5));
        assertEquals(1, geometries.findIntersections(ray).size(), errCount);

        // BV03: Ray intersects all geometries (4 points)
        ray = new Ray(P00N2, new Vector(1, 1, 12));
        assertEquals(4, geometries.findIntersections(ray).size(), errCount);
    }
}