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
 * MP2 acceleration demo + measurement suite for the {@link PisaScene} (the Leaning Tower of
 * Pisa at golden hour: ~2,500 bodies of every geometry type, five lights of all supported
 * types, shadow + reflection + refraction, and the MP1 soft-shadow effect).
 * <p>
 * One correctness test proves the manual and automatic hierarchies render identically to the
 * flat list. The timing methods render the <em>same</em> scene/camera/MP1 mode under the
 * mandated configurations so the speedups can be read off the printed times:
 * <ul>
 *   <li>the BVH algorithm axis — the 6-row table {no-accel, CBR} × {flat, manual, automatic},
 *       single-threaded, isolating the structure's speedup;</li>
 *   <li>the multithreading axis — accel-off and accel-on each rendered with MT on, which
 *       together with the single-threaded rows give the four corners
 *       {accel off/on} × {MT off/on} and separate the MT speedup from the algorithm speedup.</li>
 * </ul>
 * Finally, a high-quality beauty render (and an optional hard-shadow counterpart) produces the
 * submission image. Every parameter lives in {@link PisaScene}; nothing is hard-coded here.
 */
class PisaTests {

    /** Assertion message for a hit-count mismatch between a hierarchy and the flat list. */
    private static final String COUNT_MSG = "Hierarchy returned a different number of hits than the flat list";
    /** Assertion message for a closest-hit mismatch between a hierarchy and the flat list. */
    private static final String CLOSEST_MSG = "Hierarchy returned a different closest hit than the flat list";

    /** Default constructor to satisfy the Javadoc tool. */
    PisaTests() {
    }

    /** Leave the global CBR switch off after each test so other suites are unaffected. */
    @AfterEach
    void resetCBR() {
        Intersectable.setCBR(false);
    }

    // ============================ Correctness ============================

    /** The manual hierarchy must render identically to the flat list (CBR off and on). */
    @Test
    void testManualHierarchyMatchesFlat() {
        assertMatchesFlat(PisaScene.hierarchy());
    }

    /** The automatic BVH must render identically to the flat list (CBR off and on). */
    @Test
    void testAutoHierarchyMatchesFlat() {
        assertMatchesFlat(PisaScene.auto());
    }

    /**
     * Verifies that an alternative organisation of the same bodies yields the same rendered
     * result as the flat list — equal hit count and, crucially, the same closest intersection
     * (what the ray tracer actually shades) — over a fan of probe rays, with CBR off and on.
     *
     * @param other the hierarchy (manual or automatic) to compare against the flat list
     */
    private void assertMatchesFlat(Geometries other) {
        Geometries flat = PisaScene.flat();
        for (Ray ray : probeRays()) {
            for (boolean cbr : new boolean[]{false, true}) {
                Intersectable.setCBR(cbr);
                List<Point> flatHits = flat.findIntersections(ray);
                List<Point> otherHits = other.findIntersections(ray);
                assertEquals(flatHits == null ? 0 : flatHits.size(),
                        otherHits == null ? 0 : otherHits.size(), COUNT_MSG);
                assertEquals(ray.findClosestPoint(flatHits), ray.findClosestPoint(otherHits), CLOSEST_MSG);
            }
        }
    }

    // ============================ BVH algorithm axis (6-row table, MT off) ============================

    /** Flat list, CBR off — the no-acceleration baseline (= accel OFF + MT OFF). */
    @Test
    void timeFlatNoCBR() {
        runTiming("pisa_flat_noCBR", PisaScene.flat(), false, 0);
    }

    /** Flat list, CBR on — every body's box is tested per ray, no grouping to prune. */
    @Test
    void timeFlatCBR() {
        runTiming("pisa_flat_CBR", PisaScene.flat(), true, 0);
    }

    /** Manual hierarchy, CBR off — grouping without a box test gives no benefit (≈ baseline). */
    @Test
    void timeManualNoCBR() {
        runTiming("pisa_manual_noCBR", PisaScene.hierarchy(), false, 0);
    }

    /** Manual hierarchy, CBR on — a missed cluster box skips the whole cluster. */
    @Test
    void timeManualCBR() {
        runTiming("pisa_manual_CBR", PisaScene.hierarchy(), true, 0);
    }

    /** Automatic BVH, CBR off — the deep tree without box tests can't prune (≈ baseline / worse). */
    @Test
    void timeAutoNoCBR() {
        runAutoTiming("pisa_auto_noCBR", false, 0);
    }

