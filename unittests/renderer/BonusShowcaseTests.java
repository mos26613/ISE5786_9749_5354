package renderer;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import parser.JsonSceneParser;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

/**
 * Bonus showcase image "The Crystal Gallery": a single rich scene that contains
 * more than ten bodies, uses every implemented primitive type (Sphere, Plane via
 * tiled Polygons, Triangle, Polygon, Cylinder, Tube) and demonstrates every
 * effect implemented through stage 8 (ambient, emission, Phong diffuse/specular,
 * directional + point + spot lights, hard and partial shadows, reflection and
 * transparency).
 * <p>
 * No closed body mixes transparency and reflection: the glass sphere, the
 * translucent tube and the dice are transparency-only; the chrome sphere and the
 * mirror are reflection-only.
 */
class BonusShowcaseTests {

   /** Default constructor to satisfy the JavaDoc generator. */
   BonusShowcaseTests() { /* to satisfy JavaDoc generator */ }

   /** Uniform world scale so the scene spans hundreds of units (comfortable for the fixed secondary-ray DELTA). */
   private static final double S = 50.0;

   /**
    * Builds a scaled point from compact "design" coordinates.
    * @param x design x
    * @param y design y
    * @param z design z
    * @return the point multiplied by the world scale {@link #S}
    */
   private static Point p(double x, double y, double z) {
      return new Point(x * S, y * S, z * S);
   }

   /**
    * Builds a material whose diffuse and ambient coefficients carry the given
    * base color (so the body shows that hue under white light and ambient).
    * @param r   red component in [0,1]
    * @param g   green component in [0,1]
    * @param b   blue component in [0,1]
    * @param kd  diffuse strength
    * @param ks  specular strength (white highlight)
    * @param sh  shininess exponent
    * @param ka  ambient strength
    * @return the configured material
    */
   private static Material solid(double r, double g, double b,
                                 double kd, double ks, int sh, double ka) {
      return new Material()
         .setKD(new Double3(r * kd, g * kd, b * kd))
         .setKS(ks).setShininess(sh)
         .setKA(new Double3(r * ka, g * ka, b * ka));
   }

