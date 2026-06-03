package parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import geometries.api.Geometry;
import geometries.impl.Geometries;
import lighting.AmbientLight;
import lighting.LightSource;
import scene.Scene;

/**
 * Parses a {@link Scene} from a JSON file.
 * <p>
 * JSON files are read from the {@code json/} directory at the project root.
 * The resource name passed to {@link #parse(String)} is the file's base name
 * (no extension); this parser appends {@code .json}.
 * <p>
 * The schema mirrors the course XML layout: attribute values are strings
 * holding whitespace-separated numbers, delegated to {@link AttributeParser}
 * for conversion, and to {@link GeometryFactory} for constructing geometry
 * objects.
 * <p>
 * The class is structured so that future stages can extend it without
 * modifying the existing reader methods:
 * <ul>
 *   <li>A new geometry kind → add a {@code readXxx} private method that
 *       reads its JSON array and a matching factory method.</li>
 *   <li>A light source → add a {@code lights} section reader and a
 *       {@code LightFactory} (parallel to {@link GeometryFactory}).</li>
 *   <li>A material attached to a geometry → extend the existing
 *       {@code readXxx} method to pick up the optional material object.</li>
 * </ul>
 */
public final class JsonSceneParser implements SceneParser {
    /**
     * Directory holding JSON scene files, relative to the project root.
     */
    private static final String JSON_DIR = "json";
    /**
     * File extension appended to the resource name.
     */
    private static final String JSON_EXT = ".json";
    /**
     * Root object key.
     */
    private static final String KEY_SCENE = "scene";
    /**
     * Scene name key.
     */
    private static final String KEY_NAME = "name";
    /**
     * Background color key.
     */
    private static final String KEY_BACKGROUND = "background-color";
    /**
     * Ambient-light object key.
     */
    private static final String KEY_AMBIENT = "ambient-light";
    /**
     * Ambient-light color attribute key.
     */
    private static final String KEY_AMBIENT_COLOR = "color";
    /**
     * Geometries object key.
     */
    private static final String KEY_GEOMETRIES = "geometries";
    /**
     * Sphere array key.
     */
    private static final String KEY_SPHERE = "sphere";
    /**
     * Sphere center attribute key.
     */
    private static final String KEY_CENTER = "center";
    /**
     * Sphere radius attribute key.
     */
    private static final String KEY_RADIUS = "radius";
    /**
     * Triangle array key.
     */
    private static final String KEY_TRIANGLE = "triangle";
    /**
     * Polygon array key.
     */
    private static final String KEY_POLYGON = "polygon";
    /**
     * Prefix of the numbered polygon-vertex attribute keys ({@code p0}, {@code p1}, ...).
     */
    private static final String KEY_VERTEX_PREFIX = "p";
    /**
     * Plane array key.
     */
    private static final String KEY_PLANE = "plane";
    /**
     * First-point attribute key (triangle or three-point plane).
     */
    private static final String KEY_P0 = "p0";
    /**
     * Second-point attribute key.
     */
    private static final String KEY_P1 = "p1";
    /**
     * Third-point attribute key.
     */
    private static final String KEY_P2 = "p2";
    /**
     * Plane reference-point attribute key (point-and-normal constructor).
     */
    private static final String KEY_POINT = "point";
    /**
     * Plane normal-vector attribute key.
     */
    private static final String KEY_NORMAL = "normal";
    /**
     * Tube array key.
     */
    private static final String KEY_TUBE = "tube";
    /**
     * Cylinder array key.
     */
    private static final String KEY_CYLINDER = "cylinder";
    /**
     * Axis reference-point attribute key (tube and cylinder).
     */
    private static final String KEY_AXIS_POINT = "axis-point";
    /**
     * Axis direction-vector attribute key (tube and cylinder).
     */
    private static final String KEY_AXIS_DIRECTION = "axis-direction";
    /**
     * Cylinder height attribute key.
     */
    private static final String KEY_HEIGHT = "height";
    /**
     * Optional per-geometry emission-color attribute key.
     */
    private static final String KEY_EMISSION = "emission";
    /**
     * Optional per-geometry material object key.
     */
    private static final String KEY_MATERIAL = "material";
    /**
     * Material ambient attenuation coefficient (kA) attribute key.
     */
    private static final String KEY_KA = "kA";
    /**
     * Material diffuse attenuation coefficient (kD) attribute key.
     */
    private static final String KEY_KD = "kD";
    /**
     * Material specular attenuation coefficient (kS) attribute key.
     */
    private static final String KEY_KS = "kS";
    /**
     * Material shininess exponent attribute key.
     */
    private static final String KEY_SHININESS = "shininess";
    /**
     * Material transparency attenuation coefficient (kT) attribute key.
     */
    private static final String KEY_KT = "kT";
    /**
     * Material reflection attenuation coefficient (kR) attribute key.
     */
    private static final String KEY_KR = "kR";
    /**
     * Lights object key.
     */
    private static final String KEY_LIGHTS = "lights";
    /**
     * Directional-light array key.
     */
    private static final String KEY_DIRECTIONAL = "directional";
    /**
     * Point-light array key.
     */
    private static final String KEY_POINT_LIGHT = "point";
    /**
     * Spot-light array key.
     */
    private static final String KEY_SPOT = "spot";
    /**
     * Light intensity-color attribute key.
     */
    private static final String KEY_INTENSITY = "intensity";
    /**
     * Light direction-vector attribute key (directional and spot).
     */
    private static final String KEY_DIRECTION = "direction";
    /**
     * Light position attribute key (point and spot).
     */
    private static final String KEY_POSITION = "position";
    /**
     * Light constant-attenuation factor (kC) attribute key.
     */
    private static final String KEY_KC = "kC";
    /**
     * Light linear-attenuation factor (kL) attribute key.
     */
    private static final String KEY_KL = "kL";
    /**
     * Light quadratic-attenuation factor (kQ) attribute key.
     */
    private static final String KEY_KQ = "kQ";

