package parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

/**
 * Unit tests for {@link JsonSceneParser}.
 * <p>
 * Verifies that the parser reproduces the scene declared in
 * {@code json/basicRenderTestTwoColors.json}, which mirrors the scene built
 * in {@code RenderTests.testBasicRenderTwoColors}.
 */
class JsonSceneParserTests {
    /**
     * Expected scene name from the JSON fixture.
     */
    private static final String EXPECTED_NAME = "Two colors";
    /**
     * Expected background color from the JSON fixture.
     */
    private static final Color EXPECTED_BACKGROUND = new Color(75, 127, 90);
    /**
     * Expected ambient-light intensity from the JSON fixture.
     */
    private static final Color EXPECTED_AMBIENT = new Color(255, 191, 191);

    /**
     * Default constructor.
     */
    JsonSceneParserTests() { /* to satisfy Javadoc generator */ }

    /**
     * Tests {@link JsonSceneParser#parse(String)} on the reference fixture.
     * Verifies that scalar fields round-trip and that the geometries composite
     * reports at least one intersection along the expected camera ray.
     *
     * @throws IOException if the fixture file cannot be read
     */
    @Test
    void testParseBasicRenderTwoColors() throws IOException {
        // Arrange + Act
        Scene scene = new JsonSceneParser().parse("basicRenderTestTwoColors");

        // Assert — scalar fields
        assertEquals(EXPECTED_NAME, scene.name, "Wrong scene name");
        assertEquals(EXPECTED_BACKGROUND, scene.background, "Wrong background color");
        assertEquals(EXPECTED_AMBIENT, scene.ambientLight.getIntensity(), "Wrong ambient intensity");

        // Assert — geometries: a ray from origin toward -Z must hit the sphere at (0,0,-100).
        Ray down = new Ray(Point.ZERO, new Vector(0, 0, -1));
        assertNotNull(scene.geometries.findIntersections(down),
                "Expected at least one intersection along the central ray");
    }

    /**
     * Tests {@link JsonSceneParser#parse(String)} on a fixture that declares a
     * tube and a cylinder. Verifies that the scene name round-trips and that the
     * cylinder (axis-aligned with -Z) is hit by the central camera ray, confirming
     * both geometries were parsed and added to the composite.
     *
     * @throws IOException if the fixture file cannot be read
     */
    @Test
    void testParseTubeAndCylinder() throws IOException {
        // Arrange + Act
        Scene scene = new JsonSceneParser().parse("tubeCylinderTest");

        // Assert — scalar field
        assertEquals("Tube and cylinder", scene.name, "Wrong scene name");

        // Assert — a ray from the origin toward -Z must hit the cylinder's top base.
        Ray down = new Ray(Point.ZERO, new Vector(0, 0, -1));
        assertNotNull(scene.geometries.findIntersections(down),
                "Expected an intersection with the cylinder along the central ray");
    }
}
