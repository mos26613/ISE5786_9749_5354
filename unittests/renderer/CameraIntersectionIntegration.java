package renderer;

import geometries.api.Intersectable;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for integration of Camera with ray intersections of geometries.
 * Tests the number of intersections between rays constructed by the Camera and various geometries.
 */
class CameraIntersectionIntegration {

    /**
     * Test name for sphere integration test.
     */
    private final static String SPHERE_TEST = "testCameraRaySphereIntegration";
    /**
     * Test name for plane integration test.
     */
    private final static String PLANE_TEST = "testCameraRayPlaneIntegration";
    /**
     * Test name for triangle integration test.
     */
    private final static String TRIANGLE_TEST = "testCameraRayTriangleIntegration";
    /**
     * Camera configuration for the tests: 3 pixes for the width
     */
    private final static int NX = 3;
    /**
     * Camera configuration for the tests: 3 pixes for the height
     */
    private final static int NY = 3;
    /**
     * Camera configuration for the tests
     */
    private final static Camera CAMERA = Camera.getBuilder()
            .setLocation(Point.ZERO)
            .setDirection(Vector.AXIS_Z.scale(-1), Vector.AXIS_Y)
            .setVpDistance(1)
            .setResolution(NX, NY)
            .setVpSize(3, 3)
            .build();
    /**
     * Default constructor for CameraIntersectionIntegration to satisfy Javadoc tool
     */
    CameraIntersectionIntegration() {
    }

    /**
     * Helper method to assert the number of intersections between rays constructed by the Camera and a given geometry.
     *
     * @param camera                the Camera used to construct rays
     * @param body                  the geometry to test intersections with
     * @param expectedIntersections the expected number of intersections
     * @param testName              the name of the test for error messages
     */
    private void assertIntersectionsCount(Camera camera, Intersectable body, int expectedIntersections, String testName) {
        Ray ray;
        int count = 0;
        for (int i = 0; i < NY; i++) {
            for (int j = 0; j < NX; j++) {
                ray = camera.constructRay(j, i);
                count += body.findIntersections(ray) != null ? body.findIntersections(ray).size() : 0;
            }
        }
        assertEquals(expectedIntersections, count, testName);
    }

    /**
     * Test method for integration of Camera with ray intersections of Sphere geometry.
     */
    @Test
    void testCameraRaySphereIntegration() {
        // ============ Equivalence Partitions Tests ==============

        //EP01
        Sphere sphere1 = new Sphere(new Point(0, 0, -3), 1);
        assertIntersectionsCount(CAMERA, sphere1, 2, SPHERE_TEST);

        //EP02
        Sphere sphere2 = new Sphere(new Point(0, 0, -3), 2.5);
        assertIntersectionsCount(CAMERA, sphere2, 18, SPHERE_TEST);

        //EP03
        Sphere sphere3 = new Sphere(new Point(0, 0, -2.5), 2);
        assertIntersectionsCount(CAMERA, sphere3, 10, SPHERE_TEST);

        //EP04
        Sphere sphere4 = new Sphere(new Point(0, 0, -1), 4);
        assertIntersectionsCount(CAMERA, sphere4, 9, SPHERE_TEST);

        // ============ Boundary Values Tests ==============

        //BV01
        Sphere sphere5 = new Sphere(new Point(0, 0, 1), 0.5);
        assertIntersectionsCount(CAMERA, sphere5, 0, SPHERE_TEST);
    }

    /**
     * Test method for integration of Camera with ray intersections of Plane geometry.
     */
    @Test
    void testCameraRayPlaneIntegration() {

        // ============ Equivalence Partitions Tests ==============

        //EP01
        Plane plane1 = new Plane(new Point(0, 0, -2), Vector.AXIS_Z);
        assertIntersectionsCount(CAMERA, plane1, 9, PLANE_TEST);

        //EP02
        Plane plane2 = new Plane(new Point(0, 0, -2), new Vector(0, -0.1, 1));
        assertIntersectionsCount(CAMERA, plane2, 9, PLANE_TEST);

        //EP03
        Plane plane3 = new Plane(new Point(0, 0, -2.5), new Vector(0, -1, 1));
        assertIntersectionsCount(CAMERA, plane3, 6, PLANE_TEST);

    }

    /**
     * Test method for integration of Camera with ray intersections of Triangle geometry.
     */
    @Test
    void testCameraRayTriangleIntegration() {

        // ============ Equivalence Partitions Tests ==============

        //EP01
        Triangle triangle1 = new Triangle(new Point(0, 1, -2), new Point(-1, -1, -2), new Point(1, -1, -2));
        assertIntersectionsCount(CAMERA, triangle1, 1, TRIANGLE_TEST);

        //EP02
        Triangle triangle2 = new Triangle(new Point(0, 20, -2), new Point(-1, -1, -2), new Point(1, -1, -2));
        assertIntersectionsCount(CAMERA, triangle2, 2, TRIANGLE_TEST);

    }
}
