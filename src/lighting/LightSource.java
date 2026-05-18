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
}
