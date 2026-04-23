package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test class for {@link Vector} class.
 * The tests verify:
 * <ul>
 * <li>Vector constructor validity</li>
 * <li>{@link Vector#subtract(Point)}</li>
 * <li>{@link Vector#add(Vector)}</li>
 * <li>{@link Vector#scale(double)}</li>
 * <li>{@link Vector#dotProduct(Vector)}</li>
 * <li>{@link Vector#crossProduct(Vector)}</li>
 * <li>{@link Vector#lengthSquared()}</li>
 * <li>{@link Vector#length()}</li>
 * <li>{@link Vector#normalize()}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
class VectorTests {
    /**
     * Vector (1,0,0) used in several tests
     */
    private static final Vector V100 = new Vector(1, 0, 0);
    /**
     * Vector (0,1,0) used in several tests
     */
    private static final Vector V010 = new Vector(0, 1, 0);
    /**
     * Vector (1,2,3) used in several tests
     */
    private static final Vector V123 = new Vector(1, 2, 3);
    /**
     * Vector (-1,-2,-3) used in several tests
     */
    private static final Vector V_N1_N2_N3 = new Vector(-1, -2, -3);
    /**
     * Vector (0,3,4) used in several tests
     */
    private static final Vector V034 = new Vector(0, 3, 4);
    /**
     * Delta value for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;
    /**
     * Error message for wrong constructor behavior
     */
    private static final String ERROR_CTOR = "ERROR: Vector constructor ";
    /**
     * Error message for wrong subtract result
     */
    private static final String ERROR_SUBTRACT = "ERROR: Vector subtract() ";
    /**
     * Error message for wrong add result
     */
    private static final String ERROR_ADD = "ERROR: Vector add() ";
    /**
     * Error message for wrong scale result
     */
    private static final String ERROR_SCALE = "ERROR: Vector scale() ";
    /**
     * Error message for wrong dot product result
     */
    private static final String ERROR_DOT = "ERROR: Vector dotProduct() ";
    /**
     * Error message for wrong cross product result
     */
    private static final String ERROR_CROSS = "ERROR: Vector crossProduct() ";
    /**
     * Error message for wrong length squared result
     */
    private static final String ERROR_LENGTH_SQUARED = "ERROR: Vector lengthSquared() ";
    /**
     * Error message for wrong length result
     */
    private static final String ERROR_LENGTH = "ERROR: Vector length() ";
    /**
     * Error message for wrong normalize result
     */
    private static final String ERROR_NORMALIZE = "ERROR: Vector normalize() ";
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    VectorTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Test method for {@link Vector#Vector(double, double, double)}.
     * Verifies that the zero vector cannot be created.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct constructor
        Vector v = new Vector(1, 2, 3);
        assertEquals("(1.0,2.0,3.0)", v.toString(), ERROR_CTOR + "not properly implemented.");

        // =============== Boundary Values Tests ==================

        // BV01: Creating the zero vector must throw an exception
        assertThrows(IllegalArgumentException.class,
                () -> new Vector(0, 0, 0),
                ERROR_CTOR + "does not throw for zero vector");
    }

    /**
     * Test method for {@link Vector#subtract(Point)}.
     * Verifies vector subtraction produces correct results.
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Subtracting two different vectors
        assertEquals(new Vector(1, -1, -1), V123.subtract(V034), ERROR_SUBTRACT + "wrong result");

        // =============== Boundary Values Tests ==================

        // BV01: Subtracting a vector from itself must throw (zero vector result)
        assertThrows(IllegalArgumentException.class,
                () -> V123.subtract(V123),
                ERROR_SUBTRACT + "does not throw for zero result");
    }

    /**
     * Test method for {@link Vector#add(Vector)}.
     * Verifies vector addition produces correct results.
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Adding two vectors
        assertEquals(new Vector(1, 3, 3),
                V123.add(V010),
                ERROR_ADD + "wrong result");

        // =============== Boundary Values Tests ==================

        // BV01: Adding a vector to its negation must throw (zero vector result)
        assertThrows(IllegalArgumentException.class,
                () -> V123.add(V_N1_N2_N3),
                ERROR_ADD + "does not throw for zero result");
    }

    /**
     * Test method for {@link Vector#scale(double)}.
     * Verifies scalar multiplication of a vector.
     */
    @Test
    void testScale() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Scaling a vector by a positive scalar
        assertEquals(new Vector(2, 4, 6),
                V123.scale(2),
                ERROR_SCALE + "wrong result for positive scalar");

        // EP02: Scaling a vector by a negative scalar
        assertEquals(V_N1_N2_N3,
                V123.scale(-1),
                ERROR_SCALE + "wrong result for negative scalar");

        // =============== Boundary Values Tests ==================

        // BV01: Scaling a vector by zero must throw (zero vector result)
        assertThrows(IllegalArgumentException.class,
                () -> V123.scale(0),
                ERROR_SCALE + "does not throw for scale by zero");
    }

    /**
     * Test method for {@link Vector#dotProduct(Vector)}.
     * Verifies the dot product computation.
     */
    @Test
    void testDotProduct() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Dot product of two vectors at an acute angle (positive result)
        assertEquals(14,
                V123.dotProduct(V123),
                DELTA,
                ERROR_DOT + "wrong positive result");

        // EP02: Dot product of two vectors at an obtuse angle (negative result)
        assertEquals(-14,
                V123.dotProduct(V_N1_N2_N3),
                DELTA,
                ERROR_DOT + "wrong negative result");

        // =============== Boundary Values Tests ==================

        // BV01: Dot product of orthogonal vectors must be zero
        assertEquals(0,
                V100.dotProduct(V010),
                DELTA,
                ERROR_DOT + "orthogonal vectors result is not zero");
    }

    /**
     * Test method for {@link Vector#crossProduct(Vector)}.
     * Verifies the cross product computation.
     */
    @Test
    void testCrossProduct() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Cross product of two non-parallel vectors
        Vector result = V100.crossProduct(V034);
        // Ensure the result length equals |v1|*|v2|*sin(angle)
        assertEquals(new Vector(0, -4, 3),
                result,
                ERROR_CROSS + "wrong result");
        // Ensure the result is orthogonal to the first operand
        assertEquals(0,
                result.dotProduct(V100),
                DELTA,
                ERROR_CROSS + "result is not orthogonal to first vector");
        // Ensure the result is orthogonal to the second operand
        assertEquals(0,
                result.dotProduct(V034),
                DELTA,
                ERROR_CROSS + "result is not orthogonal to second vector");

        // =============== Boundary Values Tests ==================
        // BV01: Cross product of parallel vectors (same direction) must throw
        assertThrows(IllegalArgumentException.class,
                () -> V123.crossProduct(new Vector(2, 4, 6)),
                ERROR_CROSS + "does not throw for parallel vectors");

        // BV02: Cross product of antiparallel vectors (opposite direction) must throw
        assertThrows(IllegalArgumentException.class,
                () -> V123.crossProduct(V_N1_N2_N3),
                ERROR_CROSS + "does not throw for anti-parallel vectors");
    }

    /**
     * Test method for {@link Vector#lengthSquared()}.
     * Verifies the squared length computation.
     */
    @Test
    void testLengthSquared() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Squared length of a known vector (0,3,4) → 25
        assertEquals(25,
                V034.lengthSquared(),
                DELTA,
                ERROR_LENGTH_SQUARED + "wrong result");
    }

    /**
     * Test method for {@link Vector#length()}.
     * Verifies the length computation.
     */
    @Test
    void testLength() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Length of a known vector (0,3,4) → 5 (Pythagorean triple)
        assertEquals(5,
                V034.length(),
                DELTA,
                ERROR_LENGTH + "wrong result");
    }

    /**
     * Test method for {@link Vector#normalize()}.
     * Verifies the normalization of a vector.
     */
    @Test
    void testNormalize() {
        Vector n = V034.normalize();

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normalized vector has unit length
        assertEquals(1,
                n.length(),
                DELTA,
                ERROR_NORMALIZE + "normalized vector is not a unit vector");

        // EP02: Normalized vector is parallel to the original (cross product is zero)
        assertThrows(IllegalArgumentException.class,
                () -> V034.crossProduct(n),
                ERROR_NORMALIZE + "normalized vector is not parallel to original");

        // EP03: Normalized vector is in the same direction as the original (dot product positive)
        assertTrue(V034.dotProduct(n) > 0,
                ERROR_NORMALIZE + "normalized vector points in opposite direction");
    }
}