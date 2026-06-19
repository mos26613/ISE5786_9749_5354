package geometries.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import primitives.AABB;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a cylinder geometry, which is defined by a circular base and a height.
 * The cylinder is a three-dimensional shape that extends the properties of a tube by adding height.
 */
public class Cylinder extends Tube {
    /**
     * Height of the cylinder
     */
    private final double _height;

    /**
     * Constructs a cylinder with the given radius, axis, and height.
     *
     * @param radius Radius of the cylinder's base
     * @param axis   Axis of the cylinder
     * @param height Height of the cylinder
     */
    public Cylinder(double radius, Ray axis, double height) {
        if (height < 0) {
            throw new IllegalArgumentException("Height must be non negative.");
        }
        super(radius, axis);
        this._height = height;
    }

    @Override
    public Vector getNormal(Point point) {
        // check if point is on the axis
        if (point.equals(_axis.origin())) return _axis.direction().scale(-1);

        // project point onto axis
        Vector u = point.subtract(_axis.origin());
        double t = u.dotProduct(_axis.direction());

        // check if point is on the bottom base (t ≈ 0)
        if (Util.isZero(t)) return _axis.direction().scale(-1);

        // check if point is on the top base (t ≈ height)
        if (Util.isZero(t - _height)) return _axis.direction();

        // otherwise — point is on the side surface, delegate to Tube logic
        return super.getNormal(point);
    }

    @Override
    protected AABB createBoundingBox() {
        // Conservative box: enclose the two base centres, then grow by the radius on every
        // axis. This fully contains the (possibly tilted) cylinder without needing a tight fit.
        return AABB.around(_axis.origin(), _axis.getPoint(_height)).expand(_radius);
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        Vector va = _axis.direction();
        Point pa = _axis.origin();
        Point origin = ray.origin();

        List<Intersection> result = new ArrayList<>(2);

        // Side surface: keep the infinite-tube hits whose axial projection falls
        // strictly between the two bases (0 < tAxis < height). The rim itself
        // (tAxis == 0 or height) is an edge and is therefore excluded.
        List<Intersection> sideHits = super.calcIntersectionsHelper(ray);
        if (sideHits != null) {
            for (Intersection hit : sideHits) {
                double tAxis = hit.point.subtract(pa).dotProduct(va);
                if (alignZero(tAxis) > 0 && alignZero(tAxis - _height) < 0) {
                    result.add(hit);
                }
            }
        }

        // Bottom and top bases (discs): a hit counts only when it lies strictly
        // inside the disc, the rim being excluded as an edge.
        Intersection bottom = baseIntersection(ray, pa, va);
        if (bottom != null) result.add(bottom);
        Intersection top = baseIntersection(ray, _axis.getPoint(_height), va);
        if (top != null) result.add(top);

        if (result.isEmpty()) return null;
        if (result.size() > 1) {
            result.sort(Comparator.comparingDouble(i -> i.point.distanceSquared(origin)));
        }
        return List.copyOf(result);
    }

    /**
     * Computes the intersection of a ray with one circular base (disc) of the cylinder.
     *
     * @param ray    The ray to intersect with the base.
     * @param center The center point of the base disc, lying on the axis.
     * @param va     The unit direction of the cylinder's axis, which is the base normal.
     * @return The intersection strictly inside the disc, or {@code null} when the ray is
     * parallel to the base plane, hits behind the origin, or lands on or outside the rim.
     */
    private Intersection baseIntersection(Ray ray, Point center, Vector va) {
        Point origin = ray.origin();
        double nv = alignZero(ray.direction().dotProduct(va));

        // Ray parallel to the base plane (or starting at its center): no single intersection.
        if (isZero(nv) || center.equals(origin)) return null;

        double t = alignZero(center.subtract(origin).dotProduct(va) / nv);
        if (t <= 0) return null;

        Point p = ray.getPoint(t);
        // Strictly inside the disc — the rim (distance == radius) is excluded.
        if (alignZero(p.distanceSquared(center) - _radiusSquared) >= 0) return null;
        return new Intersection(this, p);
    }
}
