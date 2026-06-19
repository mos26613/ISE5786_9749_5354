package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for the {@link AABB} axis-aligned bounding box: its
 * {@link AABB#intersects(Ray)} boolean slab test and the
 * {@link AABB#around}/{@link AABB#expand(double)}/{@link AABB#union(AABB)} builders.
 */
class AABBTests {
    /** A 2x2x2 box spanning (0,0,0) to (2,2,2), reused across the ray tests. */
    private static final AABB BOX = new AABB(new Point(0, 0, 0), new Point(2, 2, 2));
    private static final Vector V100 = new Vector(1, 0, 0);

    private static final String SHOULD_HIT = "Ray should meet the box";
    private static final String SHOULD_MISS = "Ray should miss the box";
    private static final String WRONG_BOX = "Built box has wrong bounds";

    /** Default constructor to satisfy the Javadoc tool. */
    AABBTests() {
    }

    /**
     * Test method for {@link AABB#intersects(Ray)}.
     */
    @Test
    void testIntersects() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: ray from outside aimed straight through the box
        assertTrue(BOX.intersects(new Ray(new Point(-1, 1, 1), V100)), SHOULD_HIT);
        // EP02: ray from outside aimed clear of the box (passes above it)
        assertFalse(BOX.intersects(new Ray(new Point(-1, 3, 1), V100)), SHOULD_MISS);

        // =============== Boundary Values Tests ==================
        // BV01: ray origin inside the box
        assertTrue(BOX.intersects(new Ray(new Point(1, 1, 1), V100)), SHOULD_HIT);
        // BV02: ray parallel to the X faces, passing through the box (inside the y/z slabs)
        assertTrue(BOX.intersects(new Ray(new Point(-1, 1, 1), V100)), SHOULD_HIT);
        // BV03: ray parallel to the X faces but outside the y slab
        assertFalse(BOX.intersects(new Ray(new Point(-1, 5, 1), V100)), SHOULD_MISS);
        // BV04: box lies entirely behind the ray origin
        assertFalse(BOX.intersects(new Ray(new Point(5, 1, 1), V100)), SHOULD_MISS);
        // BV05: ray grazing along a bottom edge of the box (boundary — kept, conservative)
        assertTrue(BOX.intersects(new Ray(new Point(-1, 0, 0), V100)), SHOULD_HIT);
    }

    /**
     * Test method for {@link AABB#around(Point...)}.
     */
    @Test
    void testAround() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: tight box around two points takes per-axis min and max
        assertEquals(new AABB(new Point(-1, 2, 0), new Point(1, 5, 3)),
                AABB.around(new Point(1, 2, 3), new Point(-1, 5, 0)), WRONG_BOX);
    }

    /**
     * Test method for {@link AABB#expand(double)}.
     */
    @Test
    void testExpand() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: expanding grows the box by the margin on every side
        assertEquals(new AABB(new Point(-1, -1, -1), new Point(3, 3, 3)),
                BOX.expand(1), WRONG_BOX);
    }

    /**
     * Test method for {@link AABB#union(AABB)}.
     */
    @Test
    void testUnion() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: union covers both boxes
        AABB other = new AABB(new Point(1, 1, 1), new Point(5, 5, 5));
        assertEquals(new AABB(new Point(0, 0, 0), new Point(5, 5, 5)),
                BOX.union(other), WRONG_BOX);
    }
}
