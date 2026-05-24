package renderer;

import java.util.List;

import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static geometries.api.Intersectable.Intersection;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static primitives.Util.alignZero;

/**
 * A simple ray tracer implementation that calculates the color of a point.
 */
class SimpleRayTracer extends RayTracerBase {

    /**
     * A small constant used to offset the intersection point when checking for shadows, to avoid self-shadowing issues.
     */
    private static final double DELTA = 0.1;

    /**
     * Creates a new SimpleRayTracer for the given scene.
     *
     * @param scene The scene to be rendered by this ray tracer.
     */
    SimpleRayTracer(Scene scene) {
        super(scene);
    }

    /**
     * Checks if the given intersection point is unshaded with respect to the light source,
     * meaning that there are no geometries blocking the light from reaching the point.
     * @param intersection The intersection point to check for shadows
     * @return true if the point is unshaded (not in shadow), false if it is shaded (in shadow)
     */
    private boolean unshaded(Intersection intersection) {
        Vector pointToLight = intersection.l.scale(-1);
        Vector delta = intersection.normal.scale(intersection.lNormal < 0 ? DELTA : -DELTA);
        Ray shadowRay = new Ray(intersection.point.add(delta), pointToLight);

        var shadowIntersections = _scene.geometries.findIntersections(shadowRay);
        if (shadowIntersections == null) return true;
        else {
            double lightDistance = intersection.light.getDistance(intersection.point);
            for (var shadowIntersection : shadowIntersections) {
                if (alignZero(intersection.point.distance(shadowIntersection) - lightDistance) < 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Calculates the color at the given intersection point.
     *
     * @param intersection The point of intersection for which to calculate the color.
     * @param v            The direction vector of the ray that caused the intersection, used for shading calculations.
     * @return The color at the intersection point.
     */
    private Color calcColor(Intersection intersection, Vector v) {
        return !preprocessIntersection(intersection, v) ? Color.BLACK
                : _scene.ambientLight.getIntensity()
                  .scale(intersection.material.kA)
                  .add(calcLocalEffects(intersection));
    }

    /**
     * Calculates the local lighting effects at the given intersection point.
     * This method iterates through all light sources in the scene and calculates the contribution of each light source to the color at the intersection point,
     * taking into account the diffuse and specular components of the lighting.
     *
     * @param intersection The point of intersection for which to calculate the local lighting effects.
     * @return The color contribution from all light sources at the intersection point, which is a combination of diffuse and specular components.
     */
    private Color calcLocalEffects(Intersection intersection) {
        Color color = intersection.geometry.getEmission();
        for (LightSource lightSource : _scene.lights) {
            if (preprocessLightSource(intersection, lightSource)) {
                if (unshaded(intersection)) {
                color = color.add(
                        lightSource.getIntensity(intersection.point)
                                .scale(calcDiffuse(intersection)
                                        .add(calcSpecular(intersection))));
                }
            }
        }
        return color;
    }

    /**
     * Calculates the diffuse component of the lighting at the given intersection point.
     * The diffuse component is calculated based on the material's diffuse coefficient (kD) and the angle between the light source and the surface normal.
     *
     * @param intersection The point of intersection for which to calculate the diffuse component.
     * @return The diffuse color contribution at the intersection point, which is a scaled version of the material's diffuse coefficient based on the angle of incidence.
     */
    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD
                .scale(abs(intersection.lNormal));
    }

    /**
     * Calculates the specular component of the lighting at the given intersection point.
     *
     * @param intersection The point of intersection for which to calculate the specular component.
     * @return The specular color contribution at the intersection point,
     * which is a scaled version of the material's specular coefficient based on the angle of reflection and the viewer's direction.
     */
    private Double3 calcSpecular(Intersection intersection) {
        Vector v = intersection.normal.scale(2 * intersection.lNormal);
        Vector r = intersection.l
                .subtract(v)
                .normalize();
        double vR = intersection.v.dotProduct(r);
        return intersection.material.kS
                .scale(pow(max(0, -vR), intersection.material.nShininess));
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
        List<Intersection> intersections = _scene.geometries.calcIntersections(ray);
        if (intersections == null) return _scene.background;
        return calcColor(ray.findClosestIntersection(intersections), ray.direction());
    }
}
