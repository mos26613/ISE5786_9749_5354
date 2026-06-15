package renderer;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.IntStream;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

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
     * The ImageWriter responsible for writing the rendered image to a file.
     */
    private ImageWriter _imageWriter;
    /**
     * The RayTracer responsible for tracing rays through the scene and generating pixel colors.
     */
    private RayTracerBase _rayTracer;
    /**
     * The BeamSampler used to generate a beam of rays through each pixel for effects like antialiasing.
     */
    private BeamSampler _antialiasingBeamSampler;

    /**
     * The number of threads used for rendering the image.
     */
    private int threadsCount = 0;

    /**
     * The number of threads that are not used for rendering the image.
     */
    private static final int SPARE_THREADS = 2;

    /**
     * Debug print interval in seconds for progress percentage
     * If it is zero - there is no progress output
     */
    private double printInterval = 0.0;

    /**
     * The PixelManager responsible for managing pixel rendering in a multi-threaded environment, if applicable.
     */
    private PixelManager pixelManager;

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

    /**
     * This function renders image's pixel color map from the scene
     * included in the ray tracer object
     *
     * @return the camera object itself
     */
    public Camera renderImage() {
        pixelManager = new PixelManager(_nY, _nX, printInterval);
        return switch (threadsCount) {
            case 0 -> renderImageNoThreads();
            case -1 -> renderImageStream();
            default -> renderImageRawThreads();
        };
    }

    /**
     * Render image using multi-threading by parallel streaming
     *
     * @return the camera object itself
     */
    private Camera renderImageStream() {
        IntStream.range(0, _nY).parallel()
                .forEach(i -> IntStream.range(0, _nX).parallel()
                        .forEach(j -> castRay(j, i)));
        return this;
    }

    /**
     * Render image without multi-threading.
     *
     * @return the camera object itself
     */
    public Camera renderImageNoThreads() {
        for (int i = 0; i < _nY; i++)
            for (int j = 0; j < _nX; j++)
                castRay(j, i);
        return this;
    }

    /**
     * Render image using multi-threading by creating and running raw threads
     *
     * @return the camera object itself
     */
    private Camera renderImageRawThreads() {
        var threads = new LinkedList<Thread>();
        while (threadsCount-- > 0)
            threads.add(new Thread(() -> {
                PixelManager.Pixel pixel;
                while ((pixel = pixelManager.nextPixel()) != null)
                    // the supplied PixelManager fills Pixel(col, row) from (rowCounter, colCounter),
                    // so its accessors are inverted relative to castRay(column, row)
                    castRay(pixel.row(), pixel.col());
            }));
        for (var thread : threads) thread.start();
        try {
            for (var thread : threads) thread.join();
        } catch (InterruptedException ignored) {
        }
        return this;
    }

    /**
     * Casts rays through the specified pixel, traces them using the RayTracer, and writes the resulting color to the image.
     *
     * @param xIndex The column index of the pixel (0-based).
     * @param yIndex The row index of the pixel (0-based).
     */
    private void castRay(int xIndex, int yIndex) {
        List<Ray> rays = constructRays(xIndex, yIndex);
        Color color = Color.BLACK;
        for (Ray ray : rays) {
            color = color.add(_rayTracer.traceRay(ray));
        }
        _imageWriter.writePixel(xIndex, yIndex, color.reduce(rays.size()));
        pixelManager.pixelDone();
    }

    /**
     * Draws a grid on the rendered image by coloring pixels at regular intervals with the specified color.
     * The grid lines are drawn at every 'interval' pixel along both the horizontal and vertical directions,
     * and along the borders of the image.
     *
     * @param interval The number of pixels between each grid line. Must be a positive integer.
     * @param color    The color to use for the grid lines.
     * @return The Camera instance after drawing the grid, allowing for method chaining if desired.
     */
    public Camera printGrid(int interval, Color color) {
        for (int i = 0; i < _nY; i++)
            for (int j = 0; j < _nX; j++)
                if (i % interval == 0 || j % interval == 0 || i == _nY - 1 || j == _nX - 1)
                    _imageWriter.writePixel(j, i, color);
        return this;
    }

    /**
     * Constructs a ray from the camera through the center of the specified pixel on the view plane.
     *
     * @param xIndex The column index of the pixel (0-based).
     * @param yIndex The row index of the pixel (0-based).
     * @return A Ray object representing the ray from the camera through the center of the specified pixel.
     */
    public Ray constructRay(int xIndex, int yIndex) {
        Point pIJ = getPixelCenter(xIndex, yIndex);
        return new Ray(_p0, pIJ.subtract(_p0));
    }

    /**
     * Constructs a list of rays from the camera through the specified pixel on the view plane.
     *
     * @param xIndex The column index of the pixel (0-based).
     * @param yIndex The row index of the pixel (0-based).
     * @return A list of Ray objects representing the rays from the camera through the center of the specified pixel,
     * generated by the BeamSampler for effects like antialiasing.
     */
    public List<Ray> constructRays(int xIndex, int yIndex) {
        Point center = getPixelCenter(xIndex, yIndex);
        return _antialiasingBeamSampler.beam(center, _vRight, _vUp, Math.min(_pixelWidth, _pixelHeight),
                _p0, false, null);
    }

    /**
     * Helper method to calculate the center point of a pixel on the view plane based on its indices.
     *
     * @param xIndex The horizontal index of the pixel (0-based).
     * @param yIndex The vertical index of the pixel (0-based).
     * @return The Point representing the center of the specified pixel on the view plane.
     * @throws IllegalArgumentException If the provided pixel indices are out of bounds of the camera's resolution.
     */
    private Point getPixelCenter(int xIndex, int yIndex) {
        if (xIndex < 0 || xIndex >= _nX || yIndex < 0 || yIndex >= _nY) {
            throw new IllegalArgumentException("Pixel indices must be within the resolution bounds");
        }

        double xJ = alignZero((xIndex - (_nX - 1) / 2.0) * _pixelWidth);
        double yI = alignZero(-(yIndex - (_nY - 1) / 2.0) * _pixelHeight);

        Point pIJ = _vpCenter;
        if (!isZero(xJ)) pIJ = pIJ.add(_vRight.scale(xJ));
        if (!isZero(yI)) pIJ = pIJ.add(_vUp.scale(yI));
        return pIJ;
    }


    /**
     * Writes the rendered image to a file with the specified name.
     *
     * @param fileName The name of the output image file, without the extension. The image will be saved as a PNG file.
     */
    public void writeToImage(String fileName) {
        _imageWriter.writeToImage(fileName);
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
         * The pending soft-shadow BeamSampler, applied to the ray tracer in {@link #build()};
         * {@code null} leaves the ray tracer's disabled default (hard shadows).
         */
        private BeamSampler _softShadowSampler;

        /**
         * Satisfy Javadoc tool.
         */
        public Builder() {
        }

        /**
         * Helper method to sum multiple vectors while handling null values.
         * If a vector is null, it is treated as a zero vector and does not contribute to the sum.
         *
         * @param a The first vector
         * @param b The second vector
         * @param c The third vector
         * @return The sum of the non-null vectors among a, b, and c. If all are null, returns null.
         */
        private static Vector sumNonNull(Vector a, Vector b, Vector c) {
            Vector result = null;
            if (a != null) result = a;
            if (b != null) result = (result == null) ? b : result.add(b);
            if (c != null) result = (result == null) ? c : result.add(c);
            return result;
        }

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
         * Sets the RayTracer for the camera based on the specified type.
         *
         * @param scene The scene to be used by the RayTracer for tracing rays and determining pixel colors.
         * @param type  The type of RayTracer to use (e.g., SIMPLE).
         * @return The Builder instance for chaining method calls.
         * @throws IllegalArgumentException If an unsupported RayTracerType is provided.
         */
        public Builder setRayTracer(Scene scene, RayTracerType type) {
            switch (type) {
                case SIMPLE -> _camera._rayTracer = new SimpleRayTracer(scene);

                default -> throw new IllegalArgumentException("Unsupported RayTracerType: " + type);
            }

            return this;
        }

        /**
         * Sets the antialiasing BeamSampler used to spread the primary rays inside a pixel.
         *
         * @param sampler the BeamSampler used to generate the per-pixel ray beam
         * @return The Builder instance for chaining method calls.
         */
        public Builder setAntialiasingBeamSampler(BeamSampler sampler) {
            _camera._antialiasingBeamSampler = sampler;
            return this;
        }

        /**
         * Sets the antialiasing BeamSampler from its configuration.
         *
         * @param edge    The number of samples along one axis of the target area (e.g., 9 yields up to 81 samples);
         *                a value below 2 disables super-sampling (single central ray).
         * @param shape   The shape of the target area for sampling (e.g., SQUARE, CIRCLE).
         * @param pattern The sample distribution pattern (e.g., GRID, JITTERED).
         * @return The Builder instance for chaining method calls.
         */
        public Builder setAntialiasingBeamSampler(int edge, BeamSampler.Shape shape, BeamSampler.Pattern pattern) {
            return setAntialiasingBeamSampler(new BeamSampler(edge, shape, pattern));
        }

        /**
         * Sets the shared BeamSampler used by every light source for soft shadows.
         * The same sampler is reused for all lights; the per-light area diameter is the
         * light's own {@code size}. A disabled (single-sample) sampler keeps shadows hard.
         * It is applied to the ray tracer when the camera is built.
         *
         * @param sampler the BeamSampler used to generate shadow-ray beams
         * @return The Builder instance for chaining method calls.
         */
        public Builder setSoftShadowSampler(BeamSampler sampler) {
            _softShadowSampler = sampler;
            return this;
        }

        /**
         * Sets the shared soft-shadow BeamSampler from its configuration.
         *
         * @param edge    The number of shadow-ray samples along one axis of the light area
         *                (a value below 2 disables soft shadows: a single central shadow ray).
         * @param shape   The shape of the light's sampling area (e.g., SQUARE, CIRCLE).
         * @param pattern The sample distribution pattern (e.g., GRID, JITTERED).
         * @return The Builder instance for chaining method calls.
         */
        public Builder setSoftShadowSampler(int edge, BeamSampler.Shape shape, BeamSampler.Pattern pattern) {
            return setSoftShadowSampler(new BeamSampler(edge, shape, pattern));
        }

        /**
         * Rotates the camera around its viewing direction (vTo) by the given angle.
         * The rotation is clockwise when looking in the direction of vTo.
         * Updates vUp and vRight accordingly; vTo remains unchanged.
         *
         * @param angle the rotation angle in degrees (clockwise)
         * @return this Builder instance for fluent chaining
         */
        public Builder rotate(double angle) {
            checkLocationAndDirection();

            double radians = Math.toRadians(angle);
            double cos = Math.cos(radians);
            double sin = -Math.sin(radians);   // negate for clockwise
            double oneMinusCos = 1 - cos;

            Vector k = _camera._vTo;
            Vector v = _camera._vUp;

            // Rodriguez: v' = v·cos + (k×v)·sin + k·(k·v)·(1-cos)
            Vector term1 = isZero(cos) ? null : v.scale(cos);
            Vector term2 = isZero(sin) ? null : k.crossProduct(v).scale(sin);
            double kDotV = alignZero(k.dotProduct(v));
            Vector term3 = (isZero(oneMinusCos) || isZero(kDotV)) ? null : k.scale(kDotV * oneMinusCos);

            _camera._vUp = sumNonNull(term1, term2, term3);

            return this;
        }

        /**
         * Builds and returns a Camera instance based on the parameters set in the Builder.
         * Validates the parameters and calculates necessary values before returning the Camera.
         * If ray tracing is not set, a SimpleRayTracer will be used.
         *
         * @return A new Camera instance based on the parameters set in the Builder.
         * @throws IllegalArgumentException If any of the parameters are invalid (e.g., non-positive resolution, parallel vectors).
         * @throws MissingResourceException If any required parameters are missing (e.g., camera location, direction).
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();

            if (_camera._rayTracer == null) {
                setRayTracer(new Scene("test"), RayTracerType.SIMPLE);
            }
            if (_camera._antialiasingBeamSampler == null) {
                setAntialiasingBeamSampler(1, BeamSampler.Shape.SQUARE, BeamSampler.Pattern.GRID);
            }
            if (_softShadowSampler == null) {
                setSoftShadowSampler(1, BeamSampler.Shape.SQUARE, BeamSampler.Pattern.GRID);
            }
            _camera._rayTracer.setSoftShadowSampler(_softShadowSampler);

            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException _) {
                return null;
            }
        }

        /**
         * Validates the resolution parameters of the camera, ensuring that both nX and nY are positive integers.
         * If either nX or nY is non-positive, an {@link IllegalArgumentException} is thrown with a descriptive error message.
         * If the resolution parameters are valid, initializes the ImageWriter for the camera with the specified resolution.
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0) {
                throw new IllegalArgumentException("Resolution must be positive");
            }
            _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
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

        /**
         * Sets the number of threads to be used for rendering the image.
         *
         * @param threads The number of threads to use for rendering.
         * @return The Builder instance for chaining method calls.
         */
        public Builder setMultithreading(int threads) {
            if (threads < -2) {
                throw new IllegalArgumentException("Multithreading must be -2 or higher");
            }
            if (threads == -2) {
                int cores = Runtime.getRuntime().availableProcessors() - SPARE_THREADS;
                _camera.threadsCount = cores <= 2 ? 1 : cores;
            } else
                _camera.threadsCount = threads;
            return this;
        }

        /**
         * Set debug printing interval. If it's zero, there won't be printing at all
         *
         * @param interval printing interval in %
         * @return builder object itself
         */
        public Builder setDebugPrint(double interval) {
            if (interval < 0) throw new IllegalArgumentException("interval parameter must be non-negative");
            _camera.printInterval = interval;
            return this;
        }
    }
}
