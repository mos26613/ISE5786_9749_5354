package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

public class PointLight extends Light implements LightSource {

    protected final Point _position;
    private double _kC = 1;
    private double _kL = 0;
    private double _kQ = 0;

    public PointLight(Color intensity, Point position) {
        super(intensity);
        _position = position;
    }

    @Override
    public Vector getL(Point p) {
        return null;
    }

    @Override
    public Color getIntensity(Point p) {
        return null;
    }

    public PointLight setKC(double kc) {
        _kC = kc;
        return this;
    }

    public PointLight setKL(double kl) {
        _kL = kl;
        return this;
    }

    public PointLight setKQ(double kq) {
        _kQ = kq;
        return this;
    }
}
