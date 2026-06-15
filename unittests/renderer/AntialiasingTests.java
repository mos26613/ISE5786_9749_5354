package renderer;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import parser.JsonSceneParser;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/** Tests for antialiasing. Renders the same scene with and without antialiasing, to visually compare the results. */
class AntialiasingTests {
    /**
     * Uniform world scale so the scene spans hundreds of units (comfortable for the fixed secondary-ray DELTA).
     */
    private static final double S = 50.0;

    /**
     * Builds a scaled point from compact "design" coordinates.
     *
     * @param x design x
     * @param y design y
     * @param z design z
     * @return the point multiplied by the world scale {@link #S}
     */
    private static Point p(double x, double y, double z) {
        return new Point(x * S, y * S, z * S);
    }

    /** Number of samples for antialiasing. Higher values will produce smoother results but will take longer to render. */
    private static final int AA_SAMPLES = 30;

    /** Renders the given scene with the given antialiasing sampler (or without antialiasing if the sampler is null)
     *  and writes the image to a file with the given name.
     */
    private void createImage(String name, Scene scene, BeamSampler aaSampler) {
        // camera: angled 3/4 view from a high front-right vantage ---------------
        int nx = 1400, ny = 980;
        double fov = 52, vpDist = 8 * S;
        double vpH = 2 * vpDist * Math.tan(Math.toRadians(fov / 2));
        double vpW = vpH * ((double) nx / ny);

        Camera.Builder builder = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(p(10.5, 6.2, 22.0))
                .setDirection(p(-0.6, 2.0, -3.0), new Vector(0, 1, 0))
                .setVpDistance(vpDist).setVpSize(vpW, vpH)
                .setResolution(nx, ny)
                .setMultithreading(-2)
                .setDebugPrint(1);
                if (aaSampler != null) builder.setAntialiasingBeamSampler(aaSampler);
                builder.build()
                .renderImage()
                .writeToImage(name);
    }

    /** Renders the scene without antialiasing. The resulting image should have jagged edges and more aliasing artifacts than the one rendered with antialiasing. */
    @Test
    void testWithoutAntialiasing() throws IOException {
        Scene scene = new JsonSceneParser().parse("crystalGallery");

        createImage("antialiasing_without", scene, null);
    }

    /** Renders the scene with antialiasing. The resulting image should have smoother edges and fewer aliasing artifacts than the one rendered without antialiasing. */
    @Test
    void testWithAntialiasing() throws IOException {
        Scene scene = new JsonSceneParser().parse("crystalGallery");

        createImage("antialiasing_with", scene,
                new BeamSampler(AA_SAMPLES, BeamSampler.Shape.SQUARE, BeamSampler.Pattern.JITTERED));
    }
}
