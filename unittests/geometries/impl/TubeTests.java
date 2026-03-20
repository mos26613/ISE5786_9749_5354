package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link Tube} class
 */
class TubeTests {
    /** Default constructor for TubeTest to satisfy Javadoc tool */
    public TubeTests() {}

    /**
     * Delta value for accuracy of double comparisons
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for wrong normal length
     */
    private static final String ERR_LENGTH = "ERROR: getNormal() result is not a unit vector";

    /**
     * Error message for wrong normal direction (not orthogonal to axis)
     */
    private static final String ERR_ORTHOGONAL = "ERROR: getNormal() result is not orthogonal to the tube axis";

    /**
     * Test method for {@link Tube#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {
        // Tube with axis along the Z-axis, origin at (0,0,0), radius = 1
        Vector direction = new Vector(0, 0, 1);
        Tube tube = new Tube(1, new Ray(Point.ZERO, direction));

        // ============ Equivalence Partitions Tests ==============

        // EP01: point on the tube surface in front of the ray origin (t > 0)
        Point pFront = new Point(1, 0, 2);
        Vector nFront = tube.getNormal(pFront);
        assertEquals(1, nFront.length(), DELTA, ERR_LENGTH);
        assertEquals(0, nFront.dotProduct(direction), DELTA, ERR_ORTHOGONAL);

        // EP02: point on the tube surface behind the ray origin (t < 0)
        Point pBehind = new Point(0, 1, -3);
        Vector nBehind = tube.getNormal(pBehind);
        assertEquals(1, nBehind.length(), DELTA, ERR_LENGTH);
        assertEquals(0, nBehind.dotProduct(direction), DELTA, ERR_ORTHOGONAL);

        // =============== Boundary Values Tests ==================

        // BV01: point on the tube surface at the ray origin level (t = 0,
        //       projection of point onto axis lands exactly at the origin)
        Point pLevel = new Point(1, 0, 0);
        Vector nLevel = tube.getNormal(pLevel);
        assertEquals(1, nLevel.length(), DELTA, ERR_LENGTH);
        assertEquals(0, nLevel.dotProduct(direction), DELTA, ERR_ORTHOGONAL);
    }
}