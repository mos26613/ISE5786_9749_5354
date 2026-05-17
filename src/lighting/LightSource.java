package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

public interface LightSource {
    Vector getL(Point p);
    Color getIntensity(Point p);
}
