package lighting;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the PointLight class, which represents a point light source in a scene.
 */
class PointLightTests {

    /** Satisfy Javadoc tool */
    PointLightTests() {}

    /**
     * Tests the getL method of the PointLight class.
     */
    @Test
    void testGetL() {
        PointLight PL = new PointLight(Color.BLACK, Point.ZERO);

        // EP01 - regular case
        assertEquals(Vector.AXIS_X, PL.getL(new Point(1, 0, 0)), "ERROR in getL!");

        //BV01 - point is at the position of the light
        assertThrows(IllegalArgumentException.class, () -> PL.getL(Point.ZERO), "ERROR in getL - point is at the position of the light!");
    }

    /**
     * Tests the getIntensity method of the PointLight class.
     */
    @Test
    void testGetIntensity() {
        Color C = new Color(140, 140, 140);
        PointLight PL = new PointLight(C, Point.ZERO)
                .setKc(2)
                .setKl(2)
                .setKq(2);

        //EP01 - regular case
        assertEquals(new Color(10, 10, 10), PL.getIntensity(new Point(2, 0, 0)), "ERROR in getIntensity!");

        //BV01 - point is at the position of the light
        assertEquals(C, PL.getIntensity(Point.ZERO), "ERROR in getIntensity - point is at the position of the light!");
    }
}