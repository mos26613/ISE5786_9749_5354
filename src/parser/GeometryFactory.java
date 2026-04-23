package parser;

import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;

/**
 * Builds {@link geometries.api.Geometry} instances from course-style string
 * attributes produced by {@link AttributeParser}.
 * <p>
 * This class is the single point at which scene parsers couple to geometry
 * constructors. Adding a new geometry kind (for example a cylinder) or a new
 * constructor variant is a localized change here, without touching any
 * format-specific parser.
 * <p>
 * Later stages that attach materials or emission colors to geometries will
 * extend this factory with overloads that accept those additional attributes.
 */
final class GeometryFactory {
    /** Private constructor to prevent instantiation of this utility class. */
    private GeometryFactory() { /* static helpers only */ }

    /**
     * Builds a sphere from a center attribute and a radius attribute.
     *
     * @param centerAttr center point as "x y z"
     * @param radiusAttr radius as a single numeric string
     * @return the built sphere
     */
    static Sphere buildSphere(String centerAttr, String radiusAttr) {
        return new Sphere(
                AttributeParser.parsePoint(centerAttr),
                AttributeParser.parseDouble(radiusAttr, "sphere radius"));
    }

    /**
     * Builds a triangle from three vertex attributes.
     *
     * @param p0Attr first vertex as "x y z"
     * @param p1Attr second vertex as "x y z"
     * @param p2Attr third vertex as "x y z"
     * @return the built triangle
     */
    static Triangle buildTriangle(String p0Attr, String p1Attr, String p2Attr) {
        return new Triangle(
                AttributeParser.parsePoint(p0Attr),
                AttributeParser.parsePoint(p1Attr),
                AttributeParser.parsePoint(p2Attr));
    }

    /**
     * Builds a plane from a point-on-plane attribute and a normal-vector attribute.
     *
     * @param pointAttr  a point on the plane as "x y z"
     * @param normalAttr the plane normal as "x y z"
     * @return the built plane
     */
    static Plane buildPlaneByPointNormal(String pointAttr, String normalAttr) {
        return new Plane(
                AttributeParser.parsePoint(pointAttr),
                AttributeParser.parseVector(normalAttr));
    }

    /**
     * Builds a plane from three non-collinear points.
     *
     * @param p0Attr first point as "x y z"
     * @param p1Attr second point as "x y z"
     * @param p2Attr third point as "x y z"
     * @return the built plane
     */
    static Plane buildPlaneByThreePoints(String p0Attr, String p1Attr, String p2Attr) {
        return new Plane(
                AttributeParser.parsePoint(p0Attr),
                AttributeParser.parsePoint(p1Attr),
                AttributeParser.parsePoint(p2Attr));
    }
}
