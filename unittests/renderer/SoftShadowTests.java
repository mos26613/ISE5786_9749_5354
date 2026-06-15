package renderer;

import org.junit.jupiter.api.Test;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import sampling.BeamSampler;
import scene.Scene;

/**
 * Moonlit Zen Garden — a deliberately composed soft-shadow showcase. A still reflecting
 * pool, three stylized conifer trees built from stacked triangle tiers, a glass crystal on
 * a stone pedestal (the focal point), a cluster of river stones, and two stone lanterns
 * (the warm light sources) sit on smooth ground under a cool "moonlight" spot. The trees
 * cast long soft shadows across the open ground, the pool mirrors them, and the crystal
 * refracts the trees behind it.
 * <p>
 * Rendered as a hard-vs-soft pair from the same view: point-sized lights (crisp shadows)
 * vs. sized area lights sampled by a beam (soft penumbrae). Each render is multithreaded
 * and prints its wall time.
 */
class SoftShadowTests {

    /** Default constructor to satisfy the Javadoc generator. */
    SoftShadowTests() { /* to satisfy Javadoc generator */ }

    /** Horizontal resolution of the showcase images. */
    private static final int NX = 1000;
    /** Vertical resolution of the showcase images. */
    private static final int NY = 700;
    /** Diameter of the moonlight spot's emitting area when soft shadows are enabled. */
    private static final double SOFT_LIGHT_SIZE = 160;
    /** Diameter of each lantern's emitting area when soft shadows are enabled. */
    private static final double LANTERN_SIZE = 55;
    /** Samples per axis of the soft-shadow sampling disk (17 -> ~225 circular samples). */
    private static final int SS_SAMPLES = 17;
    /** Samples per axis of the camera's antialiasing sampling pattern. */
    private static final int AA_SAMPLES = 9;

    /**
     * Builds a glossy/matte material whose diffuse and ambient coefficients carry the
     * given base color.
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

    /**
     * Adds a stylized conifer at (cx, cz): a slim cylinder trunk topped by {@code nTiers}
     * stacked, overlapping square-pyramid "skirts" of decreasing width — a tree built from
     * triangles.
     *
     * @param scene    the scene to populate
     * @param cx       trunk center x
     * @param cz       trunk center z
     * @param trunkH   trunk height (also the bottom tier's base height)
     * @param trunkR   trunk radius
     * @param baseHW   half-width of the bottom (widest) tier
     * @param tierH    height of one tier (tiers overlap by ~45%)
     * @param nTiers   number of tiers
     * @param foliage  foliage material
     * @param trunkMat trunk material
     */
    private static void addConifer(Scene scene, double cx, double cz, double trunkH, double trunkR,
                                   double baseHW, double tierH, int nTiers,
                                   Material foliage, Material trunkMat) {
        scene.geometries.add(new Cylinder(trunkR,
                new Ray(new Point(cx, 0, cz), Vector.AXIS_Y), trunkH).setMaterial(trunkMat));

        for (int i = 0; i < nTiers; i++) {
            double hw = baseHW * (1 - 0.22 * i);
            double baseY = trunkH + i * (tierH * 0.55);
            double apexY = baseY + tierH;
            Point a = new Point(cx, apexY, cz);
            Point c0 = new Point(cx - hw, baseY, cz - hw);
            Point c1 = new Point(cx + hw, baseY, cz - hw);
            Point c2 = new Point(cx + hw, baseY, cz + hw);
            Point c3 = new Point(cx - hw, baseY, cz + hw);
            scene.geometries.add(
                    new Triangle(c0, c1, a).setMaterial(foliage),
                    new Triangle(c1, c2, a).setMaterial(foliage),
                    new Triangle(c2, c3, a).setMaterial(foliage),
                    new Triangle(c3, c0, a).setMaterial(foliage));
        }
    }

