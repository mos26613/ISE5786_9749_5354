package geometries.api;

import java.util.List;
import java.util.Objects;

import primitives.Material;
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
     * Abstract helper method to calculate the intersection points between the geometry and a given ray.
     *
     * @param ray The ray that is used to find the intersection points with the geometry.
     * @return A list of Intersection objects, where each Intersection contains the geometry and the point of intersection.
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray);

    /**
     * Calculates the intersection points between the geometry and a given ray.
     *
     * @param ray The ray that is used to find the intersection points with the geometry.
     * @return A list of Intersection objects, where each Intersection contains the geometry and the point of intersection.
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersectionsHelper(ray);
    }

    /**
     * Finds the intersection points between the geometry and a given ray.
     *
     * @param ray The ray that is used to find the intersection points with the geometry.
     * @return A list of points where the ray intersects the geometry.
     * If there are no intersections, returns null.
     */
    public final List<Point> findIntersections(Ray ray) {
        var intersections = calcIntersections(ray);
        return intersections == null ? null :
                intersections.stream()
                .map(intersection -> intersection.point)
                .toList();
    }

    /**
     * Represents a pair of a geometry and a point of intersection on that geometry.
     */
    public static final class Intersection {
        /**
         * The geometry that is intersected by the ray.
         */
        public final Geometry geometry;
        /**
         * The point of intersection on the geometry.
         */
        public final Point point;
        /**
         * The material properties of the geometry at the point of intersection.
         */
        public final Material material;

        /**
         * Constructs an Intersection object with the specified geometry and point of intersection.
         *
         * @param geometry The geometry that is intersected by the ray. If null, a default Material will be used.
         * @param point    The point of intersection on the geometry.
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
            material = geometry == null ? new Material() : geometry.getMaterial();
        }

        @Override
        public String toString() {
            return "Intersection{" +
                    "geometry=" + geometry +
                    ", point=" + point +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Intersection intersection)) return false;
            return geometry == intersection.geometry && point.equals(intersection.point);
        }

        @Override
        public int hashCode() {
            return Objects.hash(geometry, point);
        }
    }
}
