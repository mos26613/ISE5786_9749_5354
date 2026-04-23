package lighting;

import primitives.Color;

/**
 * Represents ambient light in a scene.
 */
final public class AmbientLight {

    /**
     * A constant representing the absence of ambient light, with zero intensity (black).
     */
    public static AmbientLight NONE = new AmbientLight(Color.BLACK);
    /**
     * The intensity of the ambient light, represented as a color.
     */
    private final Color _intensity;

    /**
     * Constructs an AmbientLight with the specified intensity.
     *
     * @param color the intensity of the ambient light as a Color object
     */
    public AmbientLight(Color color) {
        _intensity = color;
    }

    /**
     * Returns the intensity of the ambient light.
     *
     * @return the intensity of the ambient light as a Color object
     */
    public Color getIntensity() {
        return _intensity;
    }
}