    /**
     * Adds a stone lantern at (x, z): a dark cylinder post capped by an emissive glow
     * sphere, plus a matching warm point light at the glow so the lantern lights the scene.
     *
     * @param scene     the scene to populate
     * @param x         lantern x
     * @param z         lantern z
     * @param postH     post height
     * @param lightSize the glow's area diameter (0 for a hard shadow)
     */
    private static void addLantern(Scene scene, double x, double z, double postH, double lightSize) {
        double glowR = 20, glowY = postH + glowR * 0.4;
        scene.geometries.add(new Cylinder(13d,
                new Ray(new Point(x, 0, z), Vector.AXIS_Y), postH)
                .setMaterial(solid(0.30, 0.30, 0.33, 0.7, 0.2, 40, 0.45)));
        scene.geometries.add(new Sphere(new Point(x, glowY, z), glowR)
                .setEmission(new Color(255, 172, 84))
                .setMaterial(new Material().setKD(new Double3(0.2, 0.13, 0.06)).setKS(0.1).setShininess(40)));
        scene.lights.add(new PointLight(new Color(345, 228, 120), new Point(x, glowY, z))
                .setKl(3E-4).setKq(4E-7).setSize(lightSize));
    }

    /**
     * Adds a quad as two triangles (always planar) with the given material.
     *
     * @param scene the scene to populate
     * @param a     first corner
     * @param b     second corner
     * @param c     third corner
     * @param d     fourth corner
     * @param mat   the material
     */
    private static void addQuad(Scene scene, Point a, Point b, Point c, Point d, Material mat) {
        scene.geometries.add(
                new Triangle(a, b, c).setMaterial(mat),
                new Triangle(a, c, d).setMaterial(mat));
    }

    /**
     * Adds a box centered at (cx, cz) that flares in width (x) only: bottom half-width
     * {@code hwB}, top half-width {@code hwT}, constant half-depth {@code hd}, rising from
     * {@code cy} by {@code height}. Built from quads so every face stays planar.
     *
     * @param scene  the scene to populate
     * @param cx     center x
     * @param cy     base height
     * @param cz     center z
     * @param hwB    bottom half-width
     * @param hwT    top half-width
     * @param hd     half-depth
     * @param height box height
     * @param mat    the material
     */
    private static void addBox(Scene scene, double cx, double cy, double cz,
                               double hwB, double hwT, double hd, double height, Material mat) {
        double top = cy + height;
        Point b0 = new Point(cx - hwB, cy, cz - hd), b1 = new Point(cx + hwB, cy, cz - hd),
                b2 = new Point(cx + hwB, cy, cz + hd), b3 = new Point(cx - hwB, cy, cz + hd);
        Point t0 = new Point(cx - hwT, top, cz - hd), t1 = new Point(cx + hwT, top, cz - hd),
                t2 = new Point(cx + hwT, top, cz + hd), t3 = new Point(cx - hwT, top, cz + hd);
        addQuad(scene, b3, b2, t2, t3, mat); // front (+z)
        addQuad(scene, b1, b0, t0, t1, mat); // back (-z)
        addQuad(scene, b0, b3, t3, t0, mat); // left (-x)
        addQuad(scene, b2, b1, t1, t2, mat); // right (+x)
        addQuad(scene, t3, t2, t1, t0, mat); // top
    }

    /**
     * Adds a cylindrical limb from {@code from} to {@code to} with the given radius.
     *
     * @param scene the scene to populate
     * @param from  the start point
     * @param to    the end point
     * @param r     the radius
     * @param mat   the material
     */
    private static void addLimb(Scene scene, Point from, Point to, double r, Material mat) {
        Vector dir = to.subtract(from);
        scene.geometries.add(new Cylinder(r, new Ray(from, dir), dir.length()).setMaterial(mat));
    }

