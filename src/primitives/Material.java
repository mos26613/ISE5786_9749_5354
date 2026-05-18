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
     * Specular coefficient represents the proportion of specular light reflected by the material.
     */
    public Double3 kS = Double3.ZERO;
    /**
     * Diffuse coefficient represents the proportion of diffuse light reflected by the material.
     */
    public Double3 kD = Double3.ZERO;
    /**
     * Shininess represents the shininess of the material.
     */
    public int nShininess = 0;

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

    /**
     * Sets the specular reflection coefficient using a Double3 object.
     *
     * @param kS The Double3 object representing the specular reflection coefficient.
     * @return The Material instance with the updated specular reflection coefficient.
     */
    public Material setKS(Double3 kS) {
        this.kS = kS;
        return this;
    }

    /**
     * Sets the specular reflection coefficient using a double value.
     *
     * @param d The double value representing the specular reflection coefficient, which will be converted to a Double3 object.
     * @return The Material instance with the updated specular reflection coefficient.
     */
    public Material setKS(double d) {
        this.kS = new Double3(d);
        return this;
    }

    /**
     * Sets the diffuse reflection coefficient using a Double3 object.
     *
     * @param kD The Double3 object representing the diffuse reflection coefficient.
     * @return The Material instance with the updated diffuse reflection coefficient.
     */
    public Material setKD(Double3 kD) {
        this.kD = kD;
        return this;
    }

    /**
     * Sets the diffuse reflection coefficient using a double value.
     *
     * @param d The double value representing the diffuse reflection coefficient, which will be converted to a Double3 object.
     * @return The Material instance with the updated diffuse reflection coefficient.
     */
    public Material setKD(double d) {
        this.kD = new Double3(d);
        return this;
    }

    /**
     * Sets the shininess of the material.
     *
     * @param nShininess The shininess value to set for the material.
     * @return The Material instance with the updated shininess.
     */
    public Material setShininess(int nShininess) {
        this.nShininess = nShininess;
        return this;
    }
}
