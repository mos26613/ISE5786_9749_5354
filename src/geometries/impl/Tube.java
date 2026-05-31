package geometries.impl;

import java.util.List;

import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

import static java.lang.Math.sqrt;
import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a tube geometry.
 */
public class Tube extends RadialGeometry {
    /**
     * Axis ray of the tube
     */
    protected final Ray _axis;

    /**
     * Constructs a new Tube with the given axis ray and radius.
     *
     * @param axis   The axis ray of the tube
     * @param radius The radius of the tube
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        this._axis = axis;
    }

    @Override
    public String toString() {
        return super.toString() +
                "/nTube{" +
                "axisRay=" + _axis +
                '}';
    }

    @Override
    public Vector getNormal(Point point) {
        Vector u = point.subtract(_axis.origin());
        double t = u.dotProduct(_axis.direction());
        if (Util.isZero(t)) return u.normalize();
        Point o = _axis.getPoint(t);
        return point.subtract(o).normalize();
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        Vector va = _axis.direction();
        Vector v = ray.direction();
        Point p0 = ray.origin();
        Point pa = _axis.origin();

        // Coefficient of t^2: squared length of the ray direction's component
        // perpendicular to the axis. Zero means the ray is parallel to the axis,
        // so it never crosses the (infinite) surface.
        double dv = v.dotProduct(va);
        double a = 1 - dv * dv;
        if (isZero(a)) return null;

        // Vector from the axis origin to the ray origin, guarding the zero-vector case
        double dpva, vdp, dpSq;
        if (p0.equals(pa)) {
            dpva = vdp = dpSq = 0;
        } else {
            Vector dp = p0.subtract(pa);
            dpva = dp.dotProduct(va);
            vdp = v.dotProduct(dp);
            dpSq = dp.lengthSquared();
        }

        double b = 2 * (vdp - dv * dpva);
        double c = dpSq - dpva * dpva - _radiusSquared;

        // disc < 0: no intersection; disc == 0: tangent line (excluded)
        double disc = alignZero(b * b - 4 * a * c);
        if (disc <= 0) return null;

        double sqrtDisc = sqrt(disc);
        // a > 0, so t1 < t2 (closer intersection first)
        double t1 = alignZero((-b - sqrtDisc) / (2 * a));
        double t2 = alignZero((-b + sqrtDisc) / (2 * a));

        if (t1 <= 0 && t2 <= 0) return null;
        if (t1 > 0 && t2 > 0) return List.of(new Intersection(this, ray.getPoint(t1)),
                                             new Intersection(this, ray.getPoint(t2)));
        return List.of(new Intersection(this, ray.getPoint(t2)));
    }
}
