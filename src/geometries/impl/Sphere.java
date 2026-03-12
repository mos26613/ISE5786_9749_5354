package geometries.impl;

import primitives.Point;
import primitives.Vector;

public final class Sphere extends RadialGeometry {
    /**
     * Center point of the sphere
     */
    private final Point center;

    /**
     * Constructs a new Sphere with the given center and radius.
     *
     * @param center The center point of the sphere
     * @param radius The radius of the sphere
     */
    public Sphere(Point center, double radius) {
        super(radius);
        this.center = center;
    }

    @Override
    public String toString() {
        return super.toString() +
                "/nSphere{" +
                "center=" + center +
                '}';
    }

    @Override
    public Vector getNormal(Point point) {
        return null;
    }
}
