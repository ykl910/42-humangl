package com.humangl;

import com.humangl.math.MatrixStack;

public final class Humanoid {
    private final Cube cube;
    private final ShaderProgram shader;
    private final MatrixStack stack = new MatrixStack();
    private float torsoRotation;
    private float torsoSpinStartTime = -1f;
    private float torsoSpinDirection;
    private float heightOffset;
    private float upperArmLength = 1.05f;
    private float upperLegLength = 1.35f;
    private static final float ARM_SWING = 0.75f;
    private static final float ELBOW_BEND = 0.6f;
    private static final float ARM_PHASE_OFFSET = 0.12f;
    private static final float LEG_SWING = 0.55f;
    private static final float KNEE_BEND = 0.7f;
    private static final float FOOT_LENGTH = 0.55f;
    private static final float FOOT_HEIGHT = 0.18f;
    private static final float FOOT_FORWARD = 0.16f;
    private static final float FOOT_SWING = 0.25f;
    private static final float FOOT_LIFT = 0.12f;
    private static final float JUMP_DURATION = 0.9f;
    private static final float JUMP_HEIGHT = 0.7f;
    private boolean jumpInProgress;
    private float jumpStartTime;

    public Humanoid(Cube cube, ShaderProgram shader) {
        this.cube = cube;
        this.shader = shader;
    }

    public void draw(float time, float speed, float torsoDirection, boolean jumpRequested) {
        float jumpPose = 0f;
        float jumpCrouch = 0f;
        float jumpArmRaise = 0f;
        float jumpKneeBend = 0f;
        float jumpFootTilt = 0f;
        boolean walking = Math.abs(speed) > 0.0001f;
        if (jumpRequested && !jumpInProgress && !walking) {
            jumpInProgress = true;
            jumpStartTime = time;
        }
        if (jumpInProgress) {
            float progress = (time - jumpStartTime) / JUMP_DURATION;
            if (progress >= 1f) {
                jumpInProgress = false;
                heightOffset = 0f;
            } else {
                jumpPose = (float) Math.sin(Math.PI * progress);
                jumpCrouch = progress < 0.2f ? progress / 0.2f : progress > 0.8f ? (1f - progress) / 0.2f : 0f;
                jumpArmRaise = Math.min(1f, progress / 0.18f);
                jumpKneeBend = Math.max(jumpPose, jumpCrouch);
                jumpFootTilt = jumpArmRaise * 0.22f;
                heightOffset = jumpPose * JUMP_HEIGHT - jumpCrouch * 0.18f;
            }
        } else {
            heightOffset = 0f;
        }
        float effectiveSpeed = jumpInProgress ? 0f : speed;
        float walkPhase = time * effectiveSpeed;
        float gait = (float) Math.sin(walkPhase);
        float armBase = (float) Math.sin(walkPhase + ARM_PHASE_OFFSET);
        float leftStep = gait;
        float rightStep = -gait;
        float leftLift = positive(leftStep);
        float rightLift = positive(rightStep);
        if (torsoDirection != 0f && torsoSpinStartTime < 0f) {
            torsoSpinStartTime = time;
            torsoSpinDirection = torsoDirection;
        }
        if (torsoSpinStartTime >= 0f) {
            float spinProgress = (time - torsoSpinStartTime) / 0.6f;
            if (spinProgress >= 1f) {
                torsoRotation = torsoSpinDirection * (float) (Math.PI * 2.0);
                torsoSpinStartTime = -1f;
                torsoSpinDirection = 0f;
            } else {
                torsoRotation = torsoSpinDirection * (float) (Math.PI * 2.0) * spinProgress;
            }
        } else {
            torsoRotation = 0f;
        }
        stack.loadIdentity();
        stack.translate(0f, heightOffset, 0f);
        stack.rotateY(torsoRotation);
        drawTorso();
        drawHead();
        drawArm(-1f, -armBase, jumpArmRaise);
        drawArm(1f, armBase, jumpArmRaise);
        drawLeg(-1f, leftStep, leftLift, jumpKneeBend, jumpFootTilt, jumpCrouch);
        drawLeg(1f, rightStep, rightLift, jumpKneeBend, jumpFootTilt, jumpCrouch);
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
        stack.push();
        stack.translate(0f, 2.75f, 0f);
        stack.scale(1.35f, 1.9f, 0.7f);
        drawPart(0.19f, 0.46f, 0.62f);
        stack.pop();
    }

