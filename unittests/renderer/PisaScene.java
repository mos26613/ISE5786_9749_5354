package renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import geometries.api.Intersectable;
import geometries.impl.Cylinder;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.isZero;

/**
 * Deterministic, reusable demo scene for the MP2 acceleration submission: the
 * <b>Leaning Tower of Pisa at golden hour</b>, on a marble piazza beside a still reflecting
 * pool, flanked by Italian cypresses, with clear-glass crystal orbs and polished mirror
 * spheres in the foreground.
 * <p>
 * The scene is built entirely by composing the engine's primitive geometries (the tower is a
 * stack of tilted {@link Cylinder} drums ringed by {@link Cylinder} columns and
 * {@link Sphere} voussoir arches; trees are {@link Triangle} tiers; the plaza is a grid of
 * {@link Polygon} tiles, etc.), so it exercises every supported geometry type and reaches a
 * few thousand bodies. It is shaped to make all four effects read clearly: the low sun casts
 * the tower's long shadow across the piazza (shadow), the pool mirrors the tower (reflection),
 * the glass orbs refract the colonnade behind them (refraction), and the warm area lights
 * cast soft penumbrae (the MP1 soft-shadow effect).
 * <p>
 * Generation is driven by a single fixed-seed {@link Random}, so {@link #flat()},
 * {@link #hierarchy()} and {@link #auto()} contain byte-identical bodies — the timing runs are
 * apples-to-apples and every rendered image is identical apart from speed. Infinite geometries
 * (the ground {@link Plane} and the decorative {@link Tube} poles) are kept at the root so they
 * never null a bounded group's bounding box.
 */
public final class PisaScene {

    // ============================ Tunable constants ============================

    /** Fixed RNG seed so flat/manual/auto builds are identical and reproducible. */
    private static final long SEED = 1173L;

    // ---- Tower geometry (scene scaled to hundreds of units; foot at the origin) ----
    /** Lean angle from vertical, in degrees (the historic tower leans ~4°; 5.5° reads clearly). */
    private static final double LEAN_DEG = 5.5;
    /** Azimuth of the lean direction, in degrees (toward +x, tilted slightly toward the camera). */
    private static final double LEAN_AZ_DEG = 24;
    /** Radius of the wide ground-floor drum. */
    private static final double GROUND_R = 104;
    /** Height of the ground-floor drum (the blind arcade). */
    private static final double GROUND_H = 92;
    /** Number of open galleries (loggias) stacked above the ground floor. */
    private static final int GALLERIES = 6;
    /** Height of one gallery storey. */
    private static final double GALLERY_H = 60;
    /** Radius of a gallery's solid core drum. */
    private static final double DRUM_R = 86;
    /** Radius of the ring on which a gallery's free-standing columns stand. */
    private static final double RING_R = 99;
    /** Radius of a single column shaft. */
    private static final double COL_R = 6.5;
    /** Number of columns in each gallery ring. */
    private static final int COLS = 24;
    /** Number of voussoir spheres approximating each arch between adjacent columns. */
    private static final int ARCH_VOUSSOIRS = 5;
    /** Radius of the narrower bell-chamber drum on top. */
    private static final double BELL_R = 70;
    /** Height of the bell chamber. */
    private static final double BELL_H = 58;
    /** Number of columns ringing the bell chamber. */
    private static final int BELL_COLS = 14;

    // ---- Piazza / pool ----
    /** Number of marble tiles along each side of the square plaza grid. */
    private static final int TILE_N = 22;
    /** Edge length of a single square plaza tile. */
    private static final double TILE_SIZE = 78;
    /** Mortar gap between adjacent tiles. */
    private static final double TILE_GAP = 2.2;
    /** Surface height of the reflecting pool (a hair above the tiles, to mirror cleanly). */
    private static final double WATER_Y = 2.2;
    /** Number of scattered gravel/grass pebbles dressing the open ground. */
    private static final int GRAVEL = 460;

    // ---- Render configuration (matrix = the shared timing config; beauty = the hero image) ----
    /** Horizontal resolution of every timing-matrix render. */
    public static final int MATRIX_W = 640;
    /** Vertical resolution of every timing-matrix render. */
    public static final int MATRIX_H = 480;
    /** Soft-shadow samples per axis used by every timing-matrix render (genuinely soft, bounded cost). */
    public static final int MATRIX_SS = 4;
    /** Maximum geometries per leaf for the automatic BVH build. */
    public static final int AUTO_LEAF_SIZE = 3;
    /** Horizontal resolution of the high-quality beauty render. */
    public static final int BEAUTY_W = 1200;
    /** Vertical resolution of the high-quality beauty render. */
    public static final int BEAUTY_H = 900;
    /** Antialiasing samples per axis for the beauty render (3 -> 9 rays per pixel). */
    public static final int BEAUTY_AA = 3;
    /** Soft-shadow samples per axis for the beauty render (9 -> ~64 circular samples). */
    public static final int BEAUTY_SS = 9;

