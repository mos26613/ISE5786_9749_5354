package lighting;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the SpotLight class, which represents a spotlight light source in a scene.
 */
class SpotLightTests {

    /** Satisfy Javadoc tool */
    SpotLightTests() {}

    /**
     * Tests the getL method of the SpotLight class.
     */
    @Test
    void testGetL() {
        SpotLight SL = new SpotLight(Color.BLACK, Point.ZERO, Vector.AXIS_X);

        //EP01 - regular case
        assertEquals(Vector.AXIS_X, SL.getL(new Point(1, 0, 0)), "ERROR in getL!");

        //BV01 - point is at the position of the light
        assertThrows(IllegalArgumentException.class, () -> SL.getL(Point.ZERO), "ERROR in getL - point is at the position of the light!");
    }

    /**
     * Tests the getIntensity method of the SpotLight class.
     */
    @Test
    void testGetIntensity() {
        SpotLight SL = new SpotLight(new Color(140, 140, 140), Point.ZERO, Vector.AXIS_X)
                .setKC(2d)
                .setKL(2d)
                .setKQ(2d);

        // === Equivalence Partitions Tests ===

        //EP01 - Point in front of spotlight
        assertEquals(new Color(10, 10, 10), SL.getIntensity(new Point(2, 0, 0)), "ERROR in getIntensity!");

        //EP02 - Point is behind the spotlight
        assertEquals(Color.BLACK, SL.getIntensity(new Point(-1, 0, 0)), "ERROR in getIntensity - point is behind the spotlight!");

        // === Boundary Values Tests ===

        //BV01 - Point is at the position of the light
        assertEquals(new Color(140, 140, 140), SL.getIntensity(Point.ZERO), "ERROR in getIntensity - point is at the position of the light!");

        //BV02 - Point is at the edge of the spotlight's cone (90 degrees)
        assertEquals(Color.BLACK, SL.getIntensity(new Point(0, 1, 0)), "ERROR in getIntensity - point is at the edge of the spotlight's cone!");
    }
}