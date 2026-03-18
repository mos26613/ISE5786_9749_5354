package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link Point} class
 */
class PointTest {

    private static final Point POINT1 = new Point(3, 4, 0);
    private static final Point POINT2 = new Point(-3, -4, 0);
    private static final double DELTA = 1e-6;

    @Test
    void testConstructor() {
        assertEquals("(3.0,4.0,0.0)", POINT1.toString(), "Constructor not properly implemented.");
    }

    @Test
    void testSubtract() {
        Vector expected = new Vector(6, 8, 0);
        Vector actual = POINT1.subtract(POINT2);
        assertEquals(expected, actual, "Subtract method did not return the expected vector");
        assertThrows(IllegalArgumentException.class, () -> POINT1.subtract(POINT1), "Subtracting a point from itself should throw an exception");
    }

    @Test
    void testAdd() {
        Vector vector = new Vector(1, 1, 1);
        Point expected = new Point(4, 5, 1);
        Point actual = POINT1.add(vector);
        assertEquals(expected, actual, "Add method did not return the expected point");
    }

    @Test
    void testDistanceSquared() {
        assertEquals(100, POINT1.distanceSquared(POINT2), DELTA, "Distance squared method did not return the expected value");
    }

    @Test
    void testDistance() {
        assertEquals(10, POINT1.distance(POINT2), DELTA, "Distance method did not return the expected value");
    }
}