package com.humangl;

import com.humangl.math.Mat4;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public final class Main {
    private long window;
    private float speed = 2.2f;
    private boolean jump;
    private boolean rotateTorso;
    private boolean spaceWasDown;

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
            if (key == GLFW.GLFW_KEY_R && action == GLFW.GLFW_PRESS) rotateTorso = !rotateTorso;
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
            updateInput();
            int[] width = new int[1]; int[] height = new int[1];
            GLFW.glfwGetFramebufferSize(window, width, height);
            GL11.glViewport(0, 0, width[0], height[0]);
            GL11.glClearColor(0.035f, 0.055f, 0.09f, 1f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            Mat4 projection = Mat4.perspective((float) Math.toRadians(48), (float) width[0] / height[0], 0.1f, 100f);
            Renderer.projectionView = projection.multiply(Mat4.lookAt(0f, 3.1f, 10.5f, 0f, 2.4f, 0f));
            humanoid.draw(time, speed, rotateTorso, jump);
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
        shader.delete();
        cube.delete();
    }

    private void updateInput() {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) speed = Math.min(5f, speed + 0.02f);
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) speed = Math.max(0f, speed - 0.02f);
        boolean spaceDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        if (spaceDown && !spaceWasDown) jump = !jump;
        spaceWasDown = spaceDown;
    }
}
