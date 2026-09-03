package com.humangl;

import com.humangl.math.Mat4;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public final class Main {
    private static final float SPEED_SLOW = 1.0f;
    private static final float SPEED_NORMAL = 2.2f;
    private static final float SPEED_FAST = 4.0f;
    private static final float SPEED_FASTER = 6.0f;
    private long window;
    private float speed = SPEED_NORMAL;
    private float storedSpeed = speed;
    private boolean jumpRequested;
    private float torsoDirection;

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
        try {
            createWindow();
            loop();
        } finally {
            GLFW.glfwDestroyWindow(window);
            GLFW.glfwTerminate();
            GLFW.glfwSetErrorCallback(null).free();
        }
    }

    private void createWindow() {
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        window = GLFW.glfwCreateWindow(1100, 760, "humangl - skeletal animation", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) throw new IllegalStateException("Unable to create GLFW window");
        GLFW.glfwSetKeyCallback(window, (handle, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) GLFW.glfwSetWindowShouldClose(handle, true);
            if (key == GLFW.GLFW_KEY_R && action == GLFW.GLFW_PRESS) torsoDirection = -1f;
            if (key == GLFW.GLFW_KEY_T && action == GLFW.GLFW_PRESS) torsoDirection = 1f;
            if (key == GLFW.GLFW_KEY_A && action == GLFW.GLFW_PRESS) {
                speed = SPEED_SLOW;
                storedSpeed = speed;
            }
            if (key == GLFW.GLFW_KEY_S && action == GLFW.GLFW_PRESS) {
                speed = SPEED_NORMAL;
                storedSpeed = speed;
            }
            if (key == GLFW.GLFW_KEY_D && action == GLFW.GLFW_PRESS) {
                speed = SPEED_FAST;
                storedSpeed = speed;
            }
            if (key == GLFW.GLFW_KEY_F && action == GLFW.GLFW_PRESS) {
                speed = SPEED_FASTER;
                storedSpeed = speed;
            }
            if (key == GLFW.GLFW_KEY_E && action == GLFW.GLFW_PRESS) {
                if (speed > 0f) {
                    storedSpeed = speed;
                    speed = 0f;
                } else {
                    speed = storedSpeed;
                }
            }
            if (key == GLFW.GLFW_KEY_SPACE && action == GLFW.GLFW_PRESS) jumpRequested = true;
        });
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    private void loop() {
        ShaderProgram shader = new ShaderProgram("/shaders/humanoid.vert", "/shaders/humanoid.frag");
        Cube cube = new Cube();
        Humanoid humanoid = new Humanoid(cube, shader);
        long start = System.nanoTime();
        while (!GLFW.glfwWindowShouldClose(window)) {
            float time = (System.nanoTime() - start) / 1_000_000_000f;
            int[] width = new int[1]; int[] height = new int[1];
            GLFW.glfwGetFramebufferSize(window, width, height);
            GL11.glViewport(0, 0, width[0], height[0]);
            GL11.glClearColor(0.035f, 0.055f, 0.09f, 1f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            Mat4 projection = Mat4.perspective((float) Math.toRadians(48), (float) width[0] / height[0], 0.1f, 100f);
            Renderer.projectionView = projection.multiply(Mat4.lookAt(3.8f, 3.2f, 10.2f, 0f, 2.4f, 0f));
            humanoid.draw(time, speed, torsoDirection, jumpRequested);
            torsoDirection = 0f;
            jumpRequested = false;
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
        shader.delete();
        cube.delete();
    }

}
