package geometries.api;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Abstract class representing a geometric shape in 3D space.
 * This class serves as a base for specific geometric shapes like spheres, planes, etc.
 */
public abstract class Geometry extends Intersectable {

    /**
     * The emission color of the geometry, representing the light emitted by the surface.
     * Defaults to black.
     */
    private Color _emission = Color.BLACK;

    /**
     * Default constructor for Geometry to satisfy Javadoc tool
     */
    public Geometry() {
    }

    /**
     * Returns the emission color of the geometry, which represents the light emitted by the surface.
     * @return the emission color of the geometry
     */
    public Color getEmission() {
        return _emission;
    }

    /**
     * Sets the emission color of the geometry, which represents the light emitted by the surface.
     * @param emission the emission color to set for the geometry
     * @return the geometry instance with the updated emission color, allowing for method chaining
     */
    public Geometry setEmission(Color emission) {
        _emission = emission;
        return this;
    }

    /**
     * Returns the normal vector to the surface of the geometry at a given point.
     *
     * @param point The point on the surface of the geometry where the normal vector is to be calculated.
     * @return The normal vector to the surface of the geometry at the given point.
     */
    public abstract Vector getNormal(Point point);
}