    /**
     * Builds a LEGO-style minifigure seated in a meditation (lotus) pose, facing +z, from
     * boxes (triangles), cylinders and spheres: a wide crossed-legs lap with knees, a
     * trapezoidal robe torso, arms resting hands on the knees, and a cylindrical head with
     * a stud. Its base sits at {@code baseY}.
     *
     * @param scene the scene to populate
     * @param cx    center x
     * @param baseY base height (top of the pedestal)
     * @param cz    center z
     * @param u     unit scale
     * @param robe  torso/arm material
     * @param skin  head/hand material (LEGO yellow)
     * @param legs  lap/knee/shin material
     * @param shoes shoe (foot) material
     */
    private static void addMinifig(Scene scene, double cx, double baseY, double cz, double u,
                                   Material robe, Material skin, Material legs, Material shoes) {
        // crossed-legs lap (wide, low; shifted forward so it doesn't jut out back)
        double lapH = 0.75 * u;
        addBox(scene, cx, baseY, cz + 0.2 * u, 1.5 * u, 1.35 * u, 0.8 * u, lapH, legs);
        double lapTop = baseY + lapH;
        // prominent knees + shins crossing in front (the lotus fold) ------------
        Point lKnee = new Point(cx - 1.35 * u, baseY + 0.4 * u, cz + 0.7 * u);
        Point rKnee = new Point(cx + 1.35 * u, baseY + 0.4 * u, cz + 0.7 * u);
        scene.geometries.add(new Sphere(lKnee, 0.55 * u).setMaterial(legs));
        scene.geometries.add(new Sphere(rKnee, 0.55 * u).setMaterial(legs));
        addLimb(scene, lKnee, new Point(cx + 0.45 * u, baseY + 0.3 * u, cz + 1.3 * u), 0.4 * u, legs);
        addLimb(scene, rKnee, new Point(cx - 0.45 * u, baseY + 0.3 * u, cz + 1.3 * u), 0.4 * u, legs);
        // shoes (feet) continuing the crossed shins, capped with rounded toes ---
        Point lShinEnd = new Point(cx + 0.45 * u, baseY + 0.3 * u, cz + 1.3 * u);
        Point rShinEnd = new Point(cx - 0.45 * u, baseY + 0.3 * u, cz + 1.3 * u);
        Point lFootTip = new Point(cx + 0.78 * u, baseY + 0.5 * u, cz + 1.5 * u);
        Point rFootTip = new Point(cx - 0.78 * u, baseY + 0.5 * u, cz + 1.5 * u);
        addLimb(scene, lShinEnd, lFootTip, 0.34 * u, shoes);
        addLimb(scene, rShinEnd, rFootTip, 0.34 * u, shoes);
        scene.geometries.add(new Sphere(lFootTip, 0.34 * u).setMaterial(shoes));
        scene.geometries.add(new Sphere(rFootTip, 0.34 * u).setMaterial(shoes));

        // trapezoidal torso (robe) ----------------------------------------------
        double torsoH = 1.5 * u;
        addBox(scene, cx, lapTop, cz, 1.0 * u, 0.8 * u, 0.6 * u, torsoH, robe);
        double shoulderY = lapTop + torsoH;

        // shirt detailing on the torso front face (z = cz + 0.6u) ---------------
        double frontZ = cz + 0.6 * u;
        Material collar = solid(0.88, 0.85, 0.70, 0.7, 0.2, 60, 0.45);   // cream collar
        Material button = solid(0.20, 0.12, 0.07, 0.7, 0.2, 40, 0.45);   // dark buttons
        // collar: a downward-opening V meeting just below the neck
        Point collarApex = new Point(cx, shoulderY - 0.12 * u, frontZ + 0.02 * u);
        addLimb(scene, collarApex, new Point(cx - 0.42 * u, shoulderY - 0.52 * u, frontZ + 0.02 * u), 0.07 * u, collar);
        addLimb(scene, collarApex, new Point(cx + 0.42 * u, shoulderY - 0.52 * u, frontZ + 0.02 * u), 0.07 * u, collar);
        // buttons: a vertical column down the chest
        for (int i = 0; i < 3; i++)
            scene.geometries.add(new Sphere(
                    new Point(cx, shoulderY - (0.7 + 0.32 * i) * u, frontZ), 0.08 * u).setMaterial(button));

        // arms (shoulders -> hands resting on the knees) + yellow hands ---------
        Point lShoulder = new Point(cx - 0.85 * u, shoulderY - 0.25 * u, cz + 0.25 * u);
        Point rShoulder = new Point(cx + 0.85 * u, shoulderY - 0.25 * u, cz + 0.25 * u);
        Point lHand = new Point(cx - 1.35 * u, baseY + 0.85 * u, cz + 0.7 * u);
        Point rHand = new Point(cx + 1.35 * u, baseY + 0.85 * u, cz + 0.7 * u);
        addLimb(scene, lShoulder, lHand, 0.26 * u, robe);
        addLimb(scene, rShoulder, rHand, 0.26 * u, robe);
        scene.geometries.add(new Sphere(lHand, 0.32 * u).setMaterial(skin));
        scene.geometries.add(new Sphere(rHand, 0.32 * u).setMaterial(skin));

        // neck + head + stud (yellow) -------------------------------------------
        scene.geometries.add(new Cylinder(0.34 * u,
                new Ray(new Point(cx, shoulderY, cz), Vector.AXIS_Y), 0.22 * u).setMaterial(skin));
        double headBase = shoulderY + 0.22 * u;
        scene.geometries.add(new Cylinder(0.62 * u,
                new Ray(new Point(cx, headBase, cz), Vector.AXIS_Y), 0.8 * u).setMaterial(skin));
        double headTop = headBase + 0.8 * u;
        scene.geometries.add(new Cylinder(0.26 * u,
                new Ray(new Point(cx, headTop, cz), Vector.AXIS_Y), 0.16 * u).setMaterial(skin));

        // face: eyes + mouth on the head, set on the surface toward the camera --
        Material face = solid(0.05, 0.04, 0.05, 0.5, 0.1, 30, 0.5);
        Vector faceDir = new Vector(0.6, 0, 0.85).normalize();
        Vector facePerp = new Vector(-0.85, 0, 0.6).normalize();
        Point eyeC = new Point(cx, headBase + 0.5 * u, cz).add(faceDir.scale(0.58 * u));
        scene.geometries.add(new Sphere(eyeC.add(facePerp.scale(0.24 * u)), 0.10 * u).setMaterial(face));
        scene.geometries.add(new Sphere(eyeC.add(facePerp.scale(-0.24 * u)), 0.10 * u).setMaterial(face));
        Point mouthC = new Point(cx, headBase + 0.26 * u, cz).add(faceDir.scale(0.58 * u));
        addLimb(scene, mouthC.add(facePerp.scale(0.17 * u)), mouthC.add(facePerp.scale(-0.17 * u)), 0.05 * u, face);
    }