    /**
     * Default constructor.
     */
    public JsonSceneParser() { /* stateless */ }

    @Override
    public Scene parse(String resourceName) throws IOException {
        String text = Files.readString(Path.of(JSON_DIR, resourceName + JSON_EXT));
        JSONObject root = new JSONObject(text).getJSONObject(KEY_SCENE);

        Scene scene = new Scene(root.optString(KEY_NAME, resourceName));

        if (root.has(KEY_BACKGROUND)) {
            scene.setBackground(AttributeParser.parseColor(root.getString(KEY_BACKGROUND)));
        }
        if (root.has(KEY_AMBIENT)) {
            String colorAttr = root.getJSONObject(KEY_AMBIENT).getString(KEY_AMBIENT_COLOR);
            scene.setAmbientLight(new AmbientLight(AttributeParser.parseColor(colorAttr)));
        }
        if (root.has(KEY_GEOMETRIES)) {
            scene.setGeometries(readGeometries(root.getJSONObject(KEY_GEOMETRIES)));
        }
        if (root.has(KEY_LIGHTS)) {
            scene.lights.addAll(readLights(root.getJSONObject(KEY_LIGHTS)));
        }

        return scene;
    }

    /**
     * Reads all geometry arrays from the {@code geometries} object and returns
     * them wrapped in a single {@link Geometries} composite.
     *
     * @param node the JSON object under the {@code geometries} key
     * @return the populated {@code Geometries} composite
     */
    private static Geometries readGeometries(JSONObject node) {
        Geometries composite = new Geometries();
        readSpheres(node, composite);
        readTriangles(node, composite);
        readPolygons(node, composite);
        readPlanes(node, composite);
        readTubes(node, composite);
        readCylinders(node, composite);
        // Adding a new geometry kind → add one more readXxx call here.
        return composite;
    }