    /** Automatic BVH, CBR on — the headline acceleration (= accel ON + MT OFF). */
    @Test
    void timeAutoCBR() {
        runAutoTiming("pisa_auto_CBR", true, 0);
    }

    // ============================ Multithreading axis ============================

    /** Flat list, CBR off, multithreaded — accel OFF + MT ON (pairs with {@link #timeFlatNoCBR}). */
    @Test
    void timeFlatNoCBR_MT() {
        runTiming("pisa_flat_noCBR_MT", PisaScene.flat(), false, -2);
    }

    /** Automatic BVH, CBR on, multithreaded — accel ON + MT ON (the fastest corner). */
    @Test
    void timeAutoCBR_MT() {
        runAutoTiming("pisa_auto_CBR_MT", true, -2);
    }

    // ============================ Beauty render ============================

    /** The submission image: full resolution, antialiasing, dense soft shadows, BVH + MT. */
    @Test
    void pisaGoldenHour() {
        Geometries geometries = PisaScene.auto();
        Scene scene = PisaScene.scene(geometries);
        Camera camera = PisaScene.beautyCamera(scene, true).setCBR(true).build();
        scene.geometries.getBoundingBox();

        long start = System.nanoTime();
        camera.renderImage();
        System.out.println("[PISA] pisaGoldenHour (soft): " + (System.nanoTime() - start) / 1_000_000 + " ms");
        camera.writeToImage("pisaGoldenHour");
    }

    /** The same view with hard shadows, to show the MP1 soft-shadow effect by comparison. */
    @Test
    void pisaGoldenHourHard() {
        Geometries geometries = PisaScene.auto();
        Scene scene = PisaScene.scene(geometries);
        Camera camera = PisaScene.beautyCamera(scene, false).setCBR(true).build();
        scene.geometries.getBoundingBox();

        long start = System.nanoTime();
        camera.renderImage();
        System.out.println("[PISA] pisaGoldenHour (hard): " + (System.nanoTime() - start) / 1_000_000 + " ms");
        camera.writeToImage("pisaGoldenHourHard");
    }

    // ============================ Timing helpers ============================

    /**
     * Builds the automatic BVH with its build time measured separately, then renders and times it.
     *
     * @param name    the output image file name and label
     * @param cbr     whether to enable the CBR early-reject
     * @param threads the thread count (0 single-threaded, -2 auto)
     */
    private void runAutoTiming(String name, boolean cbr, int threads) {
        long buildStart = System.nanoTime();
        Geometries auto = PisaScene.auto();
        System.out.println("[PISA] auto build overhead: " + (System.nanoTime() - buildStart) / 1_000_000 + " ms");
        runTiming(name, auto, cbr, threads);
    }

    /**
     * Renders the demo scene once and prints the wall-clock render time. The bounding-box caches
     * are warmed before timing so the (negligible) lazy-build cost is excluded and the
     * multithreaded run has no lazy-build race.
     *
     * @param name       the output image file name and label
     * @param geometries the flat or hierarchical body collection
     * @param cbr        whether to enable the CBR early-reject
     * @param threads    the thread count (0 single-threaded, -2 auto)
     */
    private void runTiming(String name, Geometries geometries, boolean cbr, int threads) {
        Scene scene = PisaScene.scene(geometries);
        Camera camera = PisaScene.camera(scene, threads).setCBR(cbr).build();
        scene.geometries.getBoundingBox(); // warm the box cache before timing

        long start = System.nanoTime();
        camera.renderImage();
        long ms = (System.nanoTime() - start) / 1_000_000;

        System.out.println("[PISA] " + name + ": " + ms + " ms");
        camera.writeToImage(name);
    }

    /**
     * Builds a fan of probe rays from the camera position across the scene (hitting the tower,
     * pool, trees and ground at many angles) plus a few guaranteed misses.
     *
     * @return the probe rays
     */
    private static List<Ray> probeRays() {
        List<Ray> rays = new ArrayList<>();
        Point eye = new Point(430, 305, 860);
        int n = 11;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                double x = -520 + 1040.0 * i / (n - 1);
                double y = 520.0 * j / (n - 1);
                rays.add(new Ray(eye, new Point(x, y, -200).subtract(eye)));
            }
        rays.add(new Ray(eye, new Vector(0, 1, 0)));
        rays.add(new Ray(eye, new Vector(0, 0, 1)));
        rays.add(new Ray(eye, new Vector(1, 0.2, 0)));
        return rays;
    }
}
