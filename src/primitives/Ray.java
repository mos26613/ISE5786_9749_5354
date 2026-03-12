package primitives;

import java.util.Objects;

/**
 * Represents a ray in 3D Cartesian space, defined by an origin point and a direction vector.
 * The direction vector is normalized to ensure consistent behavior in geometric computations.
 */
public final class Ray {
    /** The origin point of the ray. */
    private final Point origin;
    /** The direction vector of the ray. */
    private final Vector direction;

    /**
     * Constructs a ray with the given origin point and direction vector.
     * The direction vector is normalized to ensure consistent behavior.
     *
     * @param origin    The origin point of the ray.
     * @param direction The direction vector of the ray.
     */
    public Ray(Point origin, Vector direction) {
        this.origin = origin;
        this.direction = direction.normalize();
    }

    @Override
    public String toString() {
        return "Ray{" +
                "origin=" + origin +
                ", direction=" + direction +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ray ray)) return false;
        return Objects.equals(origin, ray.origin) && Objects.equals(direction, ray.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, direction);
    }
}
