package parser;

import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;

/**
 * Builds {@link lighting.LightSource} instances from course-style string
 * attributes produced by {@link AttributeParser}.
 * <p>
 * This class is the single point at which scene parsers couple to light-source
 * constructors, mirroring {@link GeometryFactory}. Adding a new light kind or a
 * new attribute is a localized change here, without touching any format-specific
 * parser.
 */
final class LightFactory {
    /** Private constructor to prevent instantiation of this utility class. */
    private LightFactory() { /* static helpers only */ }

    /**
     * Builds a directional light from an intensity color and a direction vector.
     *
     * @param intensityAttr the light intensity color as "R G B"
     * @param directionAttr the light direction as "x y z"
     * @return the built directional light
     */
    static DirectionalLight buildDirectional(String intensityAttr, String directionAttr) {
        return new DirectionalLight(
                AttributeParser.parseColor(intensityAttr),
                AttributeParser.parseVector(directionAttr));
    }

    /**
     * Builds a point light from an intensity color, a position, and optional
     * attenuation factors. Each {@code null} attenuation attribute leaves its
     * default unchanged.
     *
     * @param intensityAttr the light intensity color as "R G B"
     * @param positionAttr  the light position as "x y z"
     * @param kcAttr        the constant attenuation factor, or {@code null}
     * @param klAttr        the linear attenuation factor, or {@code null}
     * @param kqAttr        the quadratic attenuation factor, or {@code null}
     * @param sizeAttr      the area-light diameter for soft shadows, or {@code null}
     * @return the built point light
     */
    static PointLight buildPoint(String intensityAttr, String positionAttr,
                                 String kcAttr, String klAttr, String kqAttr, String sizeAttr) {
        return applyOptional(new PointLight(
                AttributeParser.parseColor(intensityAttr),
                AttributeParser.parsePoint(positionAttr)), kcAttr, klAttr, kqAttr, sizeAttr);
    }

    /**
     * Builds a spot light from an intensity color, a position, a direction, and
     * optional attenuation factors. Each {@code null} attenuation attribute leaves
     * its default unchanged.
     *
     * @param intensityAttr the light intensity color as "R G B"
     * @param positionAttr  the light position as "x y z"
     * @param directionAttr the beam direction as "x y z"
     * @param kcAttr        the constant attenuation factor, or {@code null}
     * @param klAttr        the linear attenuation factor, or {@code null}
     * @param kqAttr        the quadratic attenuation factor, or {@code null}
     * @param sizeAttr      the area-light diameter for soft shadows, or {@code null}
     * @return the built spot light
     */
    static SpotLight buildSpot(String intensityAttr, String positionAttr, String directionAttr,
                               String kcAttr, String klAttr, String kqAttr, String sizeAttr) {
        SpotLight light = new SpotLight(
                AttributeParser.parseColor(intensityAttr),
                AttributeParser.parsePoint(positionAttr),
                AttributeParser.parseVector(directionAttr));
        applyOptional(light, kcAttr, klAttr, kqAttr, sizeAttr);
        return light;
    }

    /**
     * Applies the optional attenuation factors and area-light size to a point light
     * (or any subtype, such as a spot light). Each {@code null} attribute is skipped.
     *
     * @param light    the light to configure
     * @param kcAttr   the constant attenuation factor, or {@code null}
     * @param klAttr   the linear attenuation factor, or {@code null}
     * @param kqAttr   the quadratic attenuation factor, or {@code null}
     * @param sizeAttr the area-light diameter for soft shadows, or {@code null}
     * @return the same light, configured
     */
    private static PointLight applyOptional(PointLight light, String kcAttr, String klAttr,
                                            String kqAttr, String sizeAttr) {
        if (kcAttr != null) light.setKc(AttributeParser.parseDouble(kcAttr, "light kC"));
        if (klAttr != null) light.setKl(AttributeParser.parseDouble(klAttr, "light kL"));
        if (kqAttr != null) light.setKq(AttributeParser.parseDouble(kqAttr, "light kQ"));
        if (sizeAttr != null) light.setSize(AttributeParser.parseDouble(sizeAttr, "light size"));
        return light;
    }
}
