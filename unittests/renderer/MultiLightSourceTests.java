package renderer;

import geometries.api.Geometry;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import static java.awt.Color.BLUE;

/**
 * Integration tests for rendering scenes containing geometries illuminated by multiple
 * types of light sources simultaneously (Directional, Point, and Spotlights).
 * * Tests demonstrate color separation, light attenuation, and specular effects.
 */
public class MultiLightSourceTests {

    /**
     * Default constructor for MultiLightSourceTests.
     */
    MultiLightSourceTests() {
    }

    /**
     * The internal image resolution width and height used for rendering the test targets.
     */
    private static final int RESOLUTION = 500;

    /**
     * Scene representing the spherical test target layout.
     */
    private final Scene _scene1 = new Scene("Sphere scene")
            .setAmbientLight(new AmbientLight(new Color(0, 0, 0)));

    /**
     * Scene representing the polygonal triangle test target layout.
     */
    private final Scene _scene2 = new Scene("Triangle scene")
            .setAmbientLight(new AmbientLight(new Color(15, 15, 15)));

    /**
     * Camera builder adjusted for rendering the sphere scene layout.
     */
    private final Camera.Builder _camera1 = Camera.getBuilder()
            .setRayTracer(_scene1, RayTracerType.SIMPLE)
            .setLocation(new Point(0, 0, 1000))
            .setDirection(Point.ZERO, Vector.AXIS_Y)
            .setVpSize(150, 150).setVpDistance(1000);

    /**
     * Camera builder adjusted for rendering the triangle scene layout.
     */
    private final Camera.Builder _camera2 = Camera.getBuilder()
            .setRayTracer(_scene2, RayTracerType.SIMPLE)
            .setLocation(new Point(0, 0, 1000))
            .setDirection(Point.ZERO, Vector.AXIS_Y)
            .setVpSize(200, 200).setVpDistance(1000);

    /**
     * Default shininess exponent factor used for standard test materials.
     */
    private static final int SHININESS = 301;

    /**
     * Default diffuse reflection coefficient scalar.
     */
    private static final double KD = 0.5;

    /**
     * Default specular reflection coefficient scalar.
     */
    private static final double KS = 0.5;

    /**
     * Highly reflective material configuration tailored to make flat triangle faces
     * significantly brighter and reactive to specular spotlights.
     */
    private static final Material TRIANGLE_MATERIAL = new Material()
            .setKD(new Double3(0.6, 0.6, 0.6))
            .setKS(new Double3(0.8, 0.8, 0.8))
            .setShininess(301);

    /**
     * The default color emission mask applied to the sphere instance.
     */
    private static final Color SPHERE_COLOR = new Color(BLUE).reduce(4);

    /**
     * Center origin coordinates of the target test sphere.
     */
    private static final Point SPHERE_CENTER = new Point(0, 0, -50);

    /**
     * Radial boundaries size parameter of the target test sphere.
     */
    private static final double SPHERE_RADIUS = 50D;

    /**
     * Spatial vertex map defining the corners of the composite triangles layout.
     */
    private static final Point[] VERTICES = {
            new Point(-110, -110, -150),
            new Point(95, 100, -150),
            new Point(110, -110, -150),
            new Point(-75, 78, 100)
    };

    // =========================================================================
    // SPHERE LIGHTING (3-zone color isolation setup constants)
    // =========================================================================

    /**
     * Primary Crimson Red emission tint representing the background directional source for the sphere.
     */
    private static final Color SPHERE_DIR_COLOR = new Color(250, 0, 0);

    /**
     * Spatial vector path angle representing the background directional path for the sphere.
     */
    private static final Vector SPHERE_DIR_DIRECTION = new Vector(1, 0, -1);

    /**
     * Vivid Cyan emission tint representing the near foreground point light source for the sphere.
     */
    private static final Color SPHERE_POINT_COLOR = new Color(0, 600, 600);

    /**
     * Local positioning coordinate space map for the sphere point light component.
     */
    private static final Point SPHERE_POINT_POSITION = new Point(55, -45, 40);

    /**
     * Vibrant Yellow emission tint representing the targeted top focus spotlight for the sphere.
     */
    private static final Color SPHERE_SPOT_COLOR = new Color(600, 500, 0);

