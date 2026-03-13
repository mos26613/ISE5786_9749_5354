package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents a tube geometry.
 */
public class Tube extends RadialGeometry {
     /**
      * Axis ray of the tube
      */
     private final Ray _axis;

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
        return null;
    }
}
