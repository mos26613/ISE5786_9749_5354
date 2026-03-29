package primitives;

import java.util.Objects;

/**
 * Represents a ray in 3D Cartesian space, defined by an origin point and a direction vector.
 * The direction vector is normalized to ensure consistent behavior in geometric computations.
 */
public final class Ray {
    /**
     * The origin point of the ray.
     */
    private final Point _origin;
    /**
     * The direction vector of the ray.
     */
    private final Vector _direction;

    /**
     * Returns the direction vector of the ray.
     *
     * @return the direction vector of the ray
     */
    public Vector direction() {
        return _direction;
    }

    /**
     * Returns the origin point of the ray
     *
     * @return the origin point of the ray
     */
    public Point origin() {
        return _origin;
    }

    /**
     * Constructs a ray with the given origin point and direction vector.
     * The direction vector is normalized to ensure consistent behavior.
     *
     * @param origin    The origin point of the ray.
     * @param direction The direction vector of the ray.
     */
    public Ray(Point origin, Vector direction) {
        this._origin = origin;
        this._direction = direction.normalize();
    }

    /**
     * Calculates a point along the ray at a distance t from the origin.
     * @param t The distance from the origin along the ray.
     * @return The point along the ray at distance t from the origin.
     */
    public Point getPoint(double t) {
        try {
            return _origin.add(_direction.scale(t));
        } catch (IllegalArgumentException e) {
            return _origin;
        }
    }

    @Override
    public String toString() {
        return "Origin: " + _origin +
                "\nDirection: " + _direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ray ray)) return false;
        return Objects.equals(_origin, ray._origin) && Objects.equals(_direction, ray._direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }
}
