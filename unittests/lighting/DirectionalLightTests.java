package lighting;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the DirectionalLight class, which represents a directional light source in a scene.
 */
class DirectionalLightTests {

    /** Satisfy Javadoc tool */
    DirectionalLightTests() {}

    /**
     * Tests the getL method of the DirectionalLight class.
     */
    @Test
    void testGetL() {
        Vector X = Vector.AXIS_X;

        // EP01 - regular case
        DirectionalLight dl = new DirectionalLight(Color.BLACK, X);
        assertEquals(X, dl.getL(Point.ZERO), "ERROR in getL!");
    }

    /**
     * Tests the getIntensity method of the DirectionalLight class.
     */
    @Test
    void testGetIntensity() {
        Color C = Color.BLACK;

        // EP01 - regular case
        DirectionalLight dl = new DirectionalLight(C, Vector.AXIS_X);
        assertEquals(C, dl.getIntensity(Point.ZERO), "ERROR in getIntensity!");
    }

    /**
     * Tests that a directional light reports no emitting area, so it always casts a
     * hard shadow ({@link DirectionalLight#getSize()} is 0).
     */
    @Test
    void testGetSize() {
        DirectionalLight dl = new DirectionalLight(Color.BLACK, Vector.AXIS_X);

        // ============ Equivalence Partitions Tests ==============

        // EP01 - a directional light has no area (size 0 -> hard shadow)
        assertEquals(0, dl.getSize(), 1e-6, "ERROR: a directional light must report size 0");
    }
}