    /** Camera position: elevated, front-right of the tower, looking across the pool. */
    private static final Point CAM_POS = new Point(470, 332, 945);
    /** Camera aim point: a little below the tower's mid-height (keeps the pool in frame). */
    private static final Point CAM_TARGET = new Point(8, 236, 0);
    /** Vertical field of view, in degrees. */
    private static final double FOV_DEG = 47;
    /** View-plane distance shared by all renders. */
    private static final double VP_DIST = 720;

    // ---- Lighting (bright, linear-RGB; the renderer applies no gamma) ----
    /** Direction the low golden sun light travels: from behind-left toward the camera, grazing
     * down the piazza so the tower throws a long shadow toward the viewer. */
    private static final Vector SUN_DIR = new Vector(0.42, -0.30, 0.86);
    /** Area diameter of the warm lantern point lights (soft-shadow penumbra width). */
    private static final double LANTERN_SIZE = 42;

    // ============================ Shared materials ============================

    /** Warm off-white marble for the tower drums (matte; the pool carries the reflection). */
    private static final Material MARBLE = new Material()
            .setKD(new Double3(0.62, 0.58, 0.50)).setKS(0.45).setShininess(120)
            .setKA(new Double3(0.62, 0.59, 0.52));
    /** Slightly brighter marble for columns/capitals/arches (catches the warm light). */
    private static final Material MARBLE_COL = new Material()
            .setKD(new Double3(0.70, 0.66, 0.57)).setKS(0.5).setShininess(150)
            .setKA(new Double3(0.68, 0.64, 0.56));
    /** Still water: a dark, highly reflective mirror (kR dominant; no kT, an open surface). */
    private static final Material WATER = new Material()
            .setKD(new Double3(0.02, 0.04, 0.05)).setKS(0.55).setShininess(280)
            .setKA(new Double3(0.02, 0.03, 0.04)).setKR(new Double3(0.74, 0.80, 0.84));
    /** Clear crystal glass for the orbs (kT dominant; kR≈0 to avoid trapping rays). */
    private static final Material GLASS = new Material()
            .setKD(new Double3(0.02, 0.03, 0.03)).setKS(0.6).setShininess(300)
            .setKA(new Double3(0.02, 0.02, 0.02)).setKT(new Double3(0.82, 0.86, 0.88));
    /** Polished chrome mirror for the accent spheres (kR only). */
    private static final Material MIRROR = new Material()
            .setKD(new Double3(0.02, 0.02, 0.02)).setKS(0.5).setShininess(300)
            .setKA(new Double3(0.02, 0.02, 0.02)).setKR(new Double3(0.90, 0.90, 0.93));
    /** Dark cypress foliage. */
    private static final Material FOLIAGE = solid(0.07, 0.30, 0.14, 0.7, 0.2, 40, 0.30);
    /** Wood (tree trunks). */
    private static final Material WOOD = solid(0.30, 0.19, 0.10, 0.7, 0.1, 20, 0.40);
    /** Grey stone (pedestals, pool rim, lamp posts, steps). */
    private static final Material STONE = solid(0.50, 0.48, 0.45, 0.7, 0.25, 60, 0.50);
    /** Low shrubs. */
    private static final Material BUSH = solid(0.11, 0.34, 0.16, 0.7, 0.2, 40, 0.30);
    /** Warm bronze for the decorative flag poles. */
    private static final Material METAL = solid(0.46, 0.33, 0.16, 0.5, 0.5, 140, 0.45);
    /** Plain cloth material for the flags (color carried by emission so they stay vivid). */
    private static final Material CLOTH = solid(0.5, 0.5, 0.5, 0.5, 0.1, 30, 0.4);
    /** X/Z positions of the two flag poles (also used to hang their flags). */
    private static final double[][] POLES = {{-430, 150}, {430, 150}};

    /** Fixed lamp-post positions on the piazza (x, z); the glow heights are added in code. */
    private static final double[][] LAMPS = {
            {-330, 250}, {330, 270}, {-250, -30}, {250, -30}, {-120, 430}, {120, 430}
    };
    /** Height of a lamp post (the glow sphere sits at its top). */
    private static final double LAMP_H = 118;

    /** Non-instantiable helper. */
    private PisaScene() {
    }

    // ============================ Body assemblies ============================

