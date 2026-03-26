package geometries.api;

import primitives.Point;
import primitives.Vector;

/** * Abstract class representing a geometric shape in 3D space.
 * This class serves as a base for specific geometric shapes like spheres, planes, etc.
 */
public abstract class Geometry extends Intersectable {

    /** Default constructor for Geometry to satisfy Javadoc tool */
    public Geometry() {}

    /**
     * Returns the normal vector to the surface of the geometry at a given point.
     * @param point The point on the surface of the geometry where the normal vector is to be calculated.
     * @return The normal vector to the surface of the geometry at the given point.
     */
    public abstract Vector getNormal(Point point);
}
