package net.bummy1337.daintegrate.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DrawBatchesTest {
    @Test
    void turnsClockwiseTrianglesCounterClockwise() {
        // Top-left, top-right, bottom-right: clockwise on screen, as ImGui emits rectangles
        float[] vertices = {0, 0, 0.1f, 0.1f, 10, 0, 0.2f, 0.2f, 10, 10, 0.3f, 0.3f};
        int[] colors = {1, 2, 3};
        DrawBatches.faceFront(vertices, colors);
        assertArrayEquals(new float[]{0, 0, 0.1f, 0.1f, 10, 10, 0.3f, 0.3f, 10, 0, 0.2f, 0.2f}, vertices);
        assertArrayEquals(new int[]{1, 3, 2}, colors);
    }

    @Test
    void keepsCounterClockwiseTriangles() {
        // Top-left, bottom-left, bottom-right: the game's own winding
        float[] vertices = {0, 0, 0, 0, 0, 10, 0, 1, 10, 10, 1, 1};
        int[] colors = {1, 2, 3};
        DrawBatches.faceFront(vertices, colors);
        assertArrayEquals(new float[]{0, 0, 0, 0, 0, 10, 0, 1, 10, 10, 1, 1}, vertices);
        assertArrayEquals(new int[]{1, 2, 3}, colors);
    }
}
