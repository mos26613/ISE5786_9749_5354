package lighting;

import primitives.Color;

/**
 * Abstract class representing a light source in a scene.
 */
abstract class Light {
    /**
     * The intensity of the light, represented as a color.
     */
    protected final Color _intensity;
    /**
     * Returns the intensity of the light.
     * @return the intensity of the light as a Color object
     */
    public Color getIntensity() {
        return _intensity;
    }
    /**
     * Constructs a Light with the specified intensity.
     * @param intensity the intensity of the light as a Color object
     */
    protected Light(Color intensity) {
        _intensity = intensity;
    }
}
