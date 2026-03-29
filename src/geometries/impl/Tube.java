package geometries.impl;

import java.util.List;

import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

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
      * @param axis The axis ray of the tube
      * @param radius  The radius of the tube
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
        if(Util.isZero(t)) return u.normalize();
        Point o = _axis.getPoint(t);
        return point.subtract(o).normalize();
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        return null; // TODO: Implement the intersection logic for the tube
    }
}
