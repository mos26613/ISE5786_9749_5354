package lighting;

import primitives.Color;

/**
 * Represents ambient light in a scene.
 */
final public class AmbientLight extends Light {

    /**
     * A constant representing the absence of ambient light, with zero intensity (black).
     */
    public static AmbientLight NONE = new AmbientLight(Color.BLACK);

    /**
     * Constructs an AmbientLight with the specified intensity.
     *
     * @param intensity the intensity of the ambient light as a Color object
     */
    public AmbientLight(Color intensity) {
        super(intensity);
    }
}
