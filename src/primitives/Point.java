package primitives;

import java.util.Objects;

/**
 * Represents a point in 3D Cartesian space.
 */
public class Point {
    /**
     * The point coordinates.
     */
    protected final Double3 _xyz;

    /**
     * The zero coordinates constant (0,0,0).
     */
    public static final Double3 ZERO = Double3.ZERO;

    /**
     * Creates a point from a {@link Double3} coordinates object.
     *
     * @param xyz the coordinates of the point
     */
    public Point(Double3 xyz) {
        _xyz = xyz;
    }

    /**
     * Creates a point from x, y and z coordinates.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public Point(double x, double y, double z) {
        this(new Double3(x, y, z));
    }

    /**
     * Subtracts another point from this point.
     *
     * @param other the point to subtract
     * @return the vector from {@code other} to this point
     */
    public Vector subtract(Point other) {
        return new Vector(_xyz.subtract(other._xyz));
    }

    /**
     * Translates this point by a vector.
     *
     * @param vector the translation vector
     * @return a new translated point
     */
    public Point add(Vector vector) {
        return new Point(_xyz.add(vector._xyz));
    }

    /**
     * Calculates the squared Euclidean distance to another point.
     *
     * @param other the other point
     * @return the squared distance between the points
     */
    public double distanceSquared(Point other) {
        double dx = _xyz._d1() - other._xyz._d1();
        double dy = _xyz._d2() - other._xyz._d2();
        double dz = _xyz._d3() - other._xyz._d3();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates the Euclidean distance to another point.
     *
     * @param other the other point
     * @return the distance between the points
     */
    public double distance(Point other) {
        return Math.sqrt(distanceSquared(other));
    }

    @Override
    public String toString() {
        return _xyz.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point point)) return false;
        return Objects.equals(_xyz, point._xyz);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(_xyz);
    }
}
