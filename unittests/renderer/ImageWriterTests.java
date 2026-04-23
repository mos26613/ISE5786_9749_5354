package renderer;

import org.junit.jupiter.api.Test;
import primitives.Color;

/**
 * Unit tests for {@link ImageWriter}.
 */
class ImageWriterTests {
    /** Satisfy Javadoc tool */
    ImageWriterTests() {}
    /**
     * Image resolution (it is a rectangle: NxM)
     */
    private final static int N_X = 800;
    /**
     * Image resolution (it is a rectangle: NxM)
     */
    private final static int N_Y = 500;
    /**
     * Color of the grid lines (green)
     */
    private static final Color GRID_COLOR = new Color(0,255,0);
    /**
     * Color of the squares (red)
     */
    private static final Color SQUARE_COLOR = new Color(255,0,0);

    /**
     * Tests that the ImageWriter correctly writes pixel data to an image file.
     */
    @Test
    void testImageWriter() {
        ImageWriter iw = new ImageWriter(N_X, N_Y);
        for (int i=0; i < N_X; i++) {
            for (int j=0; j < N_Y; j++) {
                if (i % 50 == 0 || j % 50 == 0 || i == N_X - 1 || j == N_Y - 1) {
                    iw.writePixel(i, j, GRID_COLOR);
                } else {
                    iw.writePixel(i, j, SQUARE_COLOR);
                }
            }
        }
        iw.writeToImage("testImageWriter");
    }
}