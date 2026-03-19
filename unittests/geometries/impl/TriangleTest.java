package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Test class for {@link Triangle} class */
class TriangleTest {
    /** Delta for accuracy when comparing double values */
    private static final double DELTA = 1e-6;

    /** Default constructor for TriangleTest to satisfy Javadoc tool */
    public TriangleTest() {}

    /** Test method for {@link Triangle#getNormal(Point)} */
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
}