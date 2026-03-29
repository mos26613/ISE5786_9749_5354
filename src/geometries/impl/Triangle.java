package geometries.impl;

import java.util.List;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.compareSign;

/**
 * Represents a triangle geometry.
 */
public class Triangle extends Polygon {
    /**
     * Constructs a triangle with the given vertices.
     *
     * @param p1 The first vertex of the triangle.
     * @param p2 The second vertex of the triangle.
     * @param p3 The third vertex of the triangle.
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        Point intersection =
                _plane.findIntersections(ray) == null ? null : _plane.findIntersections(ray).getFirst();
        if (intersection == null) {
            return null;
        }

        // Points for calculations
        Point P0 = ray.origin();
        Point P1 = _vertices.getFirst();
        Point P2 = _vertices.get(1);
        Point P3 = _vertices.get(2);

        // Vectors for calculations
        Vector v = ray.direction();
        Vector v1 = P1.subtract(P0);
        Vector v2 = P2.subtract(P0);
        Vector v3 = P3.subtract(P0);
        Vector n1 = v1.crossProduct(v2).normalize();
        Vector n2 = v2.crossProduct(v3).normalize();

        // Signs for calculations
        double s1 = alignZero(v.dotProduct(n1));
        double s2 = alignZero(v.dotProduct(n2));
        boolean flag = compareSign(s1, s2);

        if (flag) {
            return List.of(intersection);
        } else {
            return null;
        }
    }
}
