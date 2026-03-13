package geometries.impl;

import primitives.Ray;

/** * Represents a cylinder geometry, which is defined by a circular base and a height.
 * The cylinder is a three-dimensional shape that extends the properties of a tube by adding height.
 */
public class Cylinder extends Tube {
    /** Height of the cylinder */
    private final double _height;

    /**
     * Constructs a cylinder with the given radius, axis, and height.
     * @param radius Radius of the cylinder's base
     * @param axis Axis of the cylinder
     * @param height Height of the cylinder
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        this._height = height;
    }
}
