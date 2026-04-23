package renderer;

import java.util.List;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import scene.Scene;

/**
 * A simple ray tracer implementation that calculates the color of a point.
 */
class SimpleRayTracer extends RayTracerBase {
    /**
     * Creates a new SimpleRayTracer for the given scene.
     *
     * @param scene The scene to be rendered by this ray tracer.
     */
    SimpleRayTracer(Scene scene) {
        super(scene);
    }

    /**
     * Calculates the color at the given intersection point.
     *
     * @param intersection The point of intersection for which to calculate the color.
     * @return The color at the intersection point.
     */
    private Color calcColor(Point intersection) {
        return _scene.ambientLight.getIntensity();
    }

    /**
     * Traces a ray through the scene and returns the resulting color.
     * This method finds the closest intersection point of the ray with the geometries in the scene and returns the color at that point.
     * If there are no intersections, it returns the background color of the scene.
     *
     * @param ray The ray to be traced through the scene.
     * @return The color resulting from tracing the ray through the scene, which is determined by the color of the closest intersection point,
     * or the background color if there are no intersections.
     */
    @Override
    Color traceRay(Ray ray) {
        List<Point> intersections = _scene.geometries.findIntersections(ray);
        if (intersections == null) return _scene.background;
        return calcColor(ray.findClosestPoint(intersections));
    }
}
