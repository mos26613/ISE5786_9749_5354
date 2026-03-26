package geometries.impl;

import java.util.List;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents a sphere geometry.
 */
public final class Sphere extends RadialGeometry {
    /**
     * Center point of the sphere
     */
    private final Point _center;

    /**
     * Constructs a new Sphere with the given center and radius.
     *
     * @param center The center point of the sphere
     * @param radius The radius of the sphere
     */
    public Sphere(Point center, double radius) {
        super(radius);
        this._center = center;
    }

    @Override
    public String toString() {
        return super.toString() +
                "/nSphere{" +
                "center=" + _center +
                '}';
    }

    @Override
    public Vector getNormal(Point point) {
        return point.subtract(_center).normalize();
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        return null; // TODO: Implement the intersection logic for the sphere
    }
}
