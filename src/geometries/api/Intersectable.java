package geometries.api;

import java.util.List;
import java.util.Objects;

import lighting.LightSource;
import primitives.AABB;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents an intersectable geometry that can be intersected by rays.
 * This is an abstract class that serves as a base for specific geometric shapes
 * such as spheres, planes, triangles, etc.
 */
public abstract class Intersectable {

    /**
     * Global switch (CBR — Conservative Bounding Region) for the bounding-box
     * early-reject test. When {@code false} (the default) intersection behaves exactly as
     * before and no bounding boxes are ever built. Toggled from tests / via the Camera Builder.
     */
    private static boolean s_cbrEnabled = false;

    /**
     * Lazily-built bounding box of this intersectable, or {@code null} for an unbounded
     * (infinite) geometry. Built on first use only, never in the constructor.
     */
    private AABB _boundingBox;
    /**
     * Whether {@link #_boundingBox} has already been computed.
     */
    private boolean _boxComputed = false;

    /**
     * Default constructor for Intersectable to satisfy Javadoc tool
     */
    public Intersectable() {
    }

    /**
     * Enables or disables the CBR bounding-box early-reject test globally for all
     * intersectables. Disabled by default, so existing renders are unaffected.
     *
     * @param enabled {@code true} to early-reject rays against bounding boxes,
     *                {@code false} to intersect every geometry directly
     */
    public static void setCBR(boolean enabled) {
        s_cbrEnabled = enabled;
    }

    /**
     * Computes the conservative axis-aligned bounding box of this intersectable.
     * The default returns {@code null}, meaning "unbounded" (e.g. an infinite geometry);
     * finite geometries override this to return a real box. This is the box-creation half
     * of the CBR Template Method and is never called from a constructor.
     *
     * @return a box fully containing this geometry, or {@code null} if it cannot be bounded
     */
    protected AABB createBoundingBox() {
        return null;
    }

    /**
     * Returns this intersectable's bounding box, building it once on first request and
     * caching the result. Returns {@code null} for an unbounded geometry.
     *
     * @return the cached bounding box, or {@code null} if the geometry is unbounded
     */
    public final AABB getBoundingBox() {
        if (!_boxComputed) {
            _boundingBox = createBoundingBox();
            _boxComputed = true;
        }
        return _boundingBox;
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
     * <p>
     * When CBR is enabled, the ray is first tested against this geometry's bounding box;
     * if it misses the box it cannot hit the geometry, so the expensive helper is skipped.
     * This is the box-checking half of the CBR Template Method and covers every ray kind
     * (camera, shadow, reflection, refraction) since they all go through this method.
     *
     * @param ray The ray that is used to find the intersection points with the geometry.
     * @return A list of Intersection objects, where each Intersection contains the geometry and the point of intersection.
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        if (s_cbrEnabled) {
            AABB box = getBoundingBox();
            if (box != null && !box.intersects(ray)) return null;
        }
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
         * The normal vector to the surface of the geometry at the point of intersection.
         */
        public Vector normal;
        /**
         * The vector from the view point to the intersection point, used for lighting calculations.
         */
        public Vector v;
        /**
         * The angle between the normal vector and the view vector, used for lighting calculations.
         */
        public double vNormal;
        /**
         * The light source that is illuminated by the geometry at the point of intersection.
         */
        public LightSource light;
        /**
         * The vector from the ight source to the intersection point used for lighting calculations.
         */
        public Vector l;
        /**
         * The angle between the normal vector and the light vector, used for lighting calculations.
         */
        public double lNormal;

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
