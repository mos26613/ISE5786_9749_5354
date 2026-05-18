package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Class representing a point light source in a scene.
 * A point light emits light in all directions from a specific position in space.
 */
public class PointLight extends Light implements LightSource {

    /**
     * The position of the point light in space, represented as a Point object.
     */
    protected final Point _position;
    /**
     * The attenuation factors for the point light,
     * which determine how the intensity of the light decreases with distance.
     */
    private double _kC = 1d;
    /**
     * The linear attenuation factor,
     * which determines how the intensity of the light decreases linearly with distance.
     */
    private double _kL = 0d;
    /**
     * The quadratic attenuation factor,
     * which determines how the intensity of the light decreases quadratically with distance.
     */
    private double _kQ = 0d;

    /**
     * Constructs a PointLight with the specified intensity and position.
     * @param intensity the intensity of the light as a Color object
     * @param position the position of the point light as a Point object
     */
    public PointLight(Color intensity, Point position) {
        super(intensity);
        _position = position;
    }

    @Override
    public Vector getL(Point p) {
        return p.subtract(_position).normalize();
    }

    @Override
    public Color getIntensity(Point p) {
        if (_position.equals(p)) {
            return _intensity;
        }

        double dSquared = _position.distanceSquared(p);
        double d = _position.distance(p);
        double attenuation = _kC + _kL * d + _kQ * dSquared;
        return _intensity.scale(1d / attenuation);
    }

    /**
     * Sets the constant attenuation factor for the point light.
     * @param kc the constant attenuation factor to set
     * @return the PointLight object itself, allowing for method chaining
     */
    public PointLight setKC(double kc) {
        _kC = kc;
        return this;
    }
    /**
     * Sets the constant attenuation factor for the point light.
     * @param kl the linear attenuation factor to set
     * @return the PointLight object itself, allowing for method chaining
     */
    public PointLight setKL(double kl) {
        _kL = kl;
        return this;
    }

    /**
     * Sets the constant attenuation factor for the point light.
     * @param kq the quadratic attenuation factor to set
     * @return the PointLight object itself, allowing for method chaining
     */
    public PointLight setKQ(double kq) {
        _kQ = kq;
        return this;
    }
}
