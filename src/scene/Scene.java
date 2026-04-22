package scene;

import geometries.impl.Geometries;
import lighting.AmbientLight;
import primitives.Color;

/**
 * Represents a 3D scene to be rendered. This a PDS (Passive Data Structure).
 */
public class Scene {
    /**
     * Scene name (used for identification and debugging purposes)
     */
    public String name;
    /**
     * Background color of the scene (used when rays do not intersect any geometry)
     */
    public Color background = Color.BLACK;
    /**
     * Ambient light in the scene (used as a default light source when no other lights are defined)
     */
    public AmbientLight ambientLight = AmbientLight.NONE;
    /**
     * Collection of geometries in the scene (used for ray tracing and intersection tests)
     */
    public Geometries geometries = new Geometries();

    /**
     * Creates a new scene with the given name.
     * @param name the name of the scene
     */
    public Scene(String name) {
        this.name = name;
    }

    /**
     * Sets the name of the scene.
     * @param nme the new name for the scene
     * @return this scene (for method chaining)
     */
    public Scene setName(String nme) {
        this.name = nme;
        return this;
    }

    /**
     * Sets the background color of the scene.
     * @param background the new background color for the scene
     * @return this scene (for method chaining)
     */
    public Scene setBackground(Color background) {
        this.background = background;
        return this;
    }

    /**
     * Sets the ambient light of the scene.
     * @param ambientLight the new ambient light for the scene
     * @return this scene (for method chaining)
     */
    public Scene setAmbientLight(AmbientLight ambientLight) {
        this.ambientLight = ambientLight;
        return this;
    }

    /**
     * Sets the geometries of the scene.
     * @param geometries the new geometries for the scene
     * @return this scene (for method chaining)
     */
    public Scene setGeometries(Geometries geometries) {
        this.geometries = geometries;
        return this;
    }
}