    private void drawHead() {
        stack.push();
        stack.translate(0f, 4.65f, 0f);
        stack.scale(0.82f, 0.82f, 0.82f);
        drawPart(0.91f, 0.72f, 0.45f);
        stack.pop();
    }

    private void drawArm(float side, float swing, float jumpArmRaise) {
        stack.push();
        stack.translate(side * 1.02f, 3.35f, 0f);
        stack.rotateX(swing * ARM_SWING - jumpArmRaise * 2.2f);
        drawUpperArm();
        stack.translate(0f, -upperArmLength, 0f);
        stack.rotateX(-positive(-swing) * ELBOW_BEND - jumpArmRaise * 0.55f);
        stack.rotateZ(side * positive(-swing) * 0.05f);
        drawForearm();
        stack.pop();
    }

    private void drawUpperArm() {
        stack.push();
        stack.translate(0f, -upperArmLength * 0.5f, 0f);
        stack.scale(0.42f, upperArmLength, 0.42f);
        drawPart(0.19f, 0.46f, 0.62f);
        stack.pop();
    }

    private void drawForearm() {
        float length = 0.9f;
        stack.push();
        stack.translate(0f, -length * 0.5f, 0f);
        stack.scale(0.36f, length, 0.36f);
        drawPart(0.25f, 0.55f, 0.72f);
        stack.pop();
    }

    private void drawLeg(float side, float step, float lift, float jumpKneeBend, float jumpFootTilt, float jumpCrouch) {
        float kneeBend = positive(-step) * KNEE_BEND + jumpKneeBend * 0.85f;
        stack.push();
        stack.translate(side * 0.55f, 1.85f - jumpCrouch * 0.18f, 0f);
        stack.rotateX(step * LEG_SWING - jumpCrouch * 0.75f);
        drawThigh();
        stack.translate(0f, -upperLegLength, 0f);
        stack.rotateX(kneeBend);
        drawLowerLeg();
        stack.translate(0f, -1.2f, 0f);
        float footAngle = step * FOOT_SWING;
        float footLift = positive(-step) * FOOT_LIFT;
        stack.translate(0f, footLift + jumpCrouch * 0.06f, 0f);
        stack.rotateX(footAngle + jumpFootTilt);
        drawFoot();
        stack.pop();
    }

    private static float positive(float value) {
        return Math.max(0f, value);
    }

    private void drawThigh() {
        stack.push();
        stack.translate(0f, -upperLegLength * 0.5f, 0f);
        stack.scale(0.5f, upperLegLength, 0.5f);
        drawPart(0.19f, 0.46f, 0.62f);
        stack.pop();
    }

    private void drawLowerLeg() {
        float length = 1.2f;
        stack.push();
        stack.translate(0f, -length * 0.5f, 0f);
        stack.scale(0.42f, length, 0.42f);
        drawPart(0.25f, 0.55f, 0.72f);
        stack.pop();
    }

    private void drawFoot() {
        stack.push();
        stack.translate(0f, -FOOT_HEIGHT * 0.5f, FOOT_LENGTH * 0.5f);
        stack.scale(0.48f, FOOT_HEIGHT, FOOT_LENGTH);
        drawPart(0.18f, 0.18f, 0.2f);
        stack.pop();
    }

    private void drawPart(float red, float green, float blue) {
        shader.use(Renderer.projectionView, stack.current(), red, green, blue);
        cube.draw();
    }
}
