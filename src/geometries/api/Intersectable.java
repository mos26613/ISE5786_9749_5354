package geometries.api;

import java.util.List;
import java.util.Objects;

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
         * Constructs an Intersection with the specified geometry and point.
         * @param geo  the geometry that is intersected by the ray
         * @param point the point of intersection on the geometry
         */
        public Intersection(Geometry geo, Point point) {
            geometry = geo;
            this.point = point;
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
