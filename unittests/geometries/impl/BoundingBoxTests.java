package geometries.impl;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import geometries.api.Intersectable;
import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test class for the CBR bounding boxes produced by {@link Intersectable#getBoundingBox()}:
 * one box per geometry kind, the {@link Geometries} composite union, and the guarantee that
 * the CBR early-reject ({@link Intersectable#setCBR(boolean)}) never changes intersection results.
 */
class BoundingBoxTests {
    private static final Vector V001 = new Vector(0, 0, 1);

    private static final String WRONG_BOX = "Wrong bounding box";
    private static final String SHOULD_BE_UNBOUNDED = "Infinite/unbounded geometry must report a null box";
    private static final String CBR_CHANGED = "CBR early-reject changed the intersection result";

    /** Default constructor to satisfy the Javadoc tool. */
    BoundingBoxTests() {
    }

    /** Leave the global CBR switch off after each test so other suites are unaffected. */
    @AfterEach
    void resetCBR() {
        Intersectable.setCBR(false);
    }

    /**
     * Test method for {@link Sphere#createBoundingBox()}.
     */
    @Test
    void testSphereBox() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: sphere box is the centre +/- the radius on each axis
        Sphere sphere = new Sphere(new Point(1, 2, 3), 2);
        assertEquals(new AABB(new Point(-1, 0, 1), new Point(3, 4, 5)),
                sphere.getBoundingBox(), WRONG_BOX);
    }

    /**
     * Test method for {@link Polygon#createBoundingBox()} as inherited by {@link Triangle}.
     */
    @Test
    void testTriangleBox() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: triangle box is the per-axis min/max of its vertices
        Triangle triangle = new Triangle(new Point(0, 0, 0), new Point(2, 0, 0), new Point(0, 3, 1));
        assertEquals(new AABB(new Point(0, 0, 0), new Point(2, 3, 1)),
                triangle.getBoundingBox(), WRONG_BOX);
    }

    /**
     * Test method for {@link Polygon#createBoundingBox()}.
     */
    @Test
    void testPolygonBox() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: polygon box is the per-axis min/max over all of its vertices
        Polygon polygon = new Polygon(new Point(0, 0, 0), new Point(2, 0, 0),
                new Point(2, 2, 0), new Point(0, 2, 0));
        assertEquals(new AABB(new Point(0, 0, 0), new Point(2, 2, 0)),
                polygon.getBoundingBox(), WRONG_BOX);
    }

    /**
     * Test method for {@link Cylinder#createBoundingBox()}.
     */
    @Test
    void testCylinderBox() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: conservative box of an axis-aligned cylinder = base centres grown by radius
        Cylinder cylinder = new Cylinder(1, new Ray(Point.ZERO, V001), 4);
        assertEquals(new AABB(new Point(-1, -1, -1), new Point(1, 1, 5)),
                cylinder.getBoundingBox(), WRONG_BOX);
    }

    /**
     * Test method for the unbounded geometries ({@link Plane}, {@link Tube}).
     */
    @Test
    void testInfiniteGeometriesHaveNoBox() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: an infinite plane cannot be bounded
        assertNull(new Plane(Point.ZERO, V001).getBoundingBox(), SHOULD_BE_UNBOUNDED);
        // EP02: an infinite tube cannot be bounded
        assertNull(new Tube(1, new Ray(Point.ZERO, V001)).getBoundingBox(), SHOULD_BE_UNBOUNDED);
    }

    /**
     * Test method for {@link Geometries#createBoundingBox()}.
     */
    @Test
    void testGeometriesBox() {
        Sphere a = new Sphere(new Point(0, 0, 0), 1);
        Sphere b = new Sphere(new Point(5, 0, 0), 1);

        // ============ Equivalence Partitions Tests ==============
        // EP01: composite box is the union of the children's boxes
        assertEquals(new AABB(new Point(-1, -1, -1), new Point(6, 1, 1)),
                new Geometries(a, b).getBoundingBox(), WRONG_BOX);

        // =============== Boundary Values Tests ==================
        // BV01: a composite containing an unbounded child is itself unbounded
        assertNull(new Geometries(a, new Plane(Point.ZERO, V001)).getBoundingBox(), SHOULD_BE_UNBOUNDED);
        // BV02: an empty composite is unbounded
        assertNull(new Geometries().getBoundingBox(), SHOULD_BE_UNBOUNDED);
    }

    /**
     * Test method verifying that enabling the CBR early-reject in
     * {@link Intersectable#calcIntersections(Ray)} never changes the result of
     * {@link Intersectable#findIntersections(Ray)} — for rays that hit and rays that miss.
     */
    @Test
    void testCBRPreservesIntersections() {
        Geometries geometries = new Geometries(
                new Sphere(new Point(0, 0, -3), 1),
                new Sphere(new Point(5, 0, -3), 1),
                new Triangle(new Point(-2, -2, -6), new Point(2, -2, -6), new Point(0, 2, -6)));

        // ray hitting the first sphere (and the triangle behind it)
        assertSameWithAndWithoutCBR(geometries, new Ray(new Point(0, 0, 0), new Vector(0, 0, -1)));
        // ray hitting the second sphere only
        assertSameWithAndWithoutCBR(geometries, new Ray(new Point(5, 0, 0), new Vector(0, 0, -1)));
        // ray passing through the composite region but between every body (hits nothing)
        assertSameWithAndWithoutCBR(geometries, new Ray(new Point(2.5, 0, 0), new Vector(0, 0, -1)));
        // ray aimed completely away from the scene
        assertSameWithAndWithoutCBR(geometries, new Ray(new Point(0, 0, 0), new Vector(0, 1, 0)));
    }

    /**
     * Asserts that the given collection returns identical intersection points for the ray
     * whether or not the CBR early-reject is enabled.
     *
     * @param geometries the collection to query
     * @param ray        the ray to trace
     */
    private void assertSameWithAndWithoutCBR(Geometries geometries, Ray ray) {
        Intersectable.setCBR(false);
        List<Point> withoutCBR = geometries.findIntersections(ray);
        Intersectable.setCBR(true);
        List<Point> withCBR = geometries.findIntersections(ray);
        assertEquals(withoutCBR, withCBR, CBR_CHANGED);
    }
}
