package renderer;

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
     * The maximum recursion level for calculating color, which limits the depth of recursive calls when calculating reflections and refractions.
     */
    private static final int MAX_CALC_COLOR_LEVEL = 10;
    /**
     * The minimum threshold for the color contribution (k) when calculating color,
     * which helps to optimize the ray tracing process by ignoring contributions that are too small to affect the final color significantly.
     */
    private static final double MIN_CALC_COLOR_K = 0.001;
    /**
     * The initial value of the color contribution (k) when starting the color calculation process.
     */
    private static final Double3 INITIAL_K = Double3.ONE;

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
     *
     * @param intersection The intersection point to check for shadows
     * @return true if the point is unshaded (not in shadow), false if it is shaded (in shadow)
     */
    private boolean unshaded(Intersection intersection) {
        Vector pointToLight = intersection.l.scale(-1);
        Ray shadowRay = new Ray(intersection.point, pointToLight, intersection.normal);

        var shadowIntersections = _scene.geometries.calcIntersections(shadowRay);
        if (shadowIntersections == null) return true;
        else {
            double lightDistance = intersection.light.getDistance(intersection.point);
            for (var shadowIntersection : shadowIntersections) {
                if (alignZero(intersection.point.distance(shadowIntersection.point) - lightDistance) < 0 &&
                        (shadowIntersection.material.kT.isLowerThan(MIN_CALC_COLOR_K))) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Calculates the transparency factor (ktr) for the given intersection point, which represents how much light can pass through the geometries between the light source and the intersection point.
     *
     * @param intersection The intersection point for which to calculate the transparency factor.
     * @return The transparency factor (ktr) for the given intersection point, which is a product of the transparency coefficients (kT) of all geometries that intersect with the shadow ray from the light source to the intersection point.
     */
    private Double3 transparency(Intersection intersection) {
        Vector pointToLight = intersection.l.scale(-1);
        Ray shadowRay = new Ray(intersection.point, pointToLight, intersection.normal);

        var shadowIntersections = _scene.geometries.calcIntersections(shadowRay);
        Double3 ktr = Double3.ONE;
        if (shadowIntersections == null) return ktr;

        double lightDistance = intersection.light.getDistance(intersection.point);
        for (var shadowIntersection : shadowIntersections) {
            if (alignZero(intersection.point.distance(shadowIntersection.point) - lightDistance) < 0 ) {
                ktr = ktr.product(shadowIntersection.material.kT);
                if (ktr.isLowerThan(MIN_CALC_COLOR_K)) {
                    return Double3.ZERO;
                }
            }
        }
        return ktr;
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
                .add(calcColor(intersection, MAX_CALC_COLOR_LEVEL, INITIAL_K));
    }

    /**
     * Calculates the color at the given intersection point, taking into account the recursion level and the color contribution (k) for reflections and refractions.
     *
     * @param intersection The point of intersection for which to calculate the color.
     * @param level        The current recursion level, which limits the depth of recursive calls for reflections and refractions.
     * @param k            The color contribution factor, which is used to optimize the ray tracing process by ignoring contributions that are too small to affect the final color significantly.
     * @return The color at the intersection point, which is a combination of local lighting effects and contributions from reflections and refractions.
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        return calcLocalEffects(intersection, k)
                .add(calcGlobalEffects(intersection, level, k));
    }

    /**
     * Calculates the local lighting effects (diffuse and specular) at the given intersection point, taking into account the color contribution (k) for optimization.
     *
     * @param intersection The point of intersection for which to calculate the local effects.
     * @param k            The color contribution factor, which is used to optimize the ray tracing process by ignoring contributions that are too small to affect the final color significantly.
     * @return The color contribution from local lighting effects at the intersection point, which is a combination of the material's emission and the contributions from diffuse and specular lighting based on the light sources in the scene.
     */
    private Color calcLocalEffects(Intersection intersection, Double3 k) {
        Color color = intersection.geometry.getEmission();
        for (LightSource lightSource : _scene.lights) {
            if (preprocessLightSource(intersection, lightSource)) {
                Double3 ktr = transparency(intersection);
                if (ktr.product(k).isGreaterThan(MIN_CALC_COLOR_K)) {
                    color = color.add(
                            lightSource.getIntensity(intersection.point)
                                    .scale(ktr)
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
     * Calculates a global effect (reflection or refraction) for the given ray, taking into account the recursion level and the color contribution (k) for the effect.
     *
     * @param ray   The ray for which to calculate the global effect, which can be either a reflection ray or a transparency ray.
     * @param level The current recursion level, which limits the depth of recursive calls for reflections and refractions.
     * @param k     The initial color contribution factor, which is used to optimize the ray tracing process by ignoring contributions that are too small to affect the final color significantly.
     * @param kx    The attenuation factor for the specific global effect being calculated (kR for reflections or kT for refractions), which is used to optimize the ray tracing process by ignoring contributions that are too small to affect the final color significantly.
     * @return The color contribution from the global effect (reflection or refraction) for the given ray,
     * which is determined by tracing the ray through the scene and calculating the color at the closest intersection point, if any.
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkx = k.product(kx);
        if (kkx.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
        Intersection intersection = findClosestIntersection(ray);
        if (intersection == null) return _scene.background.scale(kx);
        return preprocessIntersection(intersection, ray.direction())
                ? calcColor(intersection, level - 1, kkx).scale(kx)
                : Color.BLACK;
    }

    /**
     * Calculates the global effects (reflections and refractions) at the given intersection point,
     * taking into account the recursion level and the color contribution (k) for reflections and refractions.
     *
     * @param intersection The intersection for which to calculate the global effects.
     * @param level        The current recursion level, which limits the depth of recursive calls for reflections and refractions.
     * @param k            The color contribution factor, which is used to optimize the ray tracing process by ignoring contributions that are too small to affect the final color significantly.
     * @return The color contribution from global effects (reflections and refractions) at the intersection point,
     * which is a combination of the contributions from the reflection ray and the transparency ray.
     */
    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        return calcGlobalEffect(constructReflectionRay(intersection), level, k, intersection.material.kR)
                .add(calcGlobalEffect(constructTransparencyRay(intersection), level, k, intersection.material.kT));
    }

    /**
     * Constructs a reflection ray based on the given intersection point.
     *
     * @param intersection The point of intersection for which to construct the reflection ray.
     * @return A new Ray object representing the reflection ray.
     */
    private Ray constructReflectionRay(Intersection intersection) {
        Vector tmp = intersection.normal.scale(2 * intersection.vNormal);
        Vector r = intersection.v
                .subtract(tmp);
        return new Ray(intersection.point, r, intersection.normal);
    }

    /**
     * Constructs a transparency ray based on the given intersection point.
     *
     * @param intersection The point of intersection for which to construct the transparency ray.
     * @return A new Ray object representing the transparency ray.
     */
    private Ray constructTransparencyRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.normal);
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
        Intersection intersection = findClosestIntersection(ray);
        return intersection == null ? _scene.background : calcColor(intersection, ray.direction());
    }

    /**
     * Finds the closest intersection point of the given ray with the geometries in the scene.
     *
     * @param ray The ray for which to find the closest intersection point with the geometries in the scene.
     * @return The closest Intersection object representing the intersection point of the ray with the geometries in the scene,
     * or null if there are no intersections.
     */
    private Intersection findClosestIntersection(Ray ray) {
        return ray.findClosestIntersection(_scene.geometries.calcIntersections(ray));
    }
}
