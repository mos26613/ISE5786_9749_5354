package primitives;

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

}