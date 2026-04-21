package renderer;

import java.util.MissingResourceException;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a camera in a 3D scene, defined by its position, orientation, view plane size, and resolution.
 * The camera can construct rays through pixels on the view plane for rendering purposes.
 */
public class Camera implements Cloneable {
    /**
     * The position of the camera in 3D space.
     */
    private Point _p0;
    /**
     * The up direction vector of the camera, defining the vertical orientation.
     */
    private Vector _vUp;
    /**
     * The direction vector from the camera towards the view plane, defining the forward orientation.
     */
    private Vector _vTo;
    /**
     * The right direction vector of the camera, calculated as the cross product of vTo and vUp, defining the horizontal orientation.
     */
    private Vector _vRight;
    /**
     * The width of the view plane, representing the horizontal size of the area visible through the camera.
     */
    private double _width;
    /**
     * The height of the view plane, representing the vertical size of the area visible through the camera.
     */
    private double _height;
    /**
     * The distance from the camera to the view plane, determining how far the view plane is from the camera's position.
     */
    private double _distance;
    /**
     * The horizontal resolution of the view plane, representing the number of pixels along the width.
     */
    private int _nX = 1;
    /**
     * The vertical resolution of the view plane, representing the number of pixels along the height.
     */
    private int _nY = 1;
    /**
     * The center point of the view plane. Used for determining the position of rays through pixels.
     */
    private Point _vpCenter;
    /**
     * The width of a single pixel on the view plane. Used for determining the position of rays through pixels.
     */
    private double _pixelWidth;
    /**
     * The height of a single pixel on the view plane. Used for determining the position of rays through pixels.
     */
    private double _pixelHeight;

    /**
     * Private constructor to prevent direct instantiation. Use the Builder to create instances of Camera.
     */
    private Camera() {
    }

    /**
     * Returns a new Builder instance for constructing a Camera object with a fluent interface.
     *
     * @return A new Builder instance for constructing a Camera object.
     */
    public static Builder getBuilder() {
        return new Builder();
    }


    public Ray constructRay(int xIndex, int yIndex) {
        return null;
    }

    /**
     * A builder class for constructing Camera instances with a fluent interface.
     */
    public static class Builder {
        /**
         * The Camera instance being built. The Builder modifies this instance and returns a clone of it when build() is called.
         */
        private final Camera _camera = new Camera();

        /**
         * Sets the location of the camera in 3D space.
         *
         * @param p0 The position of the camera in 3D space.
         * @return The Builder instance for chaining method calls.
         */
        public Builder setLocation(Point p0) {
            _camera._p0 = p0;
            return this;
        }

        /**
         * Sets the direction of the camera using the provided to and up vectors.
         *
         * @param to The direction vector from the camera towards the view plane.
         * @param up The up direction vector of the camera, defining the vertical orientation.
         * @return The Builder instance for chaining method calls.
         */
        public Builder setDirection(Vector to, Vector up) {
            _camera._vTo = to;
            _camera._vUp = up;
            return this;
        }

        /**
         * Sets the direction of the camera using the provided 'up' vector and the view plane's center.
         *
         * @param vpCenter The center point of the view plane, used to determine the direction vector from the camera to the view plane.
         * @param up       The up direction vector of the camera, defining the vertical orientation.
         * @return The Builder instance for chaining method calls.
         */
        public Builder setDirection(Point vpCenter, Vector up) {
            _camera._vUp = up;
            _camera._vTo = null;
            _camera._vpCenter = vpCenter;
            return this;
        }

        /**
         * Sets the direction of the camera using the provided to vector and a default up vector (0, 1, 0).
         *
         * @param vpCenter The center point of the view plane, used to determine the direction vector from the camera to the view plane.
         * @return The Builder instance for chaining method calls.
         */
        public Builder setDirection(Point vpCenter) {
            _camera._vUp = Vector.AXIS_Y;
            _camera._vTo = null;
            _camera._vpCenter = vpCenter;
            return this;
        }

