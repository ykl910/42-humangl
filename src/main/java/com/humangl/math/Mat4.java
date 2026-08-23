package com.humangl.math;

import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryStack;

public final class Mat4 {
    private final float[] values;

    private Mat4(float[] values) {
        this.values = values;
    }

    public static Mat4 identity() {
        float[] values = new float[16];
        values[0] = values[5] = values[10] = values[15] = 1f;
        return new Mat4(values);
    }

    public Mat4 copy() {
        return new Mat4(values.clone());
    }

    public Mat4 multiply(Mat4 right) {
        float[] result = new float[16];
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                for (int index = 0; index < 4; index++) {
                    result[column * 4 + row] += values[index * 4 + row]
                            * right.values[column * 4 + index];
                }
            }
        }
        return new Mat4(result);
    }

    public Mat4 translate(float x, float y, float z) {
        Mat4 translation = identity();
        translation.values[12] = x;
        translation.values[13] = y;
        translation.values[14] = z;
        return multiply(translation);
    }

    public Mat4 scale(float x, float y, float z) {
        Mat4 scale = identity();
        scale.values[0] = x;
        scale.values[5] = y;
        scale.values[10] = z;
        return multiply(scale);
    }

    public Mat4 rotateX(float angle) {
        float sine = (float) Math.sin(angle);
        float cosine = (float) Math.cos(angle);
        Mat4 rotation = identity();
        rotation.values[5] = cosine;
        rotation.values[6] = sine;
        rotation.values[9] = -sine;
        rotation.values[10] = cosine;
        return multiply(rotation);
    }

    public Mat4 rotateY(float angle) {
        float sine = (float) Math.sin(angle);
        float cosine = (float) Math.cos(angle);
        Mat4 rotation = identity();
        rotation.values[0] = cosine;
        rotation.values[2] = -sine;
        rotation.values[8] = sine;
        rotation.values[10] = cosine;
        return multiply(rotation);
    }

    public Mat4 rotateZ(float angle) {
        float sine = (float) Math.sin(angle);
        float cosine = (float) Math.cos(angle);
        Mat4 rotation = identity();
        rotation.values[0] = cosine;
        rotation.values[1] = sine;
        rotation.values[4] = -sine;
        rotation.values[5] = cosine;
        return multiply(rotation);
    }

    public static Mat4 perspective(float fieldOfView, float aspect, float near, float far) {
        float scale = 1f / (float) Math.tan(fieldOfView / 2f);
        float[] values = new float[16];
        values[0] = scale / aspect;
        values[5] = scale;
        values[10] = -(far + near) / (far - near);
        values[11] = -1f;
        values[14] = -(2f * far * near) / (far - near);
        return new Mat4(values);
    }

    public static Mat4 lookAt(float eyeX, float eyeY, float eyeZ,
                              float centerX, float centerY, float centerZ) {
        float forwardX = centerX - eyeX;
        float forwardY = centerY - eyeY;
        float forwardZ = centerZ - eyeZ;
        float forwardLength = length(forwardX, forwardY, forwardZ);
        forwardX /= forwardLength; forwardY /= forwardLength; forwardZ /= forwardLength;

        float sideX = forwardZ;
        float sideY = 0f;
        float sideZ = -forwardX;
        float sideLength = length(sideX, sideY, sideZ);
        sideX /= sideLength; sideY /= sideLength; sideZ /= sideLength;
        float upX = sideY * forwardZ - sideZ * forwardY;
        float upY = sideZ * forwardX - sideX * forwardZ;
        float upZ = sideX * forwardY - sideY * forwardX;

        Mat4 view = identity();
        view.values[0] = sideX; view.values[4] = sideY; view.values[8] = sideZ;
        view.values[1] = upX; view.values[5] = upY; view.values[9] = upZ;
        view.values[2] = -forwardX; view.values[6] = -forwardY; view.values[10] = -forwardZ;
        return view.translate(-eyeX, -eyeY, -eyeZ);
    }

    private static float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public void put(FloatBuffer buffer) {
        buffer.put(values).flip();
    }

    public FloatBuffer toBuffer() {
        FloatBuffer buffer = MemoryStack.stackMallocFloat(16);
        put(buffer);
        return buffer;
    }
}
