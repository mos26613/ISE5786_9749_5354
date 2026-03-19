package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Vector;

/** * Represents a plane in 3D space defined by a point and a normal vector.
 * The plane can also be defined by three non-collinear points.
 */
public final class Plane extends Geometry {
    /**
     * 3D point on the plane
     */
    private final Point _point;
    /**
     * Normal vector to the plane
     */
    private final Vector _normal;

    /**
     * Constructs a plane given three points on the plane.
     * @param point1 3D point on the plane
     * @param point2 3D point on the plane
     * @param point3 3D point on the plane
     */
    public Plane(Point point1, Point point2, Point point3) {
        this._point = point1;

        Vector v1 = point2.subtract(point1);
        Vector v2 = point3.subtract(point1);
        this._normal = v1.crossProduct(v2).normalize();
    }
    /**
     * Constructs a plane given a point on the plane and a normal vector.
     * @param _point 3D point on the plane
     * @param normal Normal vector to the plane
     */
    public Plane(Point _point, Vector normal) {
        this._point = _point;
        this._normal = normal.normalize();
    }

    @Override
    public String toString() {
        return "Point: " + _point +
                "\nNormal: " + _normal;
    }

    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }
}