    /**
     * Generates the scene's bounded bodies grouped into structural clusters (deterministic for
     * the fixed seed). Each cluster is a spatially compact set of bodies — exactly what makes a
     * bounding-volume hierarchy pay off, since a ray that misses a cluster box skips it whole.
     *
     * @return a list of clusters, each a list of the bounded bodies in that cluster
     */
    private static List<List<Intersectable>> groups() {
        Random rnd = new Random(SEED);
        List<List<Intersectable>> groups = new ArrayList<>();

        groups.add(tower());
        groups.add(tileFloor(rnd));
        groups.add(pool());
        for (double[] t : new double[][]{
                {-290, -150}, {250, -190}, {-180, -350}, {360, 90},
                {-380, 70}, {150, -420}, {-60, -300}})
            groups.add(cypress(t[0], t[1], rnd));
        groups.add(gravel(rnd));
        groups.add(props());
        groups.add(sky());
        return groups;
    }

    /**
     * The genuinely infinite bodies, which must sit at the hierarchy root (their boxes are null,
     * so nesting them inside a bounded group would null that group's box and disable pruning).
     *
     * @return the ground plane and the decorative banner-pole tubes
     */
    private static List<Intersectable> infiniteBodies() {
        List<Intersectable> out = new ArrayList<>();
        out.add(new Plane(new Point(0, -1, 0), Vector.AXIS_Y)
                .setMaterial(solid(0.56, 0.47, 0.33, 0.7, 0.06, 18, 0.52)));
        for (double[] p : POLES)
            out.add(new Tube(2.4, new Ray(new Point(p[0], 0, p[1]), Vector.AXIS_Y)).setMaterial(METAL));
        return out;
    }

    /**
     * Builds the scene as one flat {@link Geometries} — every body a direct child (the no-BVH
     * baseline that {@link #auto()} reorganises).
     *
     * @return a flat collection of all bodies
     */
    public static Geometries flat() {
        Geometries flat = new Geometries();
        for (List<Intersectable> g : groups()) flat.add(g.toArray(new Intersectable[0]));
        flat.add(infiniteBodies().toArray(new Intersectable[0]));
        return flat;
    }

    /**
     * Builds the scene as a manual bounding-volume hierarchy: one nested {@link Geometries} per
     * structural cluster, with the infinite bodies kept directly at the root.
     *
     * @return a two-level hierarchy of the same bodies
     */
    public static Geometries hierarchy() {
        Geometries root = new Geometries();
        for (List<Intersectable> g : groups()) root.add(new Geometries(g.toArray(new Intersectable[0])));
        root.add(infiniteBodies().toArray(new Intersectable[0]));
        return root;
    }

    /**
     * Builds the scene as an automatically-organised bounding-volume hierarchy via
     * {@link Geometries#buildBVH(int)} (which also lifts the infinite bodies to the root).
     *
     * @return an automatically-built hierarchy of the same bodies
     */
    public static Geometries auto() {
        return flat().buildBVH(AUTO_LEAF_SIZE);
    }

    // ============================ The tower ============================

    /**
     * Builds the whole leaning tower as one compact cluster: stacked tilted drums, a ring of
     * columns + arches per gallery, the bell chamber, and a finial.
     *
     * @return the tower's bodies
     */
    private static List<Intersectable> tower() {
        List<Intersectable> out = new ArrayList<>();
        Vector a = leanAxis();
        Vector[] e = radialBasis(a);
        Point foot = new Point(0, 0, 0);
        double h = 0;

        // ground floor: wide drum with engaged (flush) half-columns -> the blind arcade
        out.add(new Cylinder(GROUND_R, new Ray(along(foot, a, h), a), GROUND_H).setMaterial(MARBLE));
        ring(out, foot, a, e, h, GROUND_R + COL_R * 0.4, COLS, GROUND_H - 8, false);
        h += GROUND_H;

        // six open galleries: core drum + free-standing column ring + arches
        for (int g = 0; g < GALLERIES; g++) {
            out.add(new Cylinder(DRUM_R, new Ray(along(foot, a, h), a), GALLERY_H).setMaterial(MARBLE));
            ring(out, foot, a, e, h, RING_R, COLS, GALLERY_H - 8, true);
            arches(out, foot, a, e, h + (GALLERY_H - 8), RING_R, COLS);
            h += GALLERY_H;
        }

        // bell chamber: narrower drum, set back along the lean axis, with its own colonnade
        out.add(new Cylinder(BELL_R, new Ray(along(foot, a, h), a), BELL_H).setMaterial(MARBLE));
        ring(out, foot, a, e, h, BELL_R + 11, BELL_COLS, BELL_H - 10, true);
        arches(out, foot, a, e, h + (BELL_H - 10), BELL_R + 11, BELL_COLS);
        h += BELL_H;

        // finial cap
        out.add(new Sphere(along(foot, a, h + 8), 12).setMaterial(MARBLE_COL));
        return out;
    }

