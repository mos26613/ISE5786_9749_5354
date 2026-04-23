package geometries.impl;

import java.util.ArrayList;
import java.util.List;

import geometries.api.Intersectable;
import primitives.Point;
import primitives.Ray;

/**
 * Represents a collection of geometries that can be intersected by rays
 */
public class Geometries extends Intersectable {

    /**
     * List of geometries in the collection
     */
    private final List<Intersectable> _geometries = new ArrayList<>();

    /**
     * Constructs a new Geometries collection with the given geometries.
     *
     * @param geometries The geometries to add to the collection
     */
    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    /**
     * Adds the given geometries to the collection.
     *
     * @param geometries The geometries to add to the collection
     */
    public void add(Intersectable... geometries) {
        _geometries.addAll(List.of(geometries));
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        List<Point> allIntersections = null; // the final result
        List<Point> intersections;

        for (Intersectable geometry : _geometries) {
            intersections = geometry.findIntersections(ray); // delegate and get all intersection points
            if (intersections != null) { // no intersection points, skip to the next geometry
                if (allIntersections == null) // not initialized yet, initialize with the first geometry's intersection points
                    allIntersections = new ArrayList<>();
                allIntersections.addAll(intersections); // add the current geometry's intersection points to the final result
            }
        }
        return allIntersections;
    }
}