   /** Produce the Crystal Gallery showcase image. */
   @Test
   void crystalGallery() {
      Scene scene = new Scene("Crystal Gallery")
         .setBackground(new Color(30, 34, 48))
         .setAmbientLight(new AmbientLight(new Color(140, 148, 172)));

      addCheckerFloor(scene);
      addFramedMirror(scene);

      // glass sphere (transparency only, tinted) ------------------------------
      scene.geometries.add(new Sphere(p(-5.6, 1.7, 2.0), 1.7 * S)
         .setMaterial(new Material().setKD(new Double3(0.06, 0.07, 0.08))
            .setKS(0.7).setShininess(300).setKA(new Double3(0.05, 0.06, 0.07))
            .setKT(new Double3(0.62, 0.72, 0.85))));

      // chrome mirror sphere (reflection only) --------------------------------
      scene.geometries.add(new Sphere(p(7.8, 1.6, -2.4), 1.6 * S)
         .setMaterial(new Material().setKD(new Double3(0.05, 0.05, 0.06))
            .setKS(0.6).setShininess(300).setKA(new Double3(0.04, 0.04, 0.05))
            .setKR(0.85)));

      // glossy crimson sphere -------------------------------------------------
      scene.geometries.add(new Sphere(p(-1.4, 1.0, 3.6), 1.0 * S)
         .setMaterial(solid(0.85, 0.10, 0.14, 0.7, 0.6, 200, 0.3)));

      // glowing orb (emissive; also lit from the warm point light) ------------
      scene.geometries.add(new Sphere(p(-0.2, 0.62, 4.3), 0.62 * S)
         .setEmission(new Color(255, 158, 64))
         .setMaterial(new Material().setKD(new Double3(0.2, 0.16, 0.08))
            .setKS(0.2).setShininess(60)));

      // glossy teal sphere ----------------------------------------------------
      scene.geometries.add(new Sphere(p(-9.0, 0.95, -0.8), 0.95 * S)
         .setMaterial(solid(0.05, 0.55, 0.55, 0.7, 0.5, 160, 0.35)));

      // gold pedestal cylinder (capped, solid) --------------------------------
      scene.geometries.add(new Cylinder(1.1 * S,
         new Ray(p(11.0, 0, -4.6), new Vector(0, 1, 0)), 2.3 * S)
         .setMaterial(solid(0.83, 0.66, 0.22, 0.7, 0.7, 200, 0.35)));

      // purple translucent tube (infinite pipe; transparency only) ------------
      scene.geometries.add(new Tube(1.3 * S,
         new Ray(p(-12.2, 0, -4.6), new Vector(0, 1, 0)))
         .setMaterial(new Material().setKD(new Double3(0.12, 0.07, 0.18))
            .setKS(0.6).setShininess(160).setKA(new Double3(0.08, 0.05, 0.12))
            .setKT(new Double3(0.45, 0.26, 0.70))));

      // emerald tetrahedron (4 triangles) -------------------------------------
      Point ta = p(-4.7, 0, -4.7), tb = p(-2.1, 0, -5.0),
            tc = p(-3.4, 0, -6.5), tApex = p(-3.4, 2.4, -5.5);
      Material emerald = solid(0.10, 0.70, 0.35, 0.7, 0.6, 180, 0.3);
      scene.geometries.add(
         new Triangle(ta, tb, tApex).setMaterial(emerald),
         new Triangle(tb, tc, tApex).setMaterial(emerald),
         new Triangle(tc, ta, tApex).setMaterial(emerald),
         new Triangle(ta, tc, tb).setMaterial(emerald));

      // half-transparent dice resting on a corner -----------------------------
      Material diceBody = new Material().setKD(new Double3(0.22, 0.32, 0.44))
         .setKS(0.6).setShininess(220).setKA(new Double3(0.14, 0.19, 0.25))
         .setKT(new Double3(0.42, 0.58, 0.76));
      Material pipMat = solid(0.06, 0.06, 0.09, 0.7, 0.5, 120, 0.4);
      addDie(scene, 5.4, 2.0, 1.2, 45, 32, diceBody, pipMat);

      // lights ----------------------------------------------------------------
      scene.lights.add(new DirectionalLight(new Color(470, 422, 340),
         new Vector(-0.5, -0.85, -0.35)));
      scene.lights.add(new SpotLight(new Color(110, 300, 460),
         p(-7.5, 9.0, 6.0), new Vector(4.5, -8.0, -6.5))
         .setKl(3E-4).setKq(1.2E-7));
      scene.lights.add(new PointLight(new Color(680, 400, 175),
         p(-0.2, 1.0, 4.3)).setKl(6E-4).setKq(4E-7));
      scene.lights.add(new SpotLight(new Color(450, 140, 400),
         p(8.0, 9.0, 2.0), new Vector(-4.9, -7.2, -2.6))
         .setKl(3E-4).setKq(1.5E-7));

      // camera: angled 3/4 view from a high front-right vantage ---------------
      int nx = 1400, ny = 980;
      double fov = 52, vpDist = 8 * S;
      double vpH = 2 * vpDist * Math.tan(Math.toRadians(fov / 2));
      double vpW = vpH * ((double) nx / ny);

      Camera.getBuilder()
         .setRayTracer(scene, RayTracerType.SIMPLE)
         .setLocation(p(10.5, 6.2, 22.0))
         .setDirection(p(-0.6, 2.0, -3.0), new Vector(0, 1, 0))
         .setVpDistance(vpDist).setVpSize(vpW, vpH)
         .setResolution(nx, ny)
         .build()
         .renderImage()
         .writeToImage("crystalGallery");
   }

   /**
    * Produce the same Crystal Gallery image from the JSON scene file
    * (json/crystalGallery.json) via the JSON SceneLoader. The parser has no
    * polygon support, so quads are stored as triangle pairs; the camera is not
    * part of the JSON, so it is supplied here with the same parameters.
    * @throws IOException if the JSON scene file cannot be read
    */
   @Test
   void crystalGalleryJson() throws IOException {
      Scene scene = new JsonSceneParser().parse("crystalGallery");

      int nx = 1400, ny = 980;
      double fov = 52, vpDist = 8 * S;
      double vpH = 2 * vpDist * Math.tan(Math.toRadians(fov / 2));
      double vpW = vpH * ((double) nx / ny);

      Camera.getBuilder()
         .setRayTracer(scene, RayTracerType.SIMPLE)
         .setLocation(p(10.5, 6.2, 22.0))
         .setDirection(p(-0.6, 2.0, -3.0), new Vector(0, 1, 0))
         .setVpDistance(vpDist).setVpSize(vpW, vpH)
         .setResolution(nx, ny)
         .build()
         .renderImage()
         .writeToImage("crystalGallery json");
   }

   // ====================================================================== floor