    /**
     * Adds a ring of columns around the tower axis at a given height: each a thin cylinder
     * parallel to the (tilted) axis, optionally with a base disc and a capital sphere.
     *
     * @param out      the body list to populate
     * @param foot     the tower foot (origin of the axis)
     * @param a        the unit lean axis
     * @param e        the orthonormal radial basis perpendicular to {@code a}
     * @param baseDist distance along {@code a} of the ring's base
     * @param ringR    radius of the ring
     * @param cols     number of columns
     * @param colH     column height
     * @param capped   whether to add base discs and capital spheres (false for engaged columns)
     */
    private static void ring(List<Intersectable> out, Point foot, Vector a, Vector[] e,
                             double baseDist, double ringR, int cols, double colH, boolean capped) {
        Point center = along(foot, a, baseDist);
        for (int j = 0; j < cols; j++) {
            double ang = 2 * Math.PI * j / cols;
            Point colBase = center.add(radial(e, ang).scale(ringR));
            out.add(new Cylinder(COL_R, new Ray(colBase, a), colH).setMaterial(MARBLE_COL));
            if (capped) {
                out.add(new Cylinder(COL_R * 1.5, new Ray(colBase, a), COL_R * 0.7).setMaterial(MARBLE_COL));
                out.add(new Sphere(colBase.add(a.scale(colH)), COL_R * 1.35).setMaterial(MARBLE_COL));
            }
        }
    }

    /**
     * Adds a semicircular arch between every pair of adjacent columns, approximated by a row of
     * small voussoir spheres bulging upward along the tower axis (kept convex — no Polygon arc).
     *
     * @param out     the body list to populate
     * @param foot    the tower foot (origin of the axis)
     * @param a       the unit lean axis
     * @param e       the orthonormal radial basis perpendicular to {@code a}
     * @param topDist distance along {@code a} of the column tops (arch springing line)
     * @param ringR   radius of the column ring
     * @param cols    number of columns (and arches)
     */
    private static void arches(List<Intersectable> out, Point foot, Vector a, Vector[] e,
                               double topDist, double ringR, int cols) {
        Point center = along(foot, a, topDist);
        double chord = 2 * ringR * Math.sin(Math.PI / cols);
        double rise = chord * 0.55;
        double r = Math.max(2.5, chord * 0.16);
        for (int j = 0; j < cols; j++) {
            Point p0 = center.add(radial(e, 2 * Math.PI * j / cols).scale(ringR));
            Point p1 = center.add(radial(e, 2 * Math.PI * (j + 1) / cols).scale(ringR));
            Vector chordVec = p1.subtract(p0);
            for (int m = 1; m <= ARCH_VOUSSOIRS; m++) {
                double u = (double) m / (ARCH_VOUSSOIRS + 1);
                Point pos = p0.add(chordVec.scale(u)).add(a.scale(rise * Math.sin(Math.PI * u)));
                out.add(new Sphere(pos, r).setMaterial(MARBLE_COL));
            }
        }
    }

    // ============================ Piazza, pool, planting ============================

    /**
     * Builds the marble piazza as a grid of square {@link Polygon} tiles with slight warm-tone
     * variation and a faint reflective sheen.
     *
     * @param rnd the shared random source (per-tile tone jitter)
     * @return the tile bodies
     */
    private static List<Intersectable> tileFloor(Random rnd) {
        List<Intersectable> out = new ArrayList<>();
        double half = TILE_N * TILE_SIZE / 2.0;
        for (int i = 0; i < TILE_N; i++)
            for (int k = 0; k < TILE_N; k++) {
                double x0 = -half + i * TILE_SIZE + TILE_GAP;
                double z0 = -half + k * TILE_SIZE + TILE_GAP;
                double x1 = x0 + TILE_SIZE - 2 * TILE_GAP;
                double z1 = z0 + TILE_SIZE - 2 * TILE_GAP;
                double v = 0.82 + rnd.nextDouble() * 0.12;
                Material m = new Material()
                        .setKD(new Double3(0.74 * v, 0.70 * v, 0.62 * v)).setKS(0.22).setShininess(80)
                        .setKA(new Double3(0.5 * v, 0.48 * v, 0.43 * v));
                out.add(new Polygon(new Point(x0, 0, z0), new Point(x1, 0, z0),
                        new Point(x1, 0, z1), new Point(x0, 0, z1)).setMaterial(m));
            }
        return out;
    }

