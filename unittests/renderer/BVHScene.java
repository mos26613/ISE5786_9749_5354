package renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import geometries.api.Intersectable;
import geometries.impl.Geometries;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Reusable, deterministic demo scene for the manual-BVH (MP2 2-B) timing and correctness
 * tests. The bodies are generated from a fixed seed and laid out in well-separated spatial
 * clusters, which is exactly what makes a manually-grouped hierarchy pay off: a ray that
 * misses a cluster's bounding box skips that whole cluster at once.
 * <p>
 * The same bodies can be assembled two ways — {@link #flat()} (every body a direct child of
 * one {@link Geometries}) and {@link #hierarchy()} (one nested {@link Geometries} per cluster).
 * Because the generation is deterministic, both contain identical bodies, so timing runs are
 * apples-to-apples and the rendered images are identical. The helper is built to scale up for
 * 2-C and the final demo (raise the grid / bodies-per-cluster, add an {@code auto()} build).
 */
public final class BVHScene {
    /** Fixed RNG seed so flat and hierarchical builds are identical and reproducible. */
    private static final long SEED = 42L;
    /** Number of clusters along each of the X and Y axes (total = GRID * GRID clusters). */
    private static final int GRID = 5;
    /** Small spheres generated per cluster (plus one triangle each). */
    private static final int BODIES_PER_CLUSTER = 18;
    /** Distance between neighbouring cluster centres. */
    private static final double CLUSTER_SPACING = 110;
    /** Half-extent of the jitter applied to bodies within a cluster. */
    private static final double CLUSTER_RADIUS = 18;
    /** Minimum sphere radius. */
    private static final double MIN_R = 3;
    /** Maximum sphere radius. */
    private static final double MAX_R = 7;
    /** Square image side length used by every timing run. */
    public static final int RESOLUTION = 400;

    /** Non-instantiable helper. */
    private BVHScene() {
    }

    /**
     * Generates the demo bodies grouped by cluster (deterministic for a fixed seed).
     *
     * @return a list of clusters, each a list of the bodies belonging to that cluster
     */
    private static List<List<Intersectable>> clusters() {
        Random rnd = new Random(SEED);
        List<List<Intersectable>> clusters = new ArrayList<>();
        double offset = (GRID - 1) / 2.0 * CLUSTER_SPACING;

        for (int gx = 0; gx < GRID; gx++) {
            for (int gy = 0; gy < GRID; gy++) {
                double cx = gx * CLUSTER_SPACING - offset;
                double cy = gy * CLUSTER_SPACING - offset;
                double cz = -50 - rnd.nextDouble() * 120; // vary depth so the hierarchy is 3-D
                List<Intersectable> bodies = new ArrayList<>();

                for (int i = 0; i < BODIES_PER_CLUSTER; i++) {
                    double x = cx + jitter(rnd);
                    double y = cy + jitter(rnd);
                    double z = cz + jitter(rnd);
                    double r = MIN_R + rnd.nextDouble() * (MAX_R - MIN_R);
                    bodies.add(new Sphere(new Point(x, y, z), r)
                            .setEmission(new Color(20 + rnd.nextInt(60), 20 + rnd.nextInt(80), 40 + rnd.nextInt(120)))
                            .setMaterial(new Material().setKD(0.5).setKS(0.4).setShininess(60)));
                }

                // one triangle per cluster so the demo covers more than a single geometry type
                bodies.add(new Triangle(
                        new Point(cx - CLUSTER_RADIUS, cy - CLUSTER_RADIUS, cz),
                        new Point(cx + CLUSTER_RADIUS, cy - CLUSTER_RADIUS, cz),
                        new Point(cx, cy + CLUSTER_RADIUS, cz))
                        .setEmission(new Color(60, 30, 30))
                        .setMaterial(new Material().setKD(0.5).setKS(0.2).setShininess(30)));

                clusters.add(bodies);
            }
        }
        return clusters;
    }

    /**
     * @param rnd the random source
     * @return a uniform jitter value in [-CLUSTER_RADIUS, CLUSTER_RADIUS]
     */
    private static double jitter(Random rnd) {
        return (rnd.nextDouble() * 2 - 1) * CLUSTER_RADIUS;
    }

    /**
     * Builds the scene as a single flat {@link Geometries} — every body is a direct child.
     * This is the baseline list (and what an automatic build in 2-C will reorganise).
     *
     * @return a flat collection of all demo bodies
     */
    public static Geometries flat() {
        Geometries flat = new Geometries();
        for (List<Intersectable> cluster : clusters()) {
            flat.add(cluster.toArray(new Intersectable[0]));
        }
        return flat;
    }

    /**
     * Builds the scene as a manual bounding-volume hierarchy: one nested {@link Geometries}
     * per cluster, all held by a root {@link Geometries}. This is the hand-grouped BVH.
     *
     * @return a two-level hierarchy of the same demo bodies
     */
    public static Geometries hierarchy() {
        Geometries root = new Geometries();
        for (List<Intersectable> cluster : clusters()) {
            root.add(new Geometries(cluster.toArray(new Intersectable[0])));
        }
        return root;
    }

    /**
     * Wraps the given geometry collection in a scene with ambient + two lights (a point light
     * and a directional light). The MP1 effect is intentionally left off here.
     *
     * @param geometries the flat or hierarchical collection to render
     * @return the ready-to-render scene
     */
    public static Scene scene(Geometries geometries) {
        Scene scene = new Scene("BVH demo")
                .setBackground(new Color(5, 5, 12))
                .setAmbientLight(new AmbientLight(new Color(30, 30, 35)));
        scene.setGeometries(geometries);
        scene.lights.add(new PointLight(new Color(700, 600, 500), new Point(300, 300, 500))
                .setKl(4e-4).setKq(2e-6));
        scene.lights.add(new DirectionalLight(new Color(120, 120, 150), new Vector(-1, -1, -2)));
        return scene;
    }

    /**
     * The single camera configuration reused by every timing run, so all runs are identical
     * apart from the geometry layout and the CBR switch. Single-threaded to isolate the
     * algorithm speedup from multithreading.
     *
     * @param scene the scene the camera renders
     * @return a configured Camera builder
     */
    public static Camera.Builder camera(Scene scene) {
        return Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(new Vector(0, 0, -1), Vector.AXIS_Y)
                .setVpDistance(1000).setVpSize(620, 620)
                .setResolution(RESOLUTION, RESOLUTION)
                .setMultithreading(0);
    }
}
