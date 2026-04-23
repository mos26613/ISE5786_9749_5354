package parser;

import java.io.IOException;

import scene.Scene;

/**
 * Parses a {@link Scene} description from an external resource
 * (JSON, XML, ...).
 * <p>
 * Concrete implementations are responsible for locating the resource and
 * parsing its contents into a fully-populated {@code Scene}. The argument
 * passed to {@link #parse(String)} is the bare resource name without any
 * format-specific suffix — each parser appends its own extension.
 * <p>
 * The interface is intentionally minimal so that future scene elements
 * (lights, materials, additional geometries) can be added without changing
 * the contract. New element categories live in their own factory class
 * (see {@link GeometryFactory}) so that extension does not modify existing
 * callers.
 */
public interface SceneParser {
    /**
     * Parses the named resource into a {@link Scene}.
     *
     * @param resourceName resource base name (without extension)
     * @return the populated scene
     * @throws IOException if the resource cannot be read or the contents are malformed
     */
    Scene parse(String resourceName) throws IOException;
}
