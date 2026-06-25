package renderer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import geometries.api.Intersectable;
import geometries.impl.Geometries;
import scene.Scene;

/**
 * MP2 BVH measurement suite for the {@link PisaScene}: the mandated <b>12-cell</b> timing table
 * {flat, manual, auto} × {no-CBR, CBR} × {MT off, on} — each cell is its own clearly-named test
 * that renders the same scene/camera/MP1 mode and prints its wall-clock render time, so the
 * speedups read straight off the printed numbers:
 * <ul>
 *   <li>the BVH algorithm axis — the six single-threaded cells {no-accel, CBR} × {flat, manual,
 *       automatic}, isolating the structure's speedup;</li>
 *   <li>the multithreading axis — the same six cells rendered again with MT on, which together
 *       give the four corners {accel off/on} × {MT off/on} and separate the MT speedup from the
 *       algorithm speedup.</li>
 * </ul>
 * Correctness (flat = manual = auto) and the beauty renders live in {@link PisaSceneTests}. Every
 * parameter lives in {@link PisaScene}; nothing is hard-coded here.
 */
class PisaTests {

    /** Default constructor to satisfy the Javadoc tool. */
    PisaTests() {
    }

    /** Leave the global CBR switch off after each test so other suites are unaffected. */
    @AfterEach
    void resetCBR() {
        Intersectable.setCBR(false);
    }

    // ============================ BVH algorithm axis (6 cells, MT off) ============================

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

    // ====== Multithreading axis — the same six cells, MT on (completes the 12-cell table) ======

    /** Flat list, CBR off, multithreaded — accel OFF + MT ON (pairs with {@link #timeFlatNoCBR}). */
    @Test
    void timeFlatNoCBR_MT() {
        runTiming("pisa_flat_noCBR_MT", PisaScene.flat(), false, -2);
    }

    /** Flat list, CBR on, multithreaded. */
    @Test
    void timeFlatCBR_MT() {
        runTiming("pisa_flat_CBR_MT", PisaScene.flat(), true, -2);
    }

    /** Manual hierarchy, CBR off, multithreaded. */
    @Test
    void timeManualNoCBR_MT() {
        runTiming("pisa_manual_noCBR_MT", PisaScene.hierarchy(), false, -2);
    }

    /** Manual hierarchy, CBR on, multithreaded. */
    @Test
    void timeManualCBR_MT() {
        runTiming("pisa_manual_CBR_MT", PisaScene.hierarchy(), true, -2);
    }

    /** Automatic BVH, CBR off, multithreaded. */
    @Test
    void timeAutoNoCBR_MT() {
        runAutoTiming("pisa_auto_noCBR_MT", false, -2);
    }

    /** Automatic BVH, CBR on, multithreaded — accel ON + MT ON (the fastest corner). */
    @Test
    void timeAutoCBR_MT() {
        runAutoTiming("pisa_auto_CBR_MT", true, -2);
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
}
