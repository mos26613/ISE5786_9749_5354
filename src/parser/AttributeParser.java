package parser;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Converts course-style string attributes into primitive domain objects.
 * <p>
 * Every attribute is a whitespace-separated list of numbers, matching the
 * format used by the course XML scene files. Keeping this logic in a single
 * class lets format-specific parsers (JSON, XML, ...) share the conversion
 * and avoids duplicating number parsing across loaders.
 * <p>
 * Future scalar attributes (for example a material index of refraction or a
 * light attenuation triple) should be added here rather than inside a
 * specific format parser.
 */
final class AttributeParser {
    /**
     * Expected number of components in a color, point, or vector attribute.
     */
    private static final int TRIPLE_COMPONENTS = 3;

    private AttributeParser() { /* static helpers only */ }

    /**
     * Parses exactly three whitespace-separated doubles.
     *
     * @param attribute   the attribute value
     * @param description human-readable name of the attribute (used in error messages)
     * @return the three parsed components
     * @throws IllegalArgumentException if the attribute does not contain exactly three numbers
     */
    private static double[] parseTriple(String attribute, String description) {
        if (attribute == null) {
            throw new IllegalArgumentException("Missing " + description + " attribute");
        }
        String[] parts = attribute.trim().split("\\s+");
        if (parts.length != TRIPLE_COMPONENTS) {
            throw new IllegalArgumentException(
                    "Expected 3 components for " + description + ", got " + parts.length + ": " + attribute);
        }
        double[] values = new double[TRIPLE_COMPONENTS];
        for (int i = 0; i < TRIPLE_COMPONENTS; i++) {
            try {
                values[i] = Double.parseDouble(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid number in " + description + " attribute: " + attribute, e);
            }
        }
        return values;
    }

    /**
     * Parses a color from a "R G B" attribute.
     *
     * @param attribute the color attribute
     * @return the parsed color
     */
    static Color parseColor(String attribute) {
        double[] c = parseTriple(attribute, "color");
        return new Color(c[0], c[1], c[2]);
    }

    /**
     * Parses a point from an "x y z" attribute.
     *
     * @param attribute the point attribute
     * @return the parsed point
     */
    static Point parsePoint(String attribute) {
        double[] c = parseTriple(attribute, "point");
        return new Point(c[0], c[1], c[2]);
    }

    /**
     * Parses a vector from an "x y z" attribute.
     *
     * @param attribute the vector attribute
     * @return the parsed vector
     */
    static Vector parseVector(String attribute) {
        double[] c = parseTriple(attribute, "vector");
        return new Vector(c[0], c[1], c[2]);
    }

    /**
     * Parses a single scalar attribute.
     *
     * @param attribute   the scalar attribute
     * @param description human-readable name of the attribute (used in error messages)
     * @return the parsed value
     * @throws IllegalArgumentException if the attribute is null or malformed
     */
    static double parseDouble(String attribute, String description) {
        if (attribute == null) {
            throw new IllegalArgumentException("Missing " + description + " attribute");
        }
        try {
            return Double.parseDouble(attribute.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid number in " + description + " attribute: " + attribute, e);
        }
    }
}
