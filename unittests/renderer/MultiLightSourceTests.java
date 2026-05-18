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

public class MultiLightSourceTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    MultiLightSourceTests() { /* to satisfy Javadoc generator */ }

    /**
     * Constant for tests resolution
     */
    private static final int RESOLUTION = 500;

    /**
     * First scene for some of the tests
     */
    private final Scene _scene1 = new Scene("Triangle scene");
    /**
     * Second scene for some of the tests
     */
    private final Scene _scene2 = new Scene("Sphere scene")
            .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

    /**
     * First camera builder for some of the tests
     */
    private final Camera.Builder _camera1 = Camera.getBuilder()                                //
            .setRayTracer(_scene1, RayTracerType.SIMPLE)                                                              //
            .setLocation(new Point(0, 0, 1000))                                                                       //
            .setDirection(Point.ZERO, Vector.AXIS_Y)                                                                  //
            .setVpSize(150, 150).setVpDistance(1000);

    /**
     * Second camera builder for some of the tests
     */
    private final Camera.Builder _camera2 = Camera.getBuilder()                                //
            .setRayTracer(_scene2, RayTracerType.SIMPLE)                                                              //
            .setLocation(new Point(0, 0, 1000))                                                                       //
            .setDirection(Point.ZERO, Vector.AXIS_Y)                                                                  //
            .setVpSize(200, 200).setVpDistance(1000);

    /**
     * Shininess value for most of the geometries in the tests
     */
    private static final int SHININESS = 301;
    /**
     * Diffusion attenuation factor for some of the geometries in the tests
     */
    private static final double KD = 0.5;
    /**
     * Diffusion attenuation factor for some of the geometries in the tests
     */
    private static final Double3 KD3 = new Double3(0.2, 0.6, 0.4);

    /**
     * Specular attenuation factor for some of the geometries in the tests
     */
    private static final double KS = 0.5;
    /**
     * Specular attenuation factor for some of the geometries in the tests
     */
    private static final Double3 KS3 = new Double3(0.2, 0.4, 0.3);

    /**
     * Material for some of the geometries in the tests
     */
    private static final Material MATERIAL = new Material().setKD(KD3).setKS(KS3)
            .setShininess(SHININESS);
    /**
     * Light color for tests with triangles
     */
    private static final Color TRIANGLES_LIGHT_COLOR = new Color(800, 500, 250);
    /**
     * Light color for tests with sphere
     */
    private static final Color SPHERE_LIGHT_COLOR = new Color(800, 500, 0);
    /**
     * Color of the sphere
     */
    private static final Color SPHERE_COLOR = new Color(BLUE).reduce(2);

    /**
     * Center of the sphere
     */
    private static final Point SPHERE_CENTER = new Point(0, 0, -50);
    /**
     * Radius of the sphere
     */
    private static final double SPHERE_RADIUS = 50D;

    /**
     * The triangles' vertices for the tests with triangles
     */
    private static final Point[] VERTICES =
            {
                    // the shared left-bottom:
                    new Point(-110, -110, -150),
                    // the shared right-top:
                    new Point(95, 100, -150),
                    // the right-bottom
                    new Point(110, -110, -150),
                    // the left-top
                    new Point(-75, 78, 100)
            };
    /**
     * Position of the light in tests with sphere
     */
    private static final Point SPHERE_LIGHT_POSITION = new Point(-50, -50, 25);
    /**
     * Light direction (directional and spot) in tests with sphere
     */
    private static final Vector SPHERE_LIGHT_DIRECTION = new Vector(1, 1, -0.5);
    /**
     * Position of the light in tests with triangles
     */
    private static final Point TRIANGLES_LIGHT_POSITION = new Point(30, 10, -100);
    /**
     * Light direction (directional and spot) in tests with triangles
     */
    private static final Vector TRIANGLES_LIGHT_DIRECTION = new Vector(-2, -2, -2);

    /**
     * The sphere in appropriate tests
     */
    private static final Geometry SPHERE = new Sphere(SPHERE_CENTER, SPHERE_RADIUS)
            .setEmission(SPHERE_COLOR).setMaterial(new Material().setKD(KD).setKS(KS).setShininess(SHININESS));
    /**
     * The first triangle in appropriate tests
     */
    private static final Geometry TRIANGLE1 = new Triangle(VERTICES[0], VERTICES[1], VERTICES[2])
            .setMaterial(MATERIAL);
    /**
     * The first triangle in appropriate tests
     */
    private static final Geometry TRIANGLE2 = new Triangle(VERTICES[0], VERTICES[1], VERTICES[3])
            .setMaterial(MATERIAL);

    /**
     * Produce a picture of a sphere lighted by a directional light
     */
    @Test
    void testSphere() {
        _scene1.geometries.add(SPHERE);

        _scene1.lights.add(new DirectionalLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_DIRECTION));
        _scene1.lights.add(new PointLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_POSITION) //
                .setKl(0.001).setKq(0.0002));
        _scene1.lights.add(new SpotLight(SPHERE_LIGHT_COLOR, SPHERE_LIGHT_POSITION, SPHERE_LIGHT_DIRECTION) //
                .setKl(0.001).setKq(0.0001));


        _camera1 //
                .setResolution(RESOLUTION, RESOLUTION) //
                .build() //
                .renderImage() //
                .writeToImage("lightSphere");
    }

    /**
     * Produce a picture of two triangles lighted by a directional light
     */
    @Test
    void testTriangles() {
        _scene2.geometries.add(TRIANGLE1, TRIANGLE2);

        _scene2.lights.add(new DirectionalLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_DIRECTION));
        _scene2.lights.add(new SpotLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_POSITION, TRIANGLES_LIGHT_DIRECTION) //
                .setKl(0.001).setKq(0.0001));
        _scene2.lights.add(new PointLight(TRIANGLES_LIGHT_COLOR, TRIANGLES_LIGHT_POSITION) //
                .setKl(0.001).setKq(0.0002));

        _camera2.setResolution(500, 500) //
                .build() //
                .renderImage() //
                .writeToImage("lightTriangles");

    }
}