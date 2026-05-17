package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

public class SpotLight extends PointLight {

    private final Vector _direction;

    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        _direction = direction.normalize();
    }

    public SpotLight setKC(double kc) {
        return (SpotLight) super.setKC(kc);
    }

    public SpotLight setKL(double kl) {
        return (SpotLight) super.setKC(kl);
    }

    public SpotLight setKQ(double kq) {
        return (SpotLight) super.setKC(kq);
    }
}