    /**
     * Builds the Zen Garden scene. A {@code lightSize} of {@code 0} keeps the lights
     * point-sized (hard shadows); positive values turn them into area lights.
     *
     * @param lightSize    the moonlight spot's area diameter
     * @param lanternSize  the lanterns' area diameter
     * @return the populated scene
     */
    private Scene buildScene(double lightSize, double lanternSize) {
        Scene scene = new Scene("Moonlit Zen Garden")
                .setBackground(new Color(16, 20, 38))
                .setAmbientLight(new AmbientLight(new Color(42, 46, 60)));

        // smooth ground (matte -> clean canvas for the soft shadows) ------------
        scene.geometries.add(new Plane(new Point(0, 0, 0), new Vector(0, 1, 0))
                .setMaterial(new Material().setKD(new Double3(0.50, 0.49, 0.45))
                        .setKS(0.08).setShininess(30).setKA(new Double3(0.62, 0.62, 0.66))));

        // still reflecting pool (kR only), a hair above the ground --------------
        scene.geometries.add(new Polygon(
                new Point(-260, 1, 110), new Point(90, 1, 110),
                new Point(90, 1, -60), new Point(-260, 1, -60))
                .setMaterial(new Material().setKD(new Double3(0.02, 0.04, 0.06))
                        .setKS(0.4).setShininess(220).setKA(new Double3(0.02, 0.03, 0.05))
                        .setKR(new Double3(0.78, 0.80, 0.85))));

        // stone pillar rising from the pond (right of center) + meditating minifig
        scene.geometries.add(new Cylinder(40d,
                new Ray(new Point(-35, 0, 25), Vector.AXIS_Y), 48d)
                .setMaterial(solid(0.42, 0.42, 0.46, 0.7, 0.25, 60, 0.5)));
        addMinifig(scene, -35, 48, 25, 26,
                solid(0.92, 0.45, 0.10, 0.75, 0.3, 80, 0.35),   // saffron robe
                solid(0.96, 0.78, 0.12, 0.7, 0.4, 100, 0.40),   // LEGO yellow head/hands
                solid(0.16, 0.34, 0.78, 0.7, 0.25, 80, 0.40),   // denim-blue pants
                solid(0.42, 0.23, 0.10, 0.7, 0.20, 60, 0.40));  // brown shoes

        // three conifers (back-left stand), varying heights ---------------------
        Material trunk = solid(0.30, 0.19, 0.10, 0.7, 0.1, 20, 0.4);
        addConifer(scene, -220, -270, 48, 9, 78, 98, 3,
                solid(0.06, 0.32, 0.15, 0.7, 0.2, 40, 0.32), trunk);
        addConifer(scene, -85, -330, 40, 8, 62, 82, 3,
                solid(0.07, 0.36, 0.17, 0.7, 0.2, 40, 0.32), trunk);
        addConifer(scene, -310, -120, 34, 7, 54, 76, 2,
                solid(0.05, 0.30, 0.14, 0.7, 0.2, 40, 0.32), trunk);

        // glass crystal on a stone pedestal (focal point; refraction) -----------
        scene.geometries.add(new Cylinder(50d,
                new Ray(new Point(70, 0, -70), Vector.AXIS_Y), 22d)
                .setMaterial(solid(0.40, 0.40, 0.43, 0.7, 0.25, 60, 0.5)));
        scene.geometries.add(new Sphere(new Point(70, 74, -70), 52d)
                .setMaterial(new Material().setKD(new Double3(0.05, 0.07, 0.08))
                        .setKS(0.7).setShininess(300).setKA(new Double3(0.04, 0.05, 0.06))
                        .setKT(new Double3(0.65, 0.78, 0.85))));

        // two stone lanterns (warm motivated lights) ----------------------------
        addLantern(scene, 250, 60, 82, lanternSize);
        addLantern(scene, -315, -245, 78, lanternSize);

        // cluster of river stones in the open foreground ------------------------
        Material stone = solid(0.56, 0.56, 0.60, 0.7, 0.3, 80, 0.62);
        scene.geometries.add(new Sphere(new Point(140, 30, 120), 30d).setMaterial(stone));
        scene.geometries.add(new Sphere(new Point(200, 22, 95), 22d).setMaterial(stone));
        scene.geometries.add(new Sphere(new Point(95, 16, 160), 16d).setMaterial(stone));

        // cool "moonlight" key spot, high upper-left, low-ish angle -------------
        scene.lights.add(new SpotLight(new Color(250, 285, 385),
                new Point(-450, 520, 220), new Vector(450, -480, -330))
                .setKl(1E-4).setKq(1E-7).setSize(lightSize));
        // dim cool fill from the camera side: lifts camera-facing surfaces and
        // adds gentle cross soft-shadows (keeps the night mood, not a 2nd key)
        scene.lights.add(new SpotLight(new Color(105, 130, 180),
                new Point(470, 430, 380), new Vector(-470, -360, -460))
                .setKl(1E-4).setKq(1E-7).setSize(lightSize));

        return scene;
    }

