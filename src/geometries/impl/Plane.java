package geometries.impl;

import java.util.Objects;

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
    private final Point point;
    /**
     * Normal vector to the plane
     */
    private final Vector normal;

    /**
     * Constructs a plane given three points on the plane.
     * @param point1 3D point on the plane
     * @param point2 3D point on the plane
     * @param point3 3D point on the plane
     */
    Plane(Point point1, Point point2, Point point3) {
        this.point = point1;
        normal = null;
    } // TODO: Implement plane construction from three points

    /**
     * Constructs a plane given a point on the plane and a normal vector.
     * @param point 3D point on the plane
     * @param normal Normal vector to the plane
     */
    Plane(Point point, Vector normal) {
        this.point = point;
        this.normal = normal.normalize();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plane plane)) return false;
        return Objects.equals(point, plane.point) && Objects.equals(normal, plane.normal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, normal);
    }

    @Override
    public String toString() {
        return "Plane{" +
                "point=" + point +
                ", normal=" + normal +
                '}';
    }

    @Override
    public Vector getNormal(Point point) {
        return normal;
    }
}
