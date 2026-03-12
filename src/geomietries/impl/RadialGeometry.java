package geomietries.impl;

import java.util.Objects;

import geomietries.api.Geometry;

/** * Abstract class representing a geometric shape that has a radius, such as a sphere or a cylinder.
 * This class extends the {@link Geometry} class and adds properties related to the radius of the shape.
 */
public abstract class RadialGeometry extends Geometry {
    /** Radius of the shape */
    private final double radius;
    /** Square of the radius for faster calculations */
    private final double radiusSquared;

    /**
     * Constructs a new RadialGeometry with the given radius and its square.
     * @param radius The radius of the shape
     * @param radiusSquared The square of the radius for faster calculations
     */
    public RadialGeometry(double radius, double radiusSquared) {
        this.radius = radius;
        this.radiusSquared = radiusSquared;
    }

    @Override
    public String toString() {
        return "RadialGeometry{" +
                "radius=" + radius +
                ", radiusSquared=" + radiusSquared +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RadialGeometry that)) return false;
        return Double.compare(radius, that.radius) == 0 && Double.compare(radiusSquared, that.radiusSquared) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(radius, radiusSquared);
    }
}
