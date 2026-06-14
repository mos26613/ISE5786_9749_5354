package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Interface representing a light source in a scene.
 * It defines methods to get the direction of the light and its intensity at a given point.
 */
public interface LightSource {
    /**
     * Returns the direction vector from the light source to the given point.
     * @param p the point to which the direction vector is calculated
     * @return the direction vector from the light source to the point
     * @throws IllegalArgumentException if the point is at the position of the light source
     */
    Vector getL(Point p);

    /**
     * Returns the intensity of the light at the given point.
     * @param p the point at which the intensity is calculated
     * @return the intensity of the light at the point as a Color object
     */
    Color getIntensity(Point p);

    /**
     * Returns the distance from the light source to the given point
     * @param point the point to which the distance is calculated
     * @return the distance from the light source to the point
     */
    double getDistance(Point point);

    /**
     * Returns the diameter of this light's emitting area, used for soft shadows.
     * A value of {@code 0} means a point-sized source that casts a hard shadow.
     * @return the area-light diameter (size); {@code 0} for a hard shadow
     */
    double getSize();

    /**
     * Returns the position of this light source in space, used as the center of
     * the area sampled for soft shadows.
     * @return the light position, or {@code null} for sources that have no
     *         position (e.g. a directional light)
     */
    Point getPosition();

    /**
     * Returns the unit direction that the soft-shadow target area is perpendicular to:
     * the direction toward the shaded point for a point light, or the beam direction
     * for a spot light.
     * @param point the shaded point being tested
     * @return the unit axis the area light's sampling disk is perpendicular to
     */
    Vector getSoftShadowAxis(Point point);
}
