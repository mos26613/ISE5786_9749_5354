package primitives;

import java.util.Objects;

/**
 * Represents a ray in 3D Cartesian space, defined by an origin point and a direction vector.
 * The direction vector is normalized to ensure consistent behavior in geometric computations.
 */
public class Ray {
    /** The origin point of the ray. */
    private Point origon;
    /** The direction vector of the ray. */
    private Vector direction;

    /**
     * Constructs a ray with the given origin point and direction vector.
     * The direction vector is normalized to ensure consistent behavior.
     *
     * @param origon    The origin point of the ray.
     * @param direction The direction vector of the ray.
     */
    public Ray(Point origon, Vector direction) {
        this.origon = origon;
        this.direction = direction.normalize();
    }

    @Override
    public String toString() {
        return "Ray{" +
                "origon=" + origon +
                ", direction=" + direction +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ray ray)) return false;
        return Objects.equals(origon, ray.origon) && Objects.equals(direction, ray.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origon, direction);
    }
}
