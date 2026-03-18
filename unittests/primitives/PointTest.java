package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link Point} class
 */
class PointTest {

    /**
     * Test constants for point operations
     */
    private static final Point POINT1 = new Point(3, 4, 0);
    private static final Point POINT2 = new Point(-3, -4, 0);
    private static final double DELTA = 1e-6;

    /**
     * Test for the constructor of the Point class
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC1: Correct constructor
        assertEquals("(3.0,4.0,0.0)", POINT1.toString(), "Constructor not properly implemented.");
    }

    /**
     * Test for {@link Point#subtract(Point)}
     */
    @Test
    void testSubtract() {
        Vector expected = new Vector(6, 8, 0);
        Vector actual = POINT1.subtract(POINT2);

        // ============ Equivalence Partitions Tests ==============

        // TC1: Correct subtract
        assertEquals(expected, actual, "Subtract method did not return the expected vector");

        // =============== Boundary Values Tests ==================

        // TC1: Subtracting a point from itself should throw an exception
        assertThrows(IllegalArgumentException.class, () -> POINT1.subtract(POINT1), "Subtracting a point from itself should throw an exception");
    }

    /**
     * Test for {@link Point#add(Vector)}
     */
    @Test
    void testAdd() {
        Vector vector = new Vector(1, 1, 1);
        Point expected = new Point(4, 5, 1);
        Point actual = POINT1.add(vector);

        // ============ Equivalence Partitions Tests ==============

        // TC1: Correct add
        assertEquals(expected, actual, "Add method did not return the expected point");
    }

    /**
     * Test for {@link Point#distanceSquared(Point)}
     */
    @Test
    void testDistanceSquared() {
        // ============ Equivalence Partitions Tests ==============

        // TC1: Correct distance squared
        assertEquals(100, POINT1.distanceSquared(POINT2), DELTA, "Distance squared method did not return the expected value");
    }

    /**
     * Test for {@link Point#distance(Point)}
     */
    @Test
    void testDistance() {
        // ============ Equivalence Partitions Tests ==============

        // TC1: Correct distance
        assertEquals(10, POINT1.distance(POINT2), DELTA, "Distance method did not return the expected value");
    }
}