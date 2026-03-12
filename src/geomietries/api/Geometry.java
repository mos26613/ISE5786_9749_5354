package geomietries.api;

import primitives.Vector;

/** * Abstract class representing a geometric shape in 3D space.
 * This class serves as a base for specific geometric shapes like spheres, planes, etc.
 */
public abstract class Geometry {
    /**
     * Returns the normal vector to the surface of the geometry at a given point.
     *
     * @return the normal vector to the surface of the geometry
     */
    public abstract Vector getNormal();
}