    /**
     * Local positioning coordinate space map for the sphere spotlight beam source.
     */
    private static final Point SPHERE_SPOT_POSITION = new Point(-10, 55, 40);

    /**
     * Vector target aim pointing path direction for the sphere spotlight component.
     */
    private static final Vector SPHERE_SPOT_DIRECTION = new Vector(0.2, -1, -0.8);

    // =========================================================================
    // TRIANGLES LIGHTING (Foreground high-intensity constants)
    // =========================================================================

    /**
     * Primary Crimson Red emission tint representing the sweeping directional path across the triangles.
     */
    private static final Color TRIANGLES_DIR_COLOR = new Color(250, 0, 0);

    /**
     * Spatial vector path angle representing the direction of the red light across the triangles.
     */
    private static final Vector TRIANGLES_DIR_DIRECTION = new Vector(-1, -1, -1);

    /**
     * Vivid Cyan emission tint representing the foreground point source for the bottom-right triangle face.
     */
    private static final Color TRIANGLES_POINT_COLOR = new Color(0, 600, 600);

    /**
     * Local positioning coordinate space map for the triangle point light, placed in front (Z=80).
     */
    private static final Point TRIANGLES_POINT_POSITION = new Point(40, -40, 80);

    /**
     * Vibrant Yellow emission tint representing the front-left spotlight beam hitting the triangles center.
     */
    private static final Color TRIANGLES_SPOT_COLOR = new Color(600, 500, 0);

    /**
     * Local positioning coordinate space map for the triangle spotlight, placed in front (Z=80).
     */
    private static final Point TRIANGLES_SPOT_POSITION = new Point(-40, 40, 80);

    /**
     * Vector target aim pointing path direction for the triangle spotlight component.
     */
    private static final Vector TRIANGLES_SPOT_DIRECTION = new Vector(1, -1, -2);

    /**
     * Geometry instance of the structural test sphere.
     */
    private static final Geometry SPHERE = new Sphere(SPHERE_CENTER, SPHERE_RADIUS)
            .setEmission(SPHERE_COLOR).setMaterial(new Material().setKD(KD).setKS(KS).setShininess(SHININESS));

    /**
     * The first structural base asset component representing the right-side base triangle.
     */
    private static final Geometry TRIANGLE1 = new Triangle(VERTICES[0], VERTICES[1], VERTICES[2])
            .setMaterial(TRIANGLE_MATERIAL);

    /**
     * The second structural base asset component representing the left-side tilting forward triangle.
     */
    private static final Geometry TRIANGLE2 = new Triangle(VERTICES[0], VERTICES[1], VERTICES[3])
            .setMaterial(TRIANGLE_MATERIAL);

    /**
     * Produces a picture of a sphere illuminated by three distinct light sources
     * (Directional, Point, Spot) showing isolated Red, Cyan, and Yellow zones.
     */
    @Test
    void testSphere() {
        _scene1.geometries.add(SPHERE);

        _scene1.lights.add(new DirectionalLight(SPHERE_DIR_COLOR, SPHERE_DIR_DIRECTION));
        _scene1.lights.add(new PointLight(SPHERE_POINT_COLOR, SPHERE_POINT_POSITION)
                .setKl(0.001).setKq(0.0005));
        _scene1.lights.add(new SpotLight(SPHERE_SPOT_COLOR, SPHERE_SPOT_POSITION, SPHERE_SPOT_DIRECTION)
                .setKl(0.001).setKq(0.0003));

        _camera1
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("lightSphere");
    }

    /**
     * Produces a picture of two triangles brightly illuminated from the front
     * using the high-intensity Red, Cyan, and Yellow palette to capture planar gradients.
     */
    @Test
    void testTriangles() {
        _scene2.geometries.add(TRIANGLE1, TRIANGLE2);

        _scene2.lights.add(new DirectionalLight(TRIANGLES_DIR_COLOR, TRIANGLES_DIR_DIRECTION));

        _scene2.lights.add(new PointLight(TRIANGLES_POINT_COLOR, TRIANGLES_POINT_POSITION)
                .setKl(0.0005).setKq(0.0001));
        _scene2.lights.add(new SpotLight(TRIANGLES_SPOT_COLOR, TRIANGLES_SPOT_POSITION, TRIANGLES_SPOT_DIRECTION)
                .setKl(0.0005).setKq(0.0001));

        _camera2
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("lightTriangles");
    }
}