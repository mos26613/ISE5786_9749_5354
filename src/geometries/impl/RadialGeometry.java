package geometries.impl;

import geometries.api.Geometry;

/**
 * Abstract class representing a geometric shape that has a radius, such as a sphere or a cylinder.
 * This class extends the {@link Geometry} class and adds properties related to the radius of the shape.
 */
public abstract class RadialGeometry extends Geometry {
    /**
     * Radius of the shape
     */
    protected final double _radius;
    /**
     * Square of the radius for faster calculations
     */
    protected final double _radiusSquared;

    /**
     * Constructs a new RadialGeometry with the given radius.
     *
     * @param radius The radius of the shape
     */
    public RadialGeometry(double radius) {
        this._radius = radius;
        this._radiusSquared = radius * radius;
    }

    @Override
    public String toString() {
        return "RadialGeometry{" +
                "radius=" + _radius +
                ", radiusSquared=" + _radiusSquared +
                '}';
    }

}
