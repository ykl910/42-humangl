package com.humangl.math;

import java.util.ArrayDeque;
import java.util.Deque;

public final class MatrixStack {
    private final Deque<Mat4> stack = new ArrayDeque<>();
    private Mat4 current = Mat4.identity();

    public void push() {
        stack.push(current.copy());
    }

    public void pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Cannot pop an empty matrix stack");
        }
        current = stack.pop();
    }

    public void loadIdentity() {
        current = Mat4.identity();
    }

    public void translate(float x, float y, float z) { current = current.translate(x, y, z); }
    public void scale(float x, float y, float z) { current = current.scale(x, y, z); }
    public void rotateX(float angle) { current = current.rotateX(angle); }
    public void rotateY(float angle) { current = current.rotateY(angle); }
    public void rotateZ(float angle) { current = current.rotateZ(angle); }
    public Mat4 current() { return current; }
}
