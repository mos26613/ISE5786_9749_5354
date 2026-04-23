package geometries.impl;

import java.util.List;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class for {@link Triangle} class
 */
class TriangleTests {
    /**
     * Delta for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;
    /**
     * Error message for unexpected intersection points
     */
    private static final String ERROR_BAD_INTERSECT = "ERROR: findIntersections() wrong result";
    /**
     * Error message when intersections should not exist
     */
    private static final String ERROR_NOT_NULL = "ERROR: findIntersections() must return null";
    /**
     * Triangle in the Z=1 plane for intersection tests: vertex A
     */
    private static final Point P001 = new Point(0, 0, 1);
    /**
     * Triangle vertex B
     */
    private static final Point P201 = new Point(2, 0, 1);
    /**
     * Triangle vertex C
     */
    private static final Point P021 = new Point(0, 2, 1);
    /**
     * Vector for tests
     */
    private static final Vector V001 = new Vector(0, 0, 1);
    /**
     * Vector for tests
     */
    private static final Vector V100 = new Vector(1, 0, 0);
    /**
     * Vector for tests
     */
    private static final Vector V111 = new Vector(1, 1, 1);
    /**
     * Default constructor for TriangleTest to satisfy Javadoc tool
     */
    public TriangleTests() {
    }

    /**
     * Test method for {@link Triangle#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        Point px = new Point(1, 0, 0);
        Point py = new Point(0, 1, 0);
        Point po = Point.ZERO;

        Triangle triangle = new Triangle(px, py, po);
        Vector actualNormal = triangle.getNormal(new Point(0.5, 0.5, 0));

        // ============ Equivalence Partitions Tests ==============

        //TC01 checks regular case
        assertEquals(1, actualNormal.length(), DELTA, "Normal vector is not a unit vector.");
        assertEquals(0, actualNormal.dotProduct(px.subtract(py)), DELTA, "Normal vector is not perpendicular to the triangle plane.");
        assertEquals(0, actualNormal.dotProduct(px.subtract(po)), DELTA, "Normal vector is not perpendicular to the triangle plane.");
    }

    /**
     * Test method for {@link Triangle#findIntersections(Ray)}.
     * Verifies correct ray-triangle intersection results.
     * <p>
     * Includes plane-level "no intersection" cases (parallel, orthogonal,
     * starting on plane) reused from Plane tests, and triangle-specific
     * sub-cases (inside, edge, vertex).
     * </p>
     */
    @Test
    void testFindIntersections() {
        Triangle triangle = new Triangle(P001, P201, P021);

        // ============ Equivalence Partitions Tests ==============
        // (Triangle-specific: ray crosses the plane — where does it hit?)

        // EP01: Ray intersects inside the triangle (1 point)
        List<Point> result = triangle.findIntersections(
                new Ray(new Point(1.5, 1.5, 0), new Vector(-1, -1, 1)));
        assertEquals(List.of(new Point(0.5, 0.5, 1)), result, ERROR_BAD_INTERSECT);

        // EP02: Ray intersects the plane but outside the triangle — against an edge (0 points)
        assertNull(triangle.findIntersections(
                new Ray(new Point(3, 3, 0), V001)), ERROR_NOT_NULL);

        // EP03: Ray intersects the plane but outside the triangle — against a vertex (0 points)
        assertNull(triangle.findIntersections(
                new Ray(new Point(-1, -1, 0), V001)), ERROR_NOT_NULL);

        // =============== Boundary Values Tests ==================

        // --- Group 1: Triangle-boundary BV ---

        // BV01: Ray hits on an edge of the triangle (0 points — edges excluded)
        assertNull(triangle.findIntersections(
                new Ray(new Point(1, 0, 0), V001)), ERROR_NOT_NULL);

        // BV02: Ray hits at a vertex of the triangle (0 points)
        assertNull(triangle.findIntersections(
                new Ray(Point.ZERO, V001)), ERROR_NOT_NULL);

        // BV03: Ray hits on the continuation of an edge outside the triangle (0 points)
        assertNull(triangle.findIntersections(
                new Ray(new Point(3, 0, 0), V001)), ERROR_NOT_NULL);

        // --- Group 2: Plane-level BV — ray parallel to the plane ---

        // BV04: Ray lies within the plane (0 points)
        assertNull(triangle.findIntersections(
                new Ray(new Point(0.5, 0.5, 1), V100)), ERROR_NOT_NULL);

        // BV05: Ray is parallel to the plane but not in it (0 points)
        assertNull(triangle.findIntersections(
                new Ray(new Point(0.5, 0.5, 2), V100)), ERROR_NOT_NULL);

        // --- Group 3: Plane-level BV — ray orthogonal to the plane ---

        // BV06: Ray is orthogonal and starts before the plane (1 point)
        result = triangle.findIntersections(
                new Ray(new Point(0.5, 0.5, 0), V001));
        assertEquals(List.of(new Point(0.5, 0.5, 1)), result, ERROR_BAD_INTERSECT);

        // BV07: Ray is orthogonal and starts in the plane (0 points)
        assertNull(triangle.findIntersections(
                new Ray(new Point(0.5, 0.5, 1), V001)), ERROR_NOT_NULL);

        // BV08: Ray is orthogonal and starts after the plane (0 points)
        assertNull(triangle.findIntersections(
                new Ray(new Point(0.5, 0.5, 2), V001)), ERROR_NOT_NULL);

        // --- Group 4: Plane-level BV — ray starts on the plane (not parallel, not orthogonal) ---

        // BV09: Ray starts on the plane, not orthogonal/parallel (0 points)
        assertNull(triangle.findIntersections(
                new Ray(new Point(0.5, 0.5, 1), V111)), ERROR_NOT_NULL);

        // --- Group 5: Plane-level BV — ray starts at the plane's reference point ---

        // BV10: Ray starts at the plane's reference point (vertex A) (0 points)
        assertNull(triangle.findIntersections(
                new Ray(P001, V111)), ERROR_NOT_NULL);
    }
}