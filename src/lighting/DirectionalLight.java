package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Class representing a directional light source in a scene.
 * A directional light is a light source that emits light in a specific direction, and its intensity is the same at all points in the scene.
 */
public class DirectionalLight extends Light implements LightSource {

    /**
     * The direction of the light, represented as a normalized vector.
     */
    private final Vector _direction;

    /**
     * Constructs a DirectionalLight with the specified intensity and direction.
     * @param intensity the intensity of the light as a Color object
     * @param direction the direction of the light as a Vector object. It will be normalized to ensure it has a length of 1.
     */
    public DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        _direction = direction.normalize();
    }


    @Override
    public Vector getL(Point p) {
        return _direction;
    }

    @Override
    public Color getIntensity(Point p) {
        return getIntensity();
    }

    @Override
    public double getDistance(Point point) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getSize() {
        return 0d;
    }

    @Override
    public Point getPosition() {
        return null;
    }

    @Override
    public Vector getSoftShadowAxis(Point point) {
        return _direction;
    }
}