    /**
     * Builds the still reflecting pool in the foreground: a flat mirror-water {@link Polygon}
     * inside a low stone rim, positioned to catch the leaning tower's reflection.
     *
     * @return the pool bodies
     */
    private static List<Intersectable> pool() {
        List<Intersectable> out = new ArrayList<>();
        double x0 = -300, x1 = 300, z0 = 150, z1 = 380;
        out.add(new Polygon(new Point(x0, WATER_Y, z0), new Point(x1, WATER_Y, z0),
                new Point(x1, WATER_Y, z1), new Point(x0, WATER_Y, z1)).setMaterial(WATER));
        double t = 12, ht = 9; // rim thickness / height
        box(out, x0 - t, 0, z0 - t, x1 + t, ht, z0, STONE);   // near rim (+? toward -z edge)
        box(out, x0 - t, 0, z1, x1 + t, ht, z1 + t, STONE);   // far rim
        box(out, x0 - t, 0, z0, x0, ht, z1, STONE);           // left rim
        box(out, x1, 0, z0, x1 + t, ht, z1, STONE);           // right rim
        return out;
    }

    /**
     * Adds a stylized Italian cypress at (cx, cz): a slim trunk cylinder topped by stacked
     * square-pyramid triangle tiers of decreasing width, with a little tone jitter.
     *
     * @param cx  trunk center x
     * @param cz  trunk center z
     * @param rnd the shared random source (height/tone jitter)
     * @return the tree bodies
     */
    private static List<Intersectable> cypress(double cx, double cz, Random rnd) {
        List<Intersectable> out = new ArrayList<>();
        double trunkH = 34 + rnd.nextDouble() * 16;
        double tierH = 70 + rnd.nextDouble() * 26;
        int tiers = 4;
        double baseHW = 30 + rnd.nextDouble() * 8;
        Material foliage = solid(0.05 + rnd.nextDouble() * 0.04, 0.27 + rnd.nextDouble() * 0.08, 0.13,
                0.7, 0.2, 40, 0.30);
        out.add(new Cylinder(6, new Ray(new Point(cx, 0, cz), Vector.AXIS_Y), trunkH).setMaterial(WOOD));
        for (int i = 0; i < tiers; i++) {
            double hw = baseHW * (1 - 0.21 * i);
            double baseY = trunkH + i * (tierH * 0.5);
            double apexY = baseY + tierH;
            Point ap = new Point(cx, apexY, cz);
            Point c0 = new Point(cx - hw, baseY, cz - hw), c1 = new Point(cx + hw, baseY, cz - hw);
            Point c2 = new Point(cx + hw, baseY, cz + hw), c3 = new Point(cx - hw, baseY, cz + hw);
            out.add(new Triangle(c0, c1, ap).setMaterial(foliage));
            out.add(new Triangle(c1, c2, ap).setMaterial(foliage));
            out.add(new Triangle(c2, c3, ap).setMaterial(foliage));
            out.add(new Triangle(c3, c0, ap).setMaterial(foliage));
        }
        return out;
    }

    /**
     * Scatters small pebbles (gravel/grass) across the open ground outside the pool footprint,
     * for ground texture and body count. Deterministic for the shared seed.
     *
     * @param rnd the shared random source
     * @return the pebble bodies
     */
    private static List<Intersectable> gravel(Random rnd) {
        List<Intersectable> out = new ArrayList<>();
        double span = TILE_N * TILE_SIZE / 2.0 - 20;
        for (int i = 0; i < GRAVEL; i++) {
            double x = (rnd.nextDouble() * 2 - 1) * span;
            double z = (rnd.nextDouble() * 2 - 1) * span;
            if (x > -312 && x < 312 && z > 138 && z < 392) continue; // skip the pool footprint
            double r = 2.5 + rnd.nextDouble() * 4.5;
            boolean grass = rnd.nextDouble() < 0.45;
            Material m = grass
                    ? solid(0.20, 0.34 + rnd.nextDouble() * 0.1, 0.14, 0.7, 0.1, 20, 0.35)
                    : solid(0.50 + rnd.nextDouble() * 0.15, 0.46, 0.40, 0.7, 0.2, 40, 0.45);
            out.add(new Sphere(new Point(x, r * 0.7, z), r).setMaterial(m));
        }
        return out;
    }

