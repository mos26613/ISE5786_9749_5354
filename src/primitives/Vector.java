package primitives;

import static primitives.Util.isZero;

/**
 * Represents a vector in 3D Cartesian space.
 */
public final class Vector extends Point {

    /**
     * Predefined unit vector along the x-axis.
     */
    public static final Vector AXIS_X = new Vector(1, 0, 0);
    /**
     * Predefined unit vector along the y-axis.
     */
    public static final Vector AXIS_Y = new Vector(0, 1, 0);
    /**
     * Predefined unit vector along the z-axis.
     */
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /**
     * Creates a vector from x, y and z coordinates.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @throws IllegalArgumentException if the resulting vector is the zero vector
     *                                  (i.e., all coordinates are zero)
     */
    public Vector(double x, double y, double z) {
        if (isZero(x) && isZero(y) && isZero(z))
            throw new IllegalArgumentException("Zero vector is not allowed");
        super(x, y, z);
    }

    /**
     * Creates a vector from a {@link Double3} coordinates object.
     *
     * @param xyz the coordinates of the vector
     * @throws IllegalArgumentException if the resulting vector is the zero vector
     */
    public Vector(Double3 xyz) {
        if (isZero(xyz._d1()) && isZero(xyz._d2()) && isZero(xyz._d3()))
            throw new IllegalArgumentException("Zero vector is not allowed");
        super(xyz);
    }

    /**
     * Adds this vector to another vector component-wise.
     *
     * @param other the vector to add
     * @return a new vector that is the sum of this and the other vector
     */
    @Override
    public Vector add(Vector other) {
        return new Vector(_xyz.add(other._xyz));
    }

    /**
     * Scales this vector by a scalar factor.
     *
     * @param scalar the scaling factor
     * @return a new vector that is this vector scaled by the given factor
     */
    public Vector scale(double scalar) {
        return new Vector(_xyz.scale(scalar));
    }

    /**
     * Calculates the dot product of this vector with another vector.
     *
     * @param other the vector to calculate the dot product with
     * @return the dot product of this vector and the other vector
     */
    public double dotProduct(Vector other) {
        return _xyz._d1() * other._xyz._d1() + _xyz._d2() * other._xyz._d2() + _xyz._d3() * other._xyz._d3();
    }

    /**
     * Calculates the cross product of this vector with another vector.
     *
     * @param other the vector to calculate the cross product with
     * @return a new vector that is the cross product of this vector and the other vector
     */
    public Vector crossProduct(Vector other) {
        double x = _xyz._d2() * other._xyz._d3() - _xyz._d3() * other._xyz._d2();
        double y = _xyz._d3() * other._xyz._d1() - _xyz._d1() * other._xyz._d3();
        double z = _xyz._d1() * other._xyz._d2() - _xyz._d2() * other._xyz._d1();
        return new Vector(x, y, z);
    }

    /**
     * Calculates the squared length of this vector.
     *
     * @return the squared length of this vector
     */
    public double lengthSquared() {
        return this.distanceSquared(new Point(0, 0, 0));
    }

    /**
     * Calculates the length of this vector.
     *
     * @return the length of this vector
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Normalizes this vector to a unit vector.
     *
     * @return a new vector that is the normalized version of this vector
     */
    public Vector normalize() {
        double len = length();
        return new Vector(_xyz.divide(len));
    }

}
