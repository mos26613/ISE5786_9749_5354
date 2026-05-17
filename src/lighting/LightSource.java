package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

public interface LightSource {
    public Vector getL(Point p);
    public Color getIntensity(Point p);
}