    /**
     * Builds the foreground props that stage reflection and refraction: clear-glass crystal orbs
     * and polished mirror spheres on stone pedestals, plus low shrubs and lamp posts (the lamp
     * glow spheres are emissive; the matching lights are added in {@link #scene(Geometries)}).
     *
     * @return the prop bodies
     */
    private static List<Intersectable> props() {
        List<Intersectable> out = new ArrayList<>();

        // glass crystal orbs (refraction) on stone pedestals, foreground in front of the pool
        // each row is {x, pedestalHeight, z, orbRadius}
        double[][] orbs = {{-200, 46, 470, 40}, {200, 52, 505, 42}, {0, 42, 545, 38}};
        for (double[] o : orbs) {
            out.add(new Cylinder(26, new Ray(new Point(o[0], 0, o[2]), Vector.AXIS_Y), o[1])
                    .setMaterial(STONE));
            out.add(new Sphere(new Point(o[0], o[1] + o[3], o[2]), o[3]).setMaterial(GLASS));
        }

        // mirror accent spheres (reflection) on pedestals, flanking the pool on the tiles
        // each row is {x, pedestalHeight, z, sphereRadius}
        double[][] mir = {{-345, 70, 210, 34}, {350, 64, 225, 32}};
        for (double[] mm : mir) {
            out.add(new Cylinder(24, new Ray(new Point(mm[0], 0, mm[2]), Vector.AXIS_Y), mm[1])
                    .setMaterial(STONE));
            out.add(new Sphere(new Point(mm[0], mm[1] + mm[3], mm[2]), mm[3]).setMaterial(MIRROR));
        }

        // low shrubs dotted near the planting
        double[][] bush = {{-250, -120}, {220, -160}, {-150, -330}, {330, 70}, {-360, 50}, {120, -400}};
        for (double[] b : bush) {
            out.add(new Sphere(new Point(b[0] + 22, 16, b[1] + 8), 20).setMaterial(BUSH));
            out.add(new Sphere(new Point(b[0] - 14, 18, b[1] - 10), 22).setMaterial(BUSH));
            out.add(new Sphere(new Point(b[0] + 4, 24, b[1] + 2), 18).setMaterial(BUSH));
        }

        // lamp posts: dark stone post + warm emissive glow sphere
        for (double[] l : LAMPS) {
            out.add(new Cylinder(5.5, new Ray(new Point(l[0], 0, l[1]), Vector.AXIS_Y), LAMP_H)
                    .setMaterial(STONE));
            out.add(new Sphere(new Point(l[0], LAMP_H + 9, l[1]), 12)
                    .setEmission(new Color(255, 184, 96))
                    .setMaterial(solid(0.25, 0.16, 0.07, 0.4, 0.1, 30, 0.4)));
        }

        // bunting: a string of little triangular pennants between two foreground lamp posts
        Point pa = new Point(LAMPS[4][0], LAMP_H - 6, LAMPS[4][1]);
        Point pb = new Point(LAMPS[5][0], LAMP_H - 6, LAMPS[5][1]);
        Vector span = pb.subtract(pa);
        int flags = 12;
        for (int i = 0; i < flags; i++) {
            double u0 = (double) i / flags, u1 = (double) (i + 1) / flags;
            Point t0 = isZero(u0) ? pa : pa.add(span.scale(u0));
            Point t1 = pa.add(span.scale(u1));
            Point tip = pa.add(span.scale((u0 + u1) / 2)).add(new Vector(0, -16, 0));
            Color c = (i % 3 == 0) ? new Color(210, 60, 50)
                    : (i % 3 == 1) ? new Color(70, 120, 200) : new Color(235, 200, 70);
            out.add(new Triangle(t0, t1, tip).setEmission(c.scale(0.35))
                    .setMaterial(solid(0.5, 0.5, 0.5, 0.4, 0.1, 30, 0.4)));
        }

        // an Italian tricolore flag on each bronze flag pole (green / white / red bands)
        Color[] tri = {new Color(70, 150, 78), new Color(240, 240, 230), new Color(206, 56, 50)};
        for (double[] pl : POLES) {
            double inward = pl[0] < 0 ? 1 : -1;     // flag hangs toward the piazza center
            double yTop = 322, yBot = 256, bandW = 26;
            for (int b = 0; b < 3; b++) {
                double xa = pl[0] + inward * b * bandW;
                double xb = pl[0] + inward * (b + 1) * bandW;
                Point a = new Point(xa, yTop, pl[1]), bb = new Point(xb, yTop, pl[1]);
                Point cc = new Point(xb, yBot, pl[1]), dd = new Point(xa, yBot, pl[1]);
                Color e = tri[b].scale(0.5);
                out.add(new Triangle(a, bb, cc).setEmission(e).setMaterial(CLOTH));
                out.add(new Triangle(a, cc, dd).setEmission(e).setMaterial(CLOTH));
            }
        }
        return out;
    }

