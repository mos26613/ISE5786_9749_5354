package renderer;

import lighting.LightSource;
import primitives.Color;
import primitives.Ray;
import primitives.Vector;
import sampling.BeamSampler;
import scene.Scene;

import static geometries.api.Intersectable.Intersection;
import static primitives.Util.alignZero;

/**
 * Abstract base class for ray tracers. This class defines the common interface and properties for all ray tracer implementations.
 * It holds a reference to the scene being rendered and provides a method for tracing rays through the scene.
 */
abstract class RayTracerBase {
    /**
     * Reference to the scene being rendered
     */
    protected Scene _scene;

    /**
     * The shared BeamSampler used to generate the beam of shadow rays toward an
     * area light for soft shadows. The ray tracer is its sole owner; the
     * {@link Camera.Builder} configures it. The default is a disabled (single-sample)
     * sampler, so a ray tracer that is never configured still casts hard shadows.
     */
    protected BeamSampler _softShadowSampler = new BeamSampler(1, BeamSampler.Shape.SQUARE, BeamSampler.Pattern.GRID);

    /**
     * Default constructor for RayTracerBase to satisfy Javadoc tool
     */
    RayTracerBase() {
    }

    /**
     * Constructor for RayTracerBase that initializes the scene reference
     *
     * @param scene The scene to be rendered by the ray tracer.
     *
     */
    RayTracerBase(Scene scene) {
        _scene = scene;
    }

    /**
     * Traces a ray through the scene and returns the resulting color. This method must be implemented by subclasses to provide specific ray tracing behavior.
     *
     * @param ray The ray to be traced through the scene.
     * @return The color resulting from tracing the ray through the scene.
     */
    abstract Color traceRay(Ray ray);

    /**
     * Injects the shared soft-shadow BeamSampler owned by the camera.
     *
     * @param softShadowSampler the BeamSampler used to generate shadow-ray beams
     */
    void setSoftShadowSampler(BeamSampler softShadowSampler) {
        _softShadowSampler = softShadowSampler;
    }

    /**
     * Preprocesses the intersection by calculating the normal vector at the intersection point and the dot product of the ray direction and the normal vector.
     * This method is used to prepare the intersection data for shading calculations.
     *
     * @param intersection The intersection to be preprocessed, which contains information about the geometry and the point of intersection.
     * @param v            The direction vector of the ray that caused the intersection.
     * @return true if the preprocessing was successful and the ray is not parallel to the surface (i.e., vNormal is not zero), false otherwise.
     */
    protected boolean preprocessIntersection(Intersection intersection, Vector v) {
        intersection.v = v;
        intersection.normal = intersection.geometry.getNormal(intersection.point);
        intersection.vNormal = alignZero(intersection.v.dotProduct(intersection.normal));
        return intersection.vNormal != 0;
    }

    /**
     * Preprocesses the light source by calculating the direction vector from the intersection point to the light source and the dot product of this direction vector and the normal vector at the intersection point.
     * This method is used to prepare the intersection data for shading calculations involving the light source.
     *
     * @param intersection The intersection to be preprocessed, which contains information about the geometry and the point of intersection.
     * @param light        The light source that is being considered for shading calculations.
     * @return true if the preprocessing was successful and the light is not parallel to the surface (i.e., lNormal is not zero) and is on the same side as the ray (i.e., lNormal * vNormal > 0), false otherwise.
     */
    protected boolean preprocessLightSource(Intersection intersection, LightSource light) {
        intersection.light = light;
        intersection.l = light.getL(intersection.point);
        intersection.lNormal = alignZero(intersection.l.dotProduct(intersection.normal));
        return intersection.lNormal * intersection.vNormal > 0;
    }
}
