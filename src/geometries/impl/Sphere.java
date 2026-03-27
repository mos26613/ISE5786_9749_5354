package geometries.impl;

import java.util.List;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static java.lang.Math.sqrt;
import static primitives.Util.alignZero;

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
        Point p0 =  ray.origin();
        Point O = _center;
        Vector v = ray.direction();

        // Check if the ray starts at the center of the sphere
        // Special check to avoid zero vector
        if (p0.equals(O)) return List.of(O.add(v.scale(_radius)));

        Vector u = O.subtract(p0);
        double tm = alignZero(v.dotProduct(u));
        double d = alignZero(sqrt(u.lengthSquared() - tm * tm));

        // Check if the ray is tangent to the sphere surface or if the ray is outside the sphere
        if (alignZero(d - _radius) >= 0) return null;

        double th = alignZero(sqrt(_radiusSquared - d * d));
        double t1 = alignZero(tm - th);
        double t2 = alignZero(tm + th);

        // No intersections if both t1 and t2 are negative
        if (t1 <= 0 && t2 <= 0) return null;
        // Return both intersections if both are positive
        if (t1 > 0 && t2 > 0) return List.of(p0.add(v.scale(t1)), p0.add(v.scale(t2)));
        // Return t2
        // If there's only one intersection, it must be using t2, because t1 is negative and t2 is positive
        return List.of(p0.add(v.scale(t2)));
    }
}
