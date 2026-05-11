package primitives;

/**
 * Material class represents the material properties of a body, including its ambient coefficient (kA).
 */
public final class Material {
    /**
     * Default constructor to satisfy Javadoc tool.
     */
    public Material() {
    }

    /**
     * Ambient coefficient (kA) represents the proportion of ambient light reflected by the material.
     * It is initialized to Double3.ONE, indicating that the material reflects all ambient light by default.
     */
    public Double3 kA = Double3.ONE;

    /**
     * Sets the ambient reflection coefficient (kA) using a Double3 object.
     *
     * @param kA The Double3 object representing the ambient reflection coefficient.
     * @return The Material instance with the updated ambient reflection coefficient.
     */
    public Material setKA(Double3 kA) {
        this.kA = kA;
        return this;
    }

    /**
     * Sets the ambient reflection coefficient (kA) using a double value.
     *
     * @param d The double value representing the ambient reflection coefficient, which will be converted to a Double3 object.
     * @return The Material instance with the updated ambient reflection coefficient.
     */
    public Material setKA(double d) {
        this.kA = new Double3(d);
        return this;
    }

}
