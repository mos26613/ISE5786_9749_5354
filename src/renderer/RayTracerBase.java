package renderer;

import primitives.Color;
import primitives.Ray;
import scene.Scene;

/**
 * Abstract base class for ray tracers. This class defines the common interface and properties for all ray tracer implementations.
 * It holds a reference to the scene being rendered and provides a method for tracing rays through the scene.
 */
abstract class RayTracerBase {
    /** Default constructor for RayTracerBase to satisfy Javadoc tool */
    RayTracerBase() {}
    /** Reference to the scene being rendered */
    protected Scene _scene;
    /** Constructor for RayTracerBase */
    RayTracerBase(Scene scene) {_scene = scene;}
    /** Traces a ray through the scene and returns the resulting color. This method must be implemented by subclasses to provide specific ray tracing behavior.
     * @param ray The ray to be traced through the scene.
     * @return The color resulting from tracing the ray through the scene.
     */
    abstract Color traceRay(Ray ray);
}
