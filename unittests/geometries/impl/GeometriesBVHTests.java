package geometries.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import geometries.api.Intersectable;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the automatic-BVH machinery added to {@link Geometries} (MP2 2-C): the recursive
 * {@link Geometries#flatten()} helper and the {@link Geometries#buildBVH(int)} automatic
 * build. Both must reorganise the geometries without changing any intersection result, with
 * the CBR early-reject off and on.
 */
class GeometriesBVHTests {
    private static final String COUNT_MSG = "Reorganised hierarchy returned a different number of hits than the flat list";
    private static final String CLOSEST_MSG = "Reorganised hierarchy returned a different closest hit than the flat list";

    /** Default constructor to satisfy the Javadoc tool. */
    GeometriesBVHTests() {
    }

    /** Leave the global CBR switch off after each test so other suites are unaffected. */
    @AfterEach
    void resetCBR() {
        Intersectable.setCBR(false);
    }

    /** Sphere-centre coordinates along each of the X and Y axes (well separated). */
    private static final double[] COORDS = {-60, -20, 20, 60};
    /** Common camera-side origin all probe rays start from. */
    private static final Point ORIGIN = new Point(0, 0, 100);

    /**
     * @return a deterministic flat collection of well-separated spheres on a single depth
     * plane (spacing 40 ≫ diameter 16, so the probe rays are tangent-free)
     */
    private static Geometries flatSpheres() {
        Geometries geometries = new Geometries();
        for (double cx : COORDS) {
            for (double cy : COORDS) {
                geometries.add(new Sphere(new Point(cx, cy, -100), 8));
            }
        }
        return geometries;
    }

    /**
     * @return probe rays aimed squarely through each sphere centre (clean two-point hits) and
     * through the gaps between them (clean misses), plus rays aimed away from the scene
     */
    private static List<Ray> probeRays() {
        List<Ray> rays = new ArrayList<>();
        for (double cx : COORDS) {
            for (double cy : COORDS) {
                rays.add(new Ray(ORIGIN, new Vector(cx, cy, -200)));           // through a centre
                rays.add(new Ray(ORIGIN, new Vector(cx + 20, cy + 20, -200))); // through a gap
            }
        }
        rays.add(new Ray(ORIGIN, new Vector(0, 0, 1))); // away from the scene
        rays.add(new Ray(ORIGIN, new Vector(1, 0, 0))); // to the side
        return rays;
    }

    /**
     * Asserts that two collections of the same bodies return identical rendered results
     * (equal hit count and equal closest hit) for every probe ray, with CBR off and on.
     *
     * @param reference the flat reference collection
     * @param candidate the reorganised collection to validate
     */
    private void assertSameHits(Geometries reference, Geometries candidate) {
        for (boolean cbr : new boolean[]{false, true}) {
            Intersectable.setCBR(cbr);
            for (Ray ray : probeRays()) {
                List<Point> ref = reference.findIntersections(ray);
                List<Point> cand = candidate.findIntersections(ray);
                assertEquals(ref == null ? 0 : ref.size(), cand == null ? 0 : cand.size(), COUNT_MSG);
                assertEquals(ray.findClosestPoint(ref), ray.findClosestPoint(cand), CLOSEST_MSG);
            }
        }
    }

    /**
     * Test method for {@link Geometries#flatten()}: flattening an arbitrarily-nested structure
     * yields the same leaves (so it intersects identically to the flat collection).
     */
    @Test
    void testFlattenPreservesIntersections() {
        Geometries flat = flatSpheres();
        Geometries nested = new Geometries(
                new Geometries(flat.flatten()),  // a nested branch holding all the leaves
                new Geometries(new Geometries())); // an empty nested branch (must be ignored)

        assertSameHits(flat, nested);             // the nested structure itself is equivalent
        assertSameHits(flat, nested.flatten());   // and so is its flattened form
    }

    /**
     * Test method for {@link Geometries#buildBVH(int)}: the automatic hierarchy renders
     * identically to the flat list, for both a normal leaf size and the deepest (1) nesting.
     */
    @Test
    void testAutoBuildMatchesFlat() {
        Geometries flat = flatSpheres();
        assertSameHits(flat, flat.buildBVH(2));
        assertSameHits(flat, flat.buildBVH(1));
    }

    /**
     * Test method for {@link Geometries#buildBVH(int)} with an unbounded body: an infinite
     * plane has no finite box, so it must be preserved (always tested) by the auto-build.
     */
    @Test
    void testAutoBuildKeepsUnboundedGeometries() {
        Geometries flat = flatSpheres();
        flat.add(new Plane(new Point(0, 0, -200), new Vector(0, 0, 1)));
        Geometries auto = flat.buildBVH(2);

        assertSameHits(flat, auto);

        // a ray that misses every sphere but crosses the infinite plane must still find it
        Intersectable.setCBR(true);
        Ray onlyPlane = new Ray(new Point(500, 500, 100), new Vector(0, 0, -1));
        assertNotNull(auto.findIntersections(onlyPlane), "Plane intersection lost after auto-build");
    }
}