        /**
         * Sets the size of the view plane, defining the horizontal and vertical dimensions of the area visible through the camera.
         *
         * @param width  The width of the view plane, representing the horizontal size of the area visible through the camera.
         * @param height The height of the view plane, representing the vertical size of the area visible through the camera.
         * @return The Builder instance for chaining method calls.
         */
        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;
            return this;
        }

        /**
         * Sets the distance from the camera to the view plane, determining how far the view plane is from the camera's position.
         *
         * @param distance The distance from the camera to the view plane.
         * @return The Builder instance for chaining method calls.
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Sets the resolution of the view plane, defining the number of pixels along the width and height.
         *
         * @param nX The horizontal resolution of the view plane, representing the number of pixels along the width.
         * @param nY The vertical resolution of the view plane, representing the number of pixels along the height.
         * @return The Builder instance for chaining method calls.
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Builds and returns a Camera instance based on the parameters set in the Builder.
         * Validates the parameters and calculates necessary values before returning the Camera.
         *
         * @return A new Camera instance based on the parameters set in the Builder.
         * @throws IllegalArgumentException If any of the parameters are invalid (e.g., non-positive resolution, parallel vectors).
         * @throws MissingResourceException If any required parameters are missing (e.g., camera location, direction).
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();

            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException _) {
                return null;
            }
        }

        /**
         * Validates the resolution parameters of the camera, ensuring that both nX and nY are positive integers.
         * If either nX or nY is non-positive, an IllegalArgumentException is thrown with a descriptive error message.
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0) {
                throw new IllegalArgumentException("Resolution must be positive");
            }
        }

        /**
         * If either the up vector or the camera's location isn't initialized, a {@link MissingResourceException} will be thrown.
         * If both the vTo vector and the view plane center aren't initialized, a {@link MissingResourceException} will be thrown.
         * If vTo isn't initialized but vpCenter is, initialize vTo according to the camera's location and the view plane's center.
         * Calculates vRight.
         * If vTo and vUp are parallel, an {@link IllegalArgumentException} will be thrown.
         * If vTo and vUp and NOT orthogonal, vUp will be recalculated to be orthogonal to vTo and vRight.
         * Normalizes all vectors.
         */
        private void checkLocationAndDirection() {
            if (_camera._p0 == null)
                throw new MissingResourceException("Camera location is not set", Camera.class.getName(), "_p0");
            if (_camera._vUp == null)
                throw new MissingResourceException("Camera vUp vector is not set", Camera.class.getName(), "_vUp");
            if (_camera._vTo == null && _camera._vpCenter == null)
                throw new MissingResourceException("Camera vTo and vpCenter are not set",
                        Camera.class.getName(), "vTo or vpCenter");

            if (_camera._vTo == null) _camera._vTo = _camera._vpCenter.subtract(_camera._p0).normalize();
            else _camera._vTo = _camera._vTo.normalize();

            try {
                _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();
            } catch (IllegalArgumentException _) {
                throw new IllegalArgumentException("vUp and vTo cannot be parallel");
            }

            if (!isZero(_camera._vUp.dotProduct(_camera._vTo))) {
                _camera._vUp = _camera._vRight.crossProduct(_camera._vTo).normalize();
            } else {
                _camera._vUp = _camera._vUp.normalize();
            }
        }

        /**
         * Validates the view plane parameters of the camera, ensuring that the width, height, and distance are positive values.
         * If any of these parameters are non-positive, an {@link IllegalArgumentException} is thrown with a descriptive error message.
         * If the parameters are valid, calculates the pixel width and height, as well as the center point of the view plane,
         * which are necessary for constructing rays through pixels.
         */
        private void checkViewPlane() {
            if (alignZero(_camera._width) <= 0 || alignZero(_camera._height) <= 0) {
                throw new IllegalArgumentException("View plane size must be positive");
            }

            if (alignZero(_camera._distance) <= 0) {
                throw new IllegalArgumentException("View plane distance must be positive");
            }

            _camera._pixelWidth = _camera._width / _camera._nX;
            _camera._pixelHeight = _camera._height / _camera._nY;

            _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));
        }
    }
}
