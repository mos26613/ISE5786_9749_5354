package renderer;

import java.util.List;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import scene.Scene;

class SimpleRayTracer extends RayTracerBase {
    SimpleRayTracer(Scene scene) {
        super(scene);
    }

    private Color calcColor(Point intersection) {
        return _scene.ambientLight.getIntensity();
    }
    @Override
    Color traceRay(Ray ray) {
        List<Point> intersections = _scene.geometries.findIntersections(ray);
        if (intersections == null) return _scene.background;
        return calcColor(ray.findClosestPoint(intersections));
    }
}
