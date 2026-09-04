package com.humangl;

import com.humangl.math.Mat4;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;

public final class ShaderProgram {
    private final int id;
    private final int mvpLocation;
    private final int colorLocation;

    public ShaderProgram(String vertexPath, String fragmentPath) {
        int vertex = compile(GL20.GL_VERTEX_SHADER, vertexPath);
        int fragment = compile(GL20.GL_FRAGMENT_SHADER, fragmentPath);
        id = GL20.glCreateProgram();
        GL20.glAttachShader(id, vertex);
        GL20.glAttachShader(id, fragment);
        GL20.glLinkProgram(id);
        if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
            throw new IllegalStateException(GL20.glGetProgramInfoLog(id));
        }
        GL20.glDeleteShader(vertex);
        GL20.glDeleteShader(fragment);
        mvpLocation = GL20.glGetUniformLocation(id, "uMvp");
        colorLocation = GL20.glGetUniformLocation(id, "uColor");
    }

    private static int compile(int type, String path) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, readResource(path));
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            throw new IllegalStateException(GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private static String readResource(String path) {
        try (InputStream stream = ShaderProgram.class.getResourceAsStream(path)) {
            if (stream == null) throw new IOException("Missing shader: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public void use(Mat4 projectionView, Mat4 model, float red, float green, float blue) {
        GL20.glUseProgram(id);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Mat4 mvp = projectionView.multiply(model);
            GL20.glUniformMatrix4fv(mvpLocation, false, mvp.toBuffer());
        }
        GL20.glUniform3f(colorLocation, red, green, blue);
    }

    public void delete() {
        GL32.glDeleteProgram(id);
    }
}
