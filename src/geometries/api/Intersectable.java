package geometries.api;

import java.util.List;

import primitives.Point;
import primitives.Ray;

public abstract class Intersectable {
    public abstract List<Point> findIntersections(Ray ray);
}
