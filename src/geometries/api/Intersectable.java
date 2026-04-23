package geometries.api;

import java.util.List;

import primitives.Point;
import primitives.Ray;

/**
 * Represents an intersectable geometry that can be intersected by rays.
 * This is an abstract class that serves as a base for specific geometric shapes
 * such as spheres, planes, triangles, etc.
 */
public abstract class Intersectable {

    /**
     * Default constructor for Intersectable to satisfy Javadoc tool
     */
    public Intersectable() {
    }

    /**
     * Finds the intersection points between the geometry and a given ray.
     *
     * @param ray The ray that is used to find the intersection points with the geometry.
     * @return A list of points where the ray intersects the geometry.
     * If there are no intersections, returns null.
     */
    public abstract List<Point> findIntersections(Ray ray);
}