   /**
    * Adds a reflective checkerboard floor. The dark squares are an infinite
    * {@link Plane} (so the floor reaches the horizon and the Plane primitive is
    * demonstrated); the light squares are thin {@link Polygon} tiles raised a
    * hair above it on the even cells (the engine has no procedural texturing, so
    * the checker is real geometry).
    * @param scene the scene to populate
    */
   private static void addCheckerFloor(Scene scene) {
      scene.geometries.add(new Plane(p(0, 0, 0), new Vector(0, 1, 0))
         .setMaterial(new Material().setKD(new Double3(0.13, 0.13, 0.15))
            .setKS(0.2).setShininess(60).setKR(0.28)
            .setKA(new Double3(0.07, 0.07, 0.08))));

      Material light = new Material().setKD(new Double3(0.62, 0.62, 0.66))
         .setKS(0.25).setShininess(60).setKR(0.28)
         .setKA(new Double3(0.32, 0.32, 0.34));

      double tile = 2.2, x0 = -26, z0 = -12, y = 0.006; // ~0.3 units above the plane
      int cols = 20, rows = 10;
      for (int i = 0; i < cols; i++) {
         for (int j = 0; j < rows; j++) {
            if (((i + j) & 1) != 0) continue;            // only the light cells
            double x = x0 + i * tile, z = z0 + j * tile;
            scene.geometries.add(new Polygon(p(x, y, z), p(x, y, z + tile),
               p(x + tile, y, z + tile), p(x + tile, y, z)).setMaterial(light));
         }
      }
   }

   // ===================================================================== mirror

   /**
    * Adds the framed standing mirror at the back: one reflective {@link Polygon}
    * pane plus four slim polished-gold frame bars just in front of it.
    * @param scene the scene to populate
    */
   private static void addFramedMirror(Scene scene) {
      double mw = 7.5, mb = 0.6, mt = 8.7, fo = mw + 0.22;
      scene.geometries.add(new Polygon(p(-mw, mb, -7), p(mw, mb, -7),
         p(mw, mt, -7), p(-mw, mt, -7))
         .setMaterial(new Material().setKD(new Double3(0.05, 0.065, 0.09))
            .setKS(0.5).setShininess(150).setKR(0.6)
            .setKA(new Double3(0.025, 0.033, 0.045))));

      Material frame = solid(0.82, 0.62, 0.22, 0.55, 0.8, 220, 0.35).setKR(0.12);
      scene.geometries.add(
         new Polygon(p(-fo, mt, -6.9), p(fo, mt, -6.9),
            p(fo, mt + 0.22, -6.9), p(-fo, mt + 0.22, -6.9)).setMaterial(frame),
         new Polygon(p(-fo, mb - 0.22, -6.9), p(fo, mb - 0.22, -6.9),
            p(fo, mb, -6.9), p(-fo, mb, -6.9)).setMaterial(frame),
         new Polygon(p(-fo, mb, -6.9), p(-mw, mb, -6.9),
            p(-mw, mt, -6.9), p(-fo, mt, -6.9)).setMaterial(frame),
         new Polygon(p(mw, mb, -6.9), p(fo, mb, -6.9),
            p(fo, mt, -6.9), p(mw, mt, -6.9)).setMaterial(frame));
   }

   // ======================================================================= dice

