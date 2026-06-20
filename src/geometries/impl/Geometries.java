package geometries.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import geometries.api.Intersectable;
import primitives.AABB;
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

    /**
     * Returns a flat collection of this hierarchy's leaf geometries, descending recursively
     * into any nested {@code Geometries}. Used as the starting point for the automatic BVH
     * build, so a scene can be assembled in any structure and still be reorganised.
     *
     * @return a new flat {@code Geometries} containing every leaf geometry
     */
    public Geometries flatten() {
        List<Intersectable> leaves = new ArrayList<>();
        flattenInto(leaves);
        return new Geometries(leaves.toArray(new Intersectable[0]));
    }

    /**
     * Recursively collects the leaf geometries of this hierarchy into the given list.
     *
     * @param leaves the accumulator the leaves are added to
     */
    private void flattenInto(List<Intersectable> leaves) {
        for (Intersectable geometry : _geometries) {
            if (geometry instanceof Geometries sub) sub.flattenInto(leaves);
            else leaves.add(geometry);
        }
    }

    /**
     * Automatically reorganises this collection into a bounding-volume hierarchy of nested
     * {@code Geometries}, so the CBR boxes can prune whole subtrees at render time. The
     * collection is first flattened to its leaves; bounded leaves are recursively split, and
     * unbounded (infinite) leaves are attached at the root so they are always tested. The
     * source collection is not modified — a new hierarchy is returned.
     *
     * @param maxLeafSize the maximum number of geometries allowed in a leaf node (≥ 1)
     * @return a new hierarchical {@code Geometries} equivalent to this one
     */
    public Geometries buildBVH(int maxLeafSize) {
        List<Intersectable> leaves = new ArrayList<>();
        flattenInto(leaves);

        List<Intersectable> bounded = new ArrayList<>();
        List<Intersectable> unbounded = new ArrayList<>();
        for (Intersectable leaf : leaves) {
            if (leaf.getBoundingBox() == null) unbounded.add(leaf);
            else bounded.add(leaf);
        }

        // No infinite bodies: the bounded subtree is already the whole hierarchy.
        if (unbounded.isEmpty()) {
            return bounded.isEmpty() ? new Geometries() : asGeometries(buildNode(bounded, maxLeafSize));
        }

        // Otherwise hold the bounded subtree and the always-tested infinite bodies together.
        Geometries root = new Geometries();
        if (!bounded.isEmpty()) root.add(buildNode(bounded, maxLeafSize));
        root.add(unbounded.toArray(new Intersectable[0]));
        return root;
    }

    /**
     * Wraps an intersectable in a {@code Geometries} if it is not already one.
     *
     * @param node the node to return as a {@code Geometries} root
     * @return {@code node} cast when it is a {@code Geometries}, otherwise a wrapper around it
     */
    private static Geometries asGeometries(Intersectable node) {
        return node instanceof Geometries geometries ? geometries : new Geometries(node);
    }

    /**
     * Recursively partitions bounded geometries into a balanced BVH node. Leaves whose count
     * is within {@code maxLeafSize} become a single {@code Geometries}; larger sets are split
     * at the median along the longest axis of their combined bounding box.
     *
     * @param items       the bounded geometries to partition (all have a finite box)
     * @param maxLeafSize the maximum number of geometries allowed in a leaf node
     * @return the root intersectable of the built subtree
     */
    private static Intersectable buildNode(List<Intersectable> items, int maxLeafSize) {
        if (items.size() <= maxLeafSize) return new Geometries(items.toArray(new Intersectable[0]));

        AABB bounds = items.getFirst().getBoundingBox();
        for (int i = 1; i < items.size(); i++) bounds = bounds.union(items.get(i).getBoundingBox());
        int axis = bounds.longestAxis();

        items.sort(Comparator.comparingDouble(item -> item.getBoundingBox().center(axis)));
        int mid = items.size() / 2;
        Intersectable left = buildNode(new ArrayList<>(items.subList(0, mid)), maxLeafSize);
        Intersectable right = buildNode(new ArrayList<>(items.subList(mid, items.size())), maxLeafSize);
        return new Geometries(left, right);
    }

    @Override
    protected AABB createBoundingBox() {
        // The composite box is the union of the children's boxes. If any child is unbounded
        // (null box) the whole collection cannot be bounded, so report unbounded as well.
        AABB result = null;
        for (Intersectable geometry : _geometries) {
            AABB box = geometry.getBoundingBox();
            if (box == null) return null;
            result = result == null ? box : result.union(box);
        }
        return result;
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        List<Intersection> allIntersections = null; // the final result
        List<Intersection> intersections;

        for (Intersectable geometry : _geometries) {
            intersections = geometry.calcIntersections(ray); // delegate and get all intersection points
            if (intersections != null) { // no intersection points, skip to the next geometry
                if (allIntersections == null) // not initialized yet, initialize with the first geometry's intersection points
                    allIntersections = new ArrayList<>();
                allIntersections.addAll(intersections); // add the current geometry's intersection points to the final result
            }
        }
        return allIntersections;
    }
}