    /**
     * Reads all sphere entries from the geometries node.
     *
     * @param node      the geometries JSON object
     * @param composite the target composite collection
     */
    private static void readSpheres(JSONObject node, Geometries composite) {
        JSONArray items = node.optJSONArray(KEY_SPHERE);
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            composite.add(applyAppearance(item, GeometryFactory.buildSphere(
                    item.getString(KEY_CENTER),
                    item.getString(KEY_RADIUS))));
        }
    }

    /**
     * Reads all triangle entries from the geometries node.
     *
     * @param node      the geometries JSON object
     * @param composite the target composite collection
     */
    private static void readTriangles(JSONObject node, Geometries composite) {
        JSONArray items = node.optJSONArray(KEY_TRIANGLE);
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            composite.add(applyAppearance(item, GeometryFactory.buildTriangle(
                    item.getString(KEY_P0),
                    item.getString(KEY_P1),
                    item.getString(KEY_P2))));
        }
    }

    /**
     * Reads all polygon entries from the geometries node. Each entry lists its
     * vertices, in edge order, under numbered keys {@code p0}, {@code p1},
     * {@code p2}, ...; reading stops at the first missing index.
     *
     * @param node      the geometries JSON object
     * @param composite the target composite collection
     */
    private static void readPolygons(JSONObject node, Geometries composite) {
        JSONArray items = node.optJSONArray(KEY_POLYGON);
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            List<String> vertexAttrs = new ArrayList<>();
            for (int v = 0; item.has(KEY_VERTEX_PREFIX + v); v++) {
                vertexAttrs.add(item.getString(KEY_VERTEX_PREFIX + v));
            }
            composite.add(applyAppearance(item,
                    GeometryFactory.buildPolygon(vertexAttrs.toArray(new String[0]))));
        }
    }

    /**
     * Reads all plane entries from the geometries node. Each entry uses either
     * the {@code point}/{@code normal} form or the three-point form.
     *
     * @param node      the geometries JSON object
     * @param composite the target composite collection
     */
    private static void readPlanes(JSONObject node, Geometries composite) {
        JSONArray items = node.optJSONArray(KEY_PLANE);
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item.has(KEY_NORMAL)) {
                composite.add(applyAppearance(item, GeometryFactory.buildPlaneByPointNormal(
                        item.getString(KEY_POINT),
                        item.getString(KEY_NORMAL))));
            } else {
                composite.add(applyAppearance(item, GeometryFactory.buildPlaneByThreePoints(
                        item.getString(KEY_P0),
                        item.getString(KEY_P1),
                        item.getString(KEY_P2))));
            }
        }
    }

    /**
     * Reads all tube entries from the geometries node.
     *
     * @param node      the geometries JSON object
     * @param composite the target composite collection
     */
    private static void readTubes(JSONObject node, Geometries composite) {
        JSONArray items = node.optJSONArray(KEY_TUBE);
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            composite.add(applyAppearance(item, GeometryFactory.buildTube(
                    item.getString(KEY_AXIS_POINT),
                    item.getString(KEY_AXIS_DIRECTION),
                    item.getString(KEY_RADIUS))));
        }
    }

    /**
     * Reads all cylinder entries from the geometries node.
     *
     * @param node      the geometries JSON object
     * @param composite the target composite collection
     */
    private static void readCylinders(JSONObject node, Geometries composite) {
        JSONArray items = node.optJSONArray(KEY_CYLINDER);
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            composite.add(applyAppearance(item, GeometryFactory.buildCylinder(
                    item.getString(KEY_AXIS_POINT),
                    item.getString(KEY_AXIS_DIRECTION),
                    item.getString(KEY_RADIUS),
                    item.getString(KEY_HEIGHT))));
        }
    }

    /**
     * Applies the optional appearance attributes ({@code emission} color and
     * {@code material} with its coefficients) of a geometry JSON item to the given
     * geometry. Attributes that are absent leave the geometry's defaults.
     *
     * @param item     the geometry JSON object, possibly carrying appearance keys
     * @param geometry the freshly built geometry to decorate
     * @return the same geometry, decorated, for fluent use inside {@code composite.add(...)}
     */
    private static Geometry applyAppearance(JSONObject item, Geometry geometry) {
        if (item.has(KEY_EMISSION)) {
            geometry.setEmission(AttributeParser.parseColor(item.getString(KEY_EMISSION)));
        }
        if (item.has(KEY_MATERIAL)) {
            JSONObject material = item.getJSONObject(KEY_MATERIAL);
            geometry.setMaterial(GeometryFactory.buildMaterial(
                    material.optString(KEY_KA, null),
                    material.optString(KEY_KD, null),
                    material.optString(KEY_KS, null),
                    material.optString(KEY_SHININESS, null),
                    material.optString(KEY_KT, null),
                    material.optString(KEY_KR, null)));
        }
        return geometry;
    }

    /**
     * Reads all light arrays from the {@code lights} object.
     *
     * @param node the JSON object under the {@code lights} key
     * @return the list of parsed light sources
     */
    private static List<LightSource> readLights(JSONObject node) {
        List<LightSource> lights = new ArrayList<>();
        readDirectionalLights(node, lights);
        readPointLights(node, lights);
        readSpotLights(node, lights);
        // Adding a new light kind → add one more readXxxLights call here.
        return lights;
    }

    /**
     * Reads all directional-light entries from the lights node.
     *
     * @param node   the lights JSON object
     * @param lights the target light list
     */
    private static void readDirectionalLights(JSONObject node, List<LightSource> lights) {
        JSONArray items = node.optJSONArray(KEY_DIRECTIONAL);
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            lights.add(LightFactory.buildDirectional(
                    item.getString(KEY_INTENSITY),
                    item.getString(KEY_DIRECTION)));
        }
    }

    /**
     * Reads all point-light entries from the lights node.
     *
     * @param node   the lights JSON object
     * @param lights the target light list
     */
    private static void readPointLights(JSONObject node, List<LightSource> lights) {
        JSONArray items = node.optJSONArray(KEY_POINT_LIGHT);
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            lights.add(LightFactory.buildPoint(
                    item.getString(KEY_INTENSITY),
                    item.getString(KEY_POSITION),
                    item.optString(KEY_KC, null),
                    item.optString(KEY_KL, null),
                    item.optString(KEY_KQ, null)));
        }
    }

    /**
     * Reads all spot-light entries from the lights node.
     *
     * @param node   the lights JSON object
     * @param lights the target light list
     */
    private static void readSpotLights(JSONObject node, List<LightSource> lights) {
        JSONArray items = node.optJSONArray(KEY_SPOT);
        if (items == null) return;
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            lights.add(LightFactory.buildSpot(
                    item.getString(KEY_INTENSITY),
                    item.getString(KEY_POSITION),
                    item.getString(KEY_DIRECTION),
                    item.optString(KEY_KC, null),
                    item.optString(KEY_KL, null),
                    item.optString(KEY_KQ, null)));
        }
    }
}