   /**
    * Adds a cube resting on a corner: its (1,1,1) diagonal is lifted vertical,
    * spun by {@code yawDeg}, then leaned {@code tiltDeg} toward the camera so its
    * three upper faces present clearly. The cube is auto-grounded so its lowest
    * vertex sits on the floor at design (cx,cz). Built from six {@link Polygon}
    * faces plus flat {@link Cylinder} pips on all six faces (opposite faces sum
    * to 7), so whichever three faces front the camera always show their dots.
    * @param scene   the scene to populate
    * @param cxU     design x of the resting position
    * @param czU     design z of the resting position
    * @param halfU   design half-edge of the cube
    * @param yawDeg  spin about the vertical, in degrees
    * @param tiltDeg lean toward the camera (+Z), in degrees
    * @param body    material for the (transparent) faces
    * @param pipMat  material for the (opaque) pips
    */
   private static void addDie(Scene scene, double cxU, double czU, double halfU,
                              double yawDeg, double tiltDeg,
                              Material body, Material pipMat) {
      double s = halfU * S;
      double[] rp = {
         Math.acos(1.0 / Math.sqrt(3)),         // align angle
         -1.0 / Math.sqrt(2), 0, 1.0 / Math.sqrt(2), // align axis (unit)
         Math.cos(Math.toRadians(yawDeg)), Math.sin(Math.toRadians(yawDeg)),
         Math.cos(Math.toRadians(tiltDeg)), Math.sin(Math.toRadians(tiltDeg))
      };

      // auto-ground: rotate the 8 corners, then translate to (cx, floor, cz)
      double sumX = 0, sumZ = 0, minY = Double.POSITIVE_INFINITY;
      for (int sx = -1; sx <= 1; sx += 2)
         for (int sy = -1; sy <= 1; sy += 2)
            for (int sz = -1; sz <= 1; sz += 2) {
               double[] r = rotDie(sx * s, sy * s, sz * s, rp);
               sumX += r[0];
               sumZ += r[2];
               if (r[1] < minY) minY = r[1];
            }
      double tX = cxU * S - sumX / 8, tY = -minY, tZ = czU * S - sumZ / 8;

      double[][] normals = {
         {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
      int[] counts = {3, 4, 1, 6, 2, 5};
      double[][][] corners = {
         {{s, -s, -s}, {s, s, -s}, {s, s, s}, {s, -s, s}},        // +X
         {{-s, -s, s}, {-s, s, s}, {-s, s, -s}, {-s, -s, -s}},    // -X
         {{-s, s, -s}, {-s, s, s}, {s, s, s}, {s, s, -s}},        // +Y
         {{-s, -s, s}, {-s, -s, -s}, {s, -s, -s}, {s, -s, s}},    // -Y
         {{-s, -s, s}, {s, -s, s}, {s, s, s}, {-s, s, s}},        // +Z
         {{s, -s, -s}, {-s, -s, -s}, {-s, s, -s}, {s, s, -s}}};   // -Z

      for (int f = 0; f < 6; f++) {
         Point[] c = new Point[4];
         for (int kk = 0; kk < 4; kk++)
            c[kk] = dieXform(corners[f][kk], rp, tX, tY, tZ);
         scene.geometries.add(new Polygon(c[0], c[1], c[2], c[3]).setMaterial(body));

         // pips on this face
         double[] nr = rotDie(normals[f][0], normals[f][1], normals[f][2], rp);
         Vector nWorld = new Vector(nr[0], nr[1], nr[2]).normalize();
         Point faceCenter = dieXform(new double[]{
            normals[f][0] * s, normals[f][1] * s, normals[f][2] * s}, rp, tX, tY, tZ);
         Vector u = c[1].subtract(c[0]).normalize();
         Vector vAxis = nWorld.crossProduct(u).normalize();
         for (double[] off : pipOffsets(counts[f], s * 0.42)) {
            Point center = faceCenter;
            if (off[0] != 0) center = center.add(u.scale(off[0]));
            if (off[1] != 0) center = center.add(vAxis.scale(off[1]));
            Point base = center.add(nWorld.scale(1.0));   // small outward offset
            scene.geometries.add(new Cylinder(s * 0.14,
               new Ray(base, nWorld), s * 0.05).setMaterial(pipMat));
         }
      }
   }

   /**
    * Applies the dice orientation to a local point: Rodrigues alignment of the
    * (1,1,1) diagonal to +Y, then a spin about Y, then a lean about X.
    * @param x  local x
    * @param y  local y
    * @param z  local z
    * @param rp packed rotation parameters {angle, axX, axY, axZ, cosYaw, sinYaw, cosTilt, sinTilt}
    * @return the rotated coordinates as {x, y, z}
    */
   private static double[] rotDie(double x, double y, double z, double[] rp) {
      double cosA = Math.cos(rp[0]), sinA = Math.sin(rp[0]), oneMinus = 1 - cosA;
      double ax = rp[1], ay = rp[2], az = rp[3];
      double dot = ax * x + ay * y + az * z;
      double cx = ay * z - az * y, cy = az * x - ax * z, cz = ax * y - ay * x;
      double rx = x * cosA + cx * sinA + ax * dot * oneMinus;
      double ry = y * cosA + cy * sinA + ay * dot * oneMinus;
      double rz = z * cosA + cz * sinA + az * dot * oneMinus;
      double yx = rp[4] * rx + rp[5] * rz, yy = ry, yz = -rp[5] * rx + rp[4] * rz;
      return new double[]{yx, rp[6] * yy - rp[7] * yz, rp[7] * yy + rp[6] * yz};
   }

   /**
    * Rotates a local dice coordinate and applies the grounding translation.
    * @param local the local {x,y,z}
    * @param rp    packed rotation parameters
    * @param tX    translation x
    * @param tY    translation y
    * @param tZ    translation z
    * @return the world-space point
    */
   private static Point dieXform(double[] local, double[] rp,
                                 double tX, double tY, double tZ) {
      double[] r = rotDie(local[0], local[1], local[2], rp);
      return new Point(r[0] + tX, r[1] + tY, r[2] + tZ);
   }

   /**
    * Standard die pip layout for a face.
    * @param n number of pips (1..6)
    * @param d half the pip spacing, in world units
    * @return list of (u,v) face-local pip offsets
    */
   private static double[][] pipOffsets(int n, double d) {
      return switch (n) {
         case 1 -> new double[][]{{0, 0}};
         case 2 -> new double[][]{{-d, -d}, {d, d}};
         case 3 -> new double[][]{{-d, -d}, {0, 0}, {d, d}};
         case 4 -> new double[][]{{-d, -d}, {-d, d}, {d, -d}, {d, d}};
         case 5 -> new double[][]{{-d, -d}, {-d, d}, {d, -d}, {d, d}, {0, 0}};
         default -> new double[][]{{-d, -d}, {-d, 0}, {-d, d}, {d, -d}, {d, 0}, {d, d}};
      };
   }
}