    /**
     * Builds the warm dusk sky furniture: a big emissive sun disk low on the horizon and a few
     * soft cloud puffs (clusters of faint emissive spheres) high and far behind the tower.
     *
     * @return the sky bodies
     */
    private static List<Intersectable> sky() {
        List<Intersectable> out = new ArrayList<>();
        // the low sun, clearly visible to the left of the tower (its light is the directional sun)
        out.add(new Sphere(new Point(-470, 338, -880), 188)
                .setEmission(new Color(255, 233, 178))
                .setMaterial(solid(0.2, 0.2, 0.2, 0, 0, 1, 0.2)));
        double[][] clouds = {{-560, 560, -1250}, {380, 650, -1400}, {-120, 500, -1150}};
        for (double[] c : clouds)
            for (int i = 0; i < 4; i++) {
                Color glow = new Color(255, 205, 158).scale(0.78);
                out.add(new Sphere(new Point(c[0] + i * 72 - 100, c[1] + (i % 2) * 26, c[2]), 62)
                        .setEmission(glow).setMaterial(solid(0.3, 0.3, 0.3, 0.1, 0, 1, 0.3)));
            }
        return out;
    }

    // ============================ Scene & camera ============================

    /**
     * Wraps the given body collection in the golden-hour scene: warm sky background and ambient,
     * a low directional sun (the long hard shadow), two warm facade up-light spots, and three
     * warm lantern point lights — five lights spanning all supported types, the point/spot ones
     * sized for soft shadows.
     *
     * @param geometries the flat or hierarchical collection to render
     * @return the ready-to-render scene
     */
    public static Scene scene(Geometries geometries) {
        Scene scene = new Scene("Pisa Golden Hour")
                .setBackground(new Color(255, 172, 112))
                .setAmbientLight(new AmbientLight(new Color(156, 133, 108)));
        scene.setGeometries(geometries);

        // the low golden sun: strong, warm, casts the tower's long shadow (hard — no size)
        scene.lights.add(new DirectionalLight(new Color(610, 430, 250), SUN_DIR));

        // two warm spot up-lights washing the camera-facing facade (kept hard as bright fill
        // against the backlight; the soft-shadow showcase comes from the lantern point lights)
        scene.lights.add(new SpotLight(new Color(540, 372, 208),
                new Point(150, 12, 250), new Vector(-0.55, 1, -0.6))
                .setKl(2E-4).setKq(5E-8));
        scene.lights.add(new SpotLight(new Color(470, 332, 196),
                new Point(-175, 12, 258), new Vector(0.5, 1, -0.6))
                .setKl(2E-4).setKq(5E-8));

        // three warm lantern point lights at lamp-post glows (soft)
        int[] lit = {0, 1, 4};
        for (int idx : lit) {
            Point glow = new Point(LAMPS[idx][0], LAMP_H + 9, LAMPS[idx][1]);
            scene.lights.add(new PointLight(new Color(330, 214, 110), glow)
                    .setKl(3E-4).setKq(4E-7).setSize(LANTERN_SIZE));
        }
        return scene;
    }

    /**
     * The shared timing-matrix camera: identical for every run apart from the CBR switch (set by
     * the test) and the thread count. Soft shadows are enabled (the MP1 mode is active and the
     * same across all timing cells); antialiasing is left off to keep the baseline tractable.
     *
     * @param scene   the scene to render
     * @param threads the thread count (0 single-threaded, -2 auto, etc.)
     * @return a configured camera builder (CBR still to be set by the caller)
     */
    public static Camera.Builder camera(Scene scene, int threads) {
        return baseCamera(scene, MATRIX_W, MATRIX_H, threads)
                .setSoftShadowSampler(MATRIX_SS, BeamSampler.Shape.CIRCLE, BeamSampler.Pattern.GRID);
    }

    /**
     * The high-quality beauty camera: full resolution, antialiasing, and dense soft shadows,
     * multithreaded. Soft shadows can be turned off (sampler edge 1) for a hard-vs-soft pair.
     *
     * @param scene the scene to render
     * @param soft  whether to enable the dense soft-shadow sampler
     * @return a configured camera builder (CBR still to be set by the caller)
     */
    public static Camera.Builder beautyCamera(Scene scene, boolean soft) {
        Camera.Builder b = baseCamera(scene, BEAUTY_W, BEAUTY_H, -2)
                .setAntialiasingBeamSampler(BEAUTY_AA, BeamSampler.Shape.SQUARE, BeamSampler.Pattern.GRID)
                .setDebugPrint(0.5);
        if (soft) b.setSoftShadowSampler(BEAUTY_SS, BeamSampler.Shape.CIRCLE, BeamSampler.Pattern.JITTERED);
        return b;
    }

    /**
     * Shared camera scaffold (location, aim, view plane derived from the field of view).
     *
     * @param scene   the scene to render
     * @param nx      horizontal resolution
     * @param ny      vertical resolution
     * @param threads thread count
     * @return the partially-configured builder
     */
    private static Camera.Builder baseCamera(Scene scene, int nx, int ny, int threads) {
        double vpH = 2 * VP_DIST * Math.tan(Math.toRadians(FOV_DEG / 2));
        double vpW = vpH * ((double) nx / ny);
        return Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(CAM_POS)
                .setDirection(CAM_TARGET, Vector.AXIS_Y)
                .setVpDistance(VP_DIST).setVpSize(vpW, vpH)
                .setResolution(nx, ny)
                .setMultithreading(threads);
    }

