package com.humangl;

import com.humangl.math.MatrixStack;

public final class Humanoid {
    private final Cube cube;
    private final ShaderProgram shader;
    private final MatrixStack stack = new MatrixStack();
    private float torsoRotation;
    private float heightOffset;
    private float upperArmLength = 1.05f;
    private float upperLegLength = 1.35f;

    public Humanoid(Cube cube, ShaderProgram shader) {
        this.cube = cube;
        this.shader = shader;
    }

    public void draw(float time, float speed, boolean rotateTorso, boolean jumping) {
        float stride = (float) Math.sin(time * speed);
        float jump = jumping ? Math.max(0f, (float) Math.sin(time * 1.5f)) * 0.7f : 0f;
        heightOffset = jump;
        torsoRotation = rotateTorso ? (float) Math.sin(time * 0.7f) * 0.25f : 0f;
        stack.loadIdentity();
        stack.translate(0f, heightOffset, 0f);
        stack.rotateY(torsoRotation);
        drawTorso();
        drawHead();
        drawArm(-1f, stride);
        drawArm(1f, -stride);
        drawLeg(-1f, -stride);
        drawLeg(1f, stride);
    }

    public void setUpperArmLength(float length) {
        if (length <= 0f) throw new IllegalArgumentException("Arm length must be positive");
        upperArmLength = length;
    }

    public void setUpperLegLength(float length) {
        if (length <= 0f) throw new IllegalArgumentException("Leg length must be positive");
        upperLegLength = length;
    }

    private void drawTorso() {
        stack.push(); stack.translate(0f, 2.75f, 0f); stack.scale(1.35f, 1.9f, 0.7f);
        drawPart(0.19f, 0.46f, 0.62f); stack.pop();
    }

    private void drawHead() {
        stack.push(); stack.translate(0f, 4.65f, 0f); stack.scale(0.82f, 0.82f, 0.82f);
        drawPart(0.91f, 0.72f, 0.45f); stack.pop();
    }

    private void drawArm(float side, float stride) {
        stack.push();
        stack.translate(side * 1.02f, 3.35f, 0f);
        stack.rotateZ(side * (0.15f + stride * 0.55f));
        drawUpperArm();
        stack.translate(0f, -upperArmLength, 0f);
        stack.rotateZ(-side * stride * 0.25f);
        drawForearm();
        stack.pop();
    }

    private void drawUpperArm() {
        stack.push(); stack.translate(0f, -upperArmLength * 0.5f, 0f); stack.scale(0.42f, upperArmLength, 0.42f);
        drawPart(0.19f, 0.46f, 0.62f); stack.pop();
    }

    private void drawForearm() {
        float length = 0.9f;
        stack.push(); stack.translate(0f, -length * 0.5f, 0f); stack.scale(0.36f, length, 0.36f);
        drawPart(0.25f, 0.55f, 0.72f); stack.pop();
    }

    private void drawLeg(float side, float stride) {
        stack.push();
        stack.translate(side * 0.55f, 1.85f, 0f);
        stack.rotateZ(side * stride * 0.5f);
        drawThigh();
        stack.translate(0f, -upperLegLength, 0f);
        stack.rotateZ(-side * Math.max(0f, -stride) * 0.2f);
        drawLowerLeg();
        stack.pop();
    }

    private void drawThigh() {
        stack.push(); stack.translate(0f, -upperLegLength * 0.5f, 0f); stack.scale(0.5f, upperLegLength, 0.5f);
        drawPart(0.19f, 0.46f, 0.62f); stack.pop();
    }

    private void drawLowerLeg() {
        float length = 1.2f;
        stack.push(); stack.translate(0f, -length * 0.5f, 0f); stack.scale(0.42f, length, 0.42f);
        drawPart(0.25f, 0.55f, 0.72f); stack.pop();
    }

    private void drawPart(float red, float green, float blue) {
        shader.use(Renderer.projectionView, stack.current(), red, green, blue);
        cube.draw();
    }
}