    /**
     * Renders the scene to an image, multithreaded and timed.
     *
     * @param name        the output image file name
     * @param lightSize   the moonlight spot's area diameter (0 for hard shadows)
     * @param lanternSize the lanterns' area diameter (0 for hard shadows)
     * @param softSampler the soft-shadow sampler, or {@code null} to leave shadows hard
     */
    private void render(String name, double lightSize, double lanternSize, BeamSampler softSampler) {
        Scene scene = buildScene(lightSize, lanternSize);

        double fov = 40, vpDist = 600;
        double vpH = 2 * vpDist * Math.tan(Math.toRadians(fov / 2));
        double vpW = vpH * ((double) NX / NY);

        Camera.Builder builder = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(350, 320, 600))
                .setDirection(new Point(-35, 80, -35), Vector.AXIS_Y)
                .setVpDistance(vpDist).setVpSize(vpW, vpH)
                .setResolution(NX, NY)
                .setAntialiasingBeamSampler(AA_SAMPLES, BeamSampler.Shape.SQUARE, BeamSampler.Pattern.GRID)
                .setMultithreading(-2)
                .setDebugPrint(0.1);
        if (softSampler != null) builder.setSoftShadowSampler(softSampler);

        Camera camera = builder.build();
        camera.renderImage();
        camera.writeToImage(name);
    }

    /** Hard shadows: point-sized lights give crisp shadow edges. */
    @Test
    void zenGardenHard() {
        render("zenGardenHard", 0, 0, null);
    }

    /** Soft shadows: the moonlight and lanterns become area lights sampled by a beam. */
    @Test
    void zenGardenSoft() {
        render("zenGardenSoft", SOFT_LIGHT_SIZE, LANTERN_SIZE,
                new BeamSampler(SS_SAMPLES, BeamSampler.Shape.CIRCLE, BeamSampler.Pattern.GRID));
    }
}