    // ============================ Small geometric helpers ============================

    /**
     * Computes the tower's tilt axis from the configured lean angle and azimuth.
     *
     * @return the unit lean axis of the tower (vertical tilted by {@link #LEAN_DEG})
     */
    private static Vector leanAxis() {
        double t = Math.toRadians(LEAN_DEG), f = Math.toRadians(LEAN_AZ_DEG);
        return new Vector(Math.sin(t) * Math.cos(f), Math.cos(t), Math.sin(t) * Math.sin(f)).normalize();
    }

    /**
     * Builds a stable pair of axes spanning the ring plane of the tilted tower.
     *
     * @param a the unit lean axis
     * @return an orthonormal basis {e1, e2} spanning the plane perpendicular to {@code a}
     */
    private static Vector[] radialBasis(Vector a) {
        Vector ref = Math.abs(a.dotProduct(Vector.AXIS_X)) < 0.9 ? Vector.AXIS_X : Vector.AXIS_Z;
        Vector e1 = a.crossProduct(ref).normalize();
        Vector e2 = a.crossProduct(e1).normalize();
        return new Vector[]{e1, e2};
    }

    /**
     * Combines the radial basis at a given angle without ever scaling a vector by zero (which the
     * engine forbids): the cos/sin component that rounds to zero is simply dropped.
     *
     * @param e   the radial basis {e1, e2}
     * @param ang the angle around the ring
     * @return the unit radial direction at {@code ang}
     */
    private static Vector radial(Vector[] e, double ang) {
        double c = Math.cos(ang), s = Math.sin(ang);
        Vector r = null;
        if (!isZero(c)) r = e[0].scale(c);
        if (!isZero(s)) r = (r == null) ? e[1].scale(s) : r.add(e[1].scale(s));
        return r; // cos and sin are never both zero
    }

    /**
     * Advances a point along a unit direction, safely handling a zero distance.
     *
     * @param base origin point
     * @param a    unit direction
     * @param dist distance to travel (zero handled without a zero-vector scale)
     * @return {@code base + a*dist}
     */
    private static Point along(Point base, Vector a, double dist) {
        return isZero(dist) ? base : base.add(a.scale(dist));
    }

    /**
     * Adds an axis-aligned stone box (its five visible faces, each two triangles) to a body list.
     *
     * @param out the body list to populate
     * @param x0  min x
     * @param y0  min y (base)
     * @param z0  min z
     * @param x1  max x
     * @param y1  max y (top)
     * @param z1  max z
     * @param m   the material
     */
    private static void box(List<Intersectable> out, double x0, double y0, double z0,
                            double x1, double y1, double z1, Material m) {
        Point b0 = new Point(x0, y0, z0), b1 = new Point(x1, y0, z0),
                b2 = new Point(x1, y0, z1), b3 = new Point(x0, y0, z1);
        Point t0 = new Point(x0, y1, z0), t1 = new Point(x1, y1, z0),
                t2 = new Point(x1, y1, z1), t3 = new Point(x0, y1, z1);
        quad(out, b3, b2, t2, t3, m); // +z
        quad(out, b1, b0, t0, t1, m); // -z
        quad(out, b0, b3, t3, t0, m); // -x
        quad(out, b2, b1, t1, t2, m); // +x
        quad(out, t3, t2, t1, t0, m); // top
    }

    /**
     * Adds a planar quad as two triangles (always coplanar, so never rejected by Polygon).
     *
     * @param out the body list to populate
     * @param a   first corner
     * @param b   second corner
     * @param c   third corner
     * @param d   fourth corner
     * @param m   the material
     */
    private static void quad(List<Intersectable> out, Point a, Point b, Point c, Point d, Material m) {
        out.add(new Triangle(a, b, c).setMaterial(m));
        out.add(new Triangle(a, c, d).setMaterial(m));
    }

    /**
     * Builds a matte/glossy material whose diffuse and ambient terms carry the given base color.
     *
     * @param r  red base in [0,1]
     * @param g  green base in [0,1]
     * @param b  blue base in [0,1]
     * @param kd diffuse strength
     * @param ks specular strength
     * @param sh shininess exponent
     * @param ka ambient strength
     * @return the configured material
     */
    private static Material solid(double r, double g, double b,
                                  double kd, double ks, int sh, double ka) {
        return new Material()
                .setKD(new Double3(r * kd, g * kd, b * kd))
                .setKS(ks).setShininess(sh)
                .setKA(new Double3(r * ka, g * ka, b * ka));
    }
}
