package net.bummy1337.daintegrate.ui;

import imgui.ImDrawList;

/**
 * Line icons drawn with the draw list, so the menu needs no icon font.
 * Every icon is centred on (cx, cy) and fits a square of the given size.
 */
final class Icons {
    enum Kind { Home, Plug, Bolt, Gear, Question, Close, Heart, Plus, Play, Copy, Trash, Up, Down }

    private Icons() {
    }

    static void draw(ImDrawList d, Kind kind, float cx, float cy, float size, int color) {
        float h = size / 2;
        float t = Math.max(1.5f, size / 10f);
        switch (kind) {
            case Home -> {
                d.addLine(cx - h, cy - h * 0.05f, cx, cy - h * 0.9f, color, t);
                d.addLine(cx, cy - h * 0.9f, cx + h, cy - h * 0.05f, color, t);
                d.addRect(cx - h * 0.65f, cy - h * 0.2f, cx + h * 0.65f, cy + h * 0.85f, color, h * 0.15f, t);
            }
            case Plug -> {
                d.addLine(cx - h * 0.35f, cy - h, cx - h * 0.35f, cy - h * 0.4f, color, t);
                d.addLine(cx + h * 0.35f, cy - h, cx + h * 0.35f, cy - h * 0.4f, color, t);
                d.addRect(cx - h * 0.7f, cy - h * 0.4f, cx + h * 0.7f, cy + h * 0.25f, color, h * 0.3f, t);
                d.addLine(cx, cy + h * 0.25f, cx, cy + h, color, t);
            }
            case Bolt -> {
                d.addLine(cx + h * 0.25f, cy - h, cx - h * 0.55f, cy + h * 0.1f, color, t);
                d.addLine(cx - h * 0.55f, cy + h * 0.1f, cx + h * 0.1f, cy + h * 0.1f, color, t);
                d.addLine(cx + h * 0.1f, cy + h * 0.1f, cx - h * 0.25f, cy + h, color, t);
                d.addLine(cx - h * 0.25f, cy + h, cx + h * 0.55f, cy - h * 0.1f, color, t);
                d.addLine(cx + h * 0.55f, cy - h * 0.1f, cx - h * 0.1f, cy - h * 0.1f, color, t);
                d.addLine(cx - h * 0.1f, cy - h * 0.1f, cx + h * 0.25f, cy - h, color, t);
            }
            case Gear -> {
                d.addCircle(cx, cy, h * 0.62f, color, 0, t);
                d.addCircleFilled(cx, cy, h * 0.2f, color);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI / 4 * i;
                    float c = (float) Math.cos(a);
                    float sn = (float) Math.sin(a);
                    d.addLine(cx + c * h * 0.62f, cy + sn * h * 0.62f, cx + c * h, cy + sn * h, color, t * 1.2f);
                }
            }
            case Question -> {
                d.addCircle(cx, cy, h * 0.95f, color, 0, t);
                d.pathArcTo(cx, cy - h * 0.2f, h * 0.32f, (float) Math.PI, (float) (Math.PI * 2.4));
                d.pathLineTo(cx, cy + h * 0.25f);
                d.pathStroke(color, 0, t);
                d.addCircleFilled(cx, cy + h * 0.55f, t * 0.75f, color);
            }
            case Close -> {
                d.addLine(cx - h, cy - h, cx + h, cy + h, color, t);
                d.addLine(cx + h, cy - h, cx - h, cy + h, color, t);
            }
            case Heart -> {
                float r = h * 0.48f;
                d.addCircleFilled(cx - r * 0.95f, cy - r * 0.35f, r, color);
                d.addCircleFilled(cx + r * 0.95f, cy - r * 0.35f, r, color);
                d.addTriangleFilled(cx - h * 0.98f, cy - r * 0.05f, cx + h * 0.98f, cy - r * 0.05f, cx, cy + h * 0.95f, color);
            }
            case Plus -> {
                d.addLine(cx - h, cy, cx + h, cy, color, t);
                d.addLine(cx, cy - h, cx, cy + h, color, t);
            }
            case Play -> d.addTriangleFilled(cx - h * 0.6f, cy - h, cx - h * 0.6f, cy + h, cx + h, cy, color);
            case Copy -> {
                d.addRect(cx - h, cy - h * 0.4f, cx + h * 0.4f, cy + h, color, h * 0.2f, t);
                d.addLine(cx - h * 0.4f, cy - h, cx + h, cy - h, color, t);
                d.addLine(cx + h, cy - h, cx + h, cy + h * 0.4f, color, t);
            }
            case Trash -> {
                d.addLine(cx - h, cy - h * 0.6f, cx + h, cy - h * 0.6f, color, t);
                d.addLine(cx - h * 0.3f, cy - h, cx + h * 0.3f, cy - h, color, t);
                d.addRect(cx - h * 0.7f, cy - h * 0.6f, cx + h * 0.7f, cy + h, color, h * 0.15f, t);
            }
            case Up -> {
                d.addLine(cx - h, cy + h * 0.45f, cx, cy - h * 0.45f, color, t);
                d.addLine(cx, cy - h * 0.45f, cx + h, cy + h * 0.45f, color, t);
            }
            case Down -> {
                d.addLine(cx - h, cy - h * 0.45f, cx, cy + h * 0.45f, color, t);
                d.addLine(cx, cy + h * 0.45f, cx + h, cy - h * 0.45f, color, t);
            }
        }
    }
}
