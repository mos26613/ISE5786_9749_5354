package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Class representing a spotlight light source in a scene.
 */
public class SpotLight extends PointLight {

    /**
     * The direction of the spotlight, represented as a normalized Vector object.
     */
    private final Vector _direction;

    /**
     * Constructs a SpotLight with the specified intensity, position, and direction.
     * @param intensity the intensity of the light as a Color object
     * @param position the position of the spotlight as a Point object
     * @param direction the direction of the spotlight as a Vector object, which will be normalized
     */
    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        _direction = direction.normalize();
    }

    /**
     * Returns the direction vector from the spotlight to a given point in space.
     * @param kc the constant attenuation factor to set
     * @return the SpotLight object itself for method chaining
     */
    public SpotLight setKc(double kc) {
       return (SpotLight) super.setKc(kc);
    }


    /**
     * Returns the direction vector from the spotlight to a given point in space.
     * @param kl the linear attenuation factor to set
     * @return the SpotLight object itself for method chaining
     */
    public SpotLight setKl(double kl) {
        return (SpotLight) super.setKl(kl);
    }

    /**
     * Returns the direction vector from the spotlight to a given point in space.
     * @param kq the quadratic attenuation factor to set
     * @return the SpotLight object itself for method chaining
     */
    public SpotLight setKq(double kq) {
        return (SpotLight) super.setKq(kq);
    }

    /**
     * Sets the diameter of the spotlight's emitting area for soft shadows.
     * @param size the area-light diameter to set
     * @return the SpotLight object itself for method chaining
     */
    public SpotLight setSize(double size) {
        return (SpotLight) super.setSize(size);
    }

    @Override
    public Vector getSoftShadowAxis(Point p) {
        return _direction;
    }

    @Override
    public Color getIntensity(Point p) {
        Vector l;
        try {
            l = getL(p);
        } catch (IllegalArgumentException e) {
            return _intensity;
        }
        Color baseColor = super.getIntensity(p);
        double dl = _direction.dotProduct(l);
        return baseColor.scale(Math.max(0, dl));
    }
}
