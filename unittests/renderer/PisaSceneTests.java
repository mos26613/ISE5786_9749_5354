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
 * Correctness and final-image companion to the {@link PisaTests} 12-cell measurement suite (kept
 * separate so the measurement class is exactly the 12 timing cells). Two correctness tests prove
 * the manual and automatic hierarchies render identically to the flat list (so the timing runs are
 * comparing identical results), and two beauty renders produce the high-quality submission image
 * and its hard-shadow counterpart (the MP1 soft-shadow before/after).
 */
class PisaSceneTests {

    /** Assertion message for a hit-count mismatch between a hierarchy and the flat list. */
    private static final String COUNT_MSG = "Hierarchy returned a different number of hits than the flat list";
    /** Assertion message for a closest-hit mismatch between a hierarchy and the flat list. */
    private static final String CLOSEST_MSG = "Hierarchy returned a different closest hit than the flat list";

    /** Default constructor to satisfy the Javadoc tool. */
    PisaSceneTests() {
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

    // ============================ Beauty renders ============================

    /** The submission image: full resolution, antialiasing, dense soft shadows, BVH + MT. */
    @Test
    void pisaGoldenHour() {
        render("pisaGoldenHour", true);
    }

    /** The same view with hard shadows, to show the MP1 soft-shadow effect by comparison. */
    @Test
    void pisaGoldenHourHard() {
        render("pisaGoldenHourHard", false);
    }

    /**
     * Renders the beauty view (auto BVH + CBR + multithreading), times it and writes the image.
     *
     * @param name the output image file name and label
     * @param soft whether to enable the dense soft-shadow sampler
     */
    private void render(String name, boolean soft) {
        Scene scene = PisaScene.scene(PisaScene.auto());
        Camera camera = PisaScene.beautyCamera(scene, soft).setCBR(true).build();
        scene.geometries.getBoundingBox();

        long start = System.nanoTime();
        camera.renderImage();
        System.out.println("[PISA] " + name + ": " + (System.nanoTime() - start) / 1_000_000 + " ms");
        camera.writeToImage(name);
    }
}
