package geometries.impl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import primitives.Point;
import primitives.Vector;

/** Test class for {@link Sphere} class */
class SphereTest {
    /** Default constructor for SphereTest to satisfy Javadoc tool */
    public SphereTest() {}

    /** Test method for {@link Sphere#Sphere(Point, double)} */
    @Test
    void testGetNormal() {
        Point center = Point.ZERO;
        Point point = new Point(1, 0, 0);
        Sphere sphere = new Sphere(center, 1);
        Vector actualNormal = sphere.getNormal(point);

            // ============ Equivalence Partitions Tests ==============

        // TC01 checks regular case
        assertEquals(point.subtract(center), actualNormal, "Normal vector is not correct.");
    }
}