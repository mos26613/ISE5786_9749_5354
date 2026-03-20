package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

/**
 * Represents a cylinder geometry, which is defined by a circular base and a height.
 * The cylinder is a three-dimensional shape that extends the properties of a tube by adding height.
 */
public class Cylinder extends Tube {
    /**
     * Height of the cylinder
     */
    private final double _height;

    /**
     * Constructs a cylinder with the given radius, axis, and height.
     *
     * @param radius Radius of the cylinder's base
     * @param axis   Axis of the cylinder
     * @param height Height of the cylinder
     */
    public Cylinder(double radius, Ray axis, double height) {
        if (height < 0) {
            throw new IllegalArgumentException("Height must be non negative.");
        }
        super(radius, axis);
        this._height = height;
    }

    @Override
    public Vector getNormal(Point point) {
        // check if point is on the axis
        if (point.equals(_axis.origin())) return _axis.direction().scale(-1);

        // project point onto axis
        Vector u = point.subtract(_axis.origin());
        double t = u.dotProduct(_axis.direction());

        // check if point is on the bottom base (t ≈ 0)
        if (Util.isZero(t)) return _axis.direction().scale(-1);

        // check if point is on the top base (t ≈ height)
        if (Util.isZero(t - _height)) return _axis.direction();

        // otherwise — point is on the side surface, delegate to Tube logic
        return super.getNormal(point);
    }
}
