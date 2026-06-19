package renderer;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import geometries.api.Intersectable;
import geometries.impl.Geometries;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for MP2 stage 2-B — the manual bounding-volume hierarchy built by nesting
 * {@link Geometries} (the Composite), reusing the 2-A CBR boxes. One test proves the
 * hierarchy never changes the rendered result; four timing methods render the same scene
 * {flat, manual} × {CBR off, CBR on} so the hierarchy's speedup can be read off the printed
 * times. The hierarchy is constructed entirely from existing types — no new data structure.
 */
class BVHTests {
    private static final Point CAMERA_ORIGIN = new Point(0, 0, 1000);

    private static final String COUNT_MSG = "Manual hierarchy returned a different number of hits than the flat list";
    private static final String CLOSEST_MSG = "Manual hierarchy returned a different closest hit than the flat list";

    /** Default constructor to satisfy the Javadoc tool. */
    BVHTests() {
    }

    /** Leave the global CBR switch off after each test so other suites are unaffected. */
    @AfterEach
    void resetCBR() {
        Intersectable.setCBR(false);
    }

    /**
     * Verifies that grouping the bodies into a manual hierarchy of nested {@link Geometries}
     * produces the same rendered result as the flat list — for rays that hit and rays that
     * miss, with CBR both off and on. Comparison is order-insensitive (the composite
     * concatenates child hits in iteration order): equal total hit count and, crucially, the
     * same closest intersection, which is what the ray tracer actually shades.
     */
    @Test
    void testManualHierarchyMatchesFlat() {
        Geometries flat = BVHScene.flat();
        Geometries hierarchy = BVHScene.hierarchy();

        for (Ray ray : probeRays()) {
            for (boolean cbr : new boolean[]{false, true}) {
                Intersectable.setCBR(cbr);
                List<Point> flatHits = flat.findIntersections(ray);
                List<Point> hierarchyHits = hierarchy.findIntersections(ray);

                assertEquals(flatHits == null ? 0 : flatHits.size(),
                        hierarchyHits == null ? 0 : hierarchyHits.size(), COUNT_MSG);
                assertEquals(ray.findClosestPoint(flatHits), ray.findClosestPoint(hierarchyHits), CLOSEST_MSG);
            }
        }
    }

    /** Flat list, CBR off — the no-acceleration baseline. */
    @Test
    void timeFlatNoCBR() {
        runTiming("bvh_flat_noCBR", BVHScene.flat(), false);
    }

    /** Flat list, CBR on — every body's box is tested per ray. */
    @Test
    void timeFlatCBR() {
        runTiming("bvh_flat_CBR", BVHScene.flat(), true);
    }

    /** Manual hierarchy, CBR off — grouping alone (no box test) gives no benefit. */
    @Test
    void timeManualNoCBR() {
        runTiming("bvh_manual_noCBR", BVHScene.hierarchy(), false);
    }

    /** Manual hierarchy, CBR on — the win: a missed cluster box skips the whole cluster. */
    @Test
    void timeManualCBR() {
        runTiming("bvh_manual_CBR", BVHScene.hierarchy(), true);
    }

    /**
     * Renders the demo scene once and prints the wall-clock render time. The CBR box caches
     * are warmed before timing so the (negligible) build cost is excluded and the
     * multithreaded-free run has no lazy-build race.
     *
     * @param name       the output image file name and label
     * @param geometries the flat or hierarchical body collection
     * @param cbr        whether to enable the CBR early-reject
     */
    private void runTiming(String name, Geometries geometries, boolean cbr) {
        Scene scene = BVHScene.scene(geometries);
        Camera camera = BVHScene.camera(scene).setCBR(cbr).build();
        scene.geometries.getBoundingBox(); // warm the box cache (single-threaded)

        long start = System.nanoTime();
        camera.renderImage();
        long ms = (System.nanoTime() - start) / 1_000_000;

        System.out.println("[BVH] " + name + ": " + ms + " ms");
        camera.writeToImage(name);
    }

    /**
     * Builds a fan of probe rays from the camera position across the cluster plane (hitting
     * many clusters and passing between others), plus a few that clearly miss the scene.
     *
     * @return the probe rays
     */
    private static List<Ray> probeRays() {
        List<Ray> rays = new ArrayList<>();
        int n = 13;
        double span = 260;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double x = -span + 2 * span * i / (n - 1);
                double y = -span + 2 * span * j / (n - 1);
                rays.add(new Ray(CAMERA_ORIGIN, new Vector(x, y, -1100))); // toward (x, y, -100)
            }
        }
        // guaranteed misses: away from and to the side of the scene
        rays.add(new Ray(CAMERA_ORIGIN, new Vector(0, 0, 1)));
        rays.add(new Ray(CAMERA_ORIGIN, new Vector(1, 0, 0)));
        rays.add(new Ray(CAMERA_ORIGIN, new Vector(0, 1, 0)));
        return rays;
    }
}
