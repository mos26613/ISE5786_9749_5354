package lighting;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionalLightTests {

    @Test
    void testGetL() {
        Vector X = Vector.AXIS_X;

        // EP01 - regular case
        DirectionalLight dl = new DirectionalLight(Color.BLACK, X);
        assertEquals(X, dl.getL(Point.ZERO), "ERROR in getL!");
    }

    @Test
    void testGetIntensity() {
        Color C = Color.BLACK;

        // EP01 - regular case
        DirectionalLight dl = new DirectionalLight(C, Vector.AXIS_X);
        assertEquals(C, dl.getIntensity(Point.ZERO), "ERROR in getIntensity!");
    }
}