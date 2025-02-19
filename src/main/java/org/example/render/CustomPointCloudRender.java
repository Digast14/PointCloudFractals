package org.example.render;


import org.example.gui.GuiLayer;
import org.example.render.shader.ShaderProgramm;
import org.example.render.shader.UniformsMap;
import org.example.scene.Camera;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL43.*;

public class CustomPointCloudRender {

    private final ShaderProgramm shaderProgramDepth;
    private final ShaderProgramm shaderProgramColor;
    private final ShaderProgramm shaderProgramResolve;

    private final UniformsMap uniformsMapDepth;
    private final UniformsMap uniformsMapColor;


    private int pointCount;
    private final int fbo;
    private final int texture;


    private int width;
    private int height;

    private int depthBuffer;
    private int colorBuffer;

    public CustomPointCloudRender(int pointCount, int texture, int fbo, PointCloudRender.SceneSettings sceneSettings) {

        shaderProgramDepth = new ShaderProgramm("/shaders/PointCloud/CustomRender/CustomRenderDepth.comp", GL_COMPUTE_SHADER);
        shaderProgramColor = new ShaderProgramm("/shaders/PointCloud/CustomRender/CustomRenderColor.comp", GL_COMPUTE_SHADER);
        shaderProgramResolve = new ShaderProgramm("/shaders/PointCloud/CustomRender/CustomRenderResolve.comp", GL_COMPUTE_SHADER);


        this.pointCount = pointCount;
        this.texture = texture;
        this.fbo = fbo;

        width = sceneSettings.width;
        height = sceneSettings.height;

        depthBuffer = createDepthBuffer();
        colorBuffer = createColorBuffer();

        uniformsMapDepth = createUniformDepth();
        uniformsMapColor = createUniformColor();
    }

    public void updatePointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        glDeleteBuffers(depthBuffer);
        glDeleteBuffers(colorBuffer);
        depthBuffer = createDepthBuffer();
        colorBuffer = createColorBuffer();
    }

    private int createDepthBuffer() {
        int ssDepthBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssDepthBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) width * height * 8, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, ssDepthBuffer);

        // Allocate and set Integer.MAX_VALUE
        IntBuffer clearValue = MemoryUtil.memAllocInt(1);
        clearValue.put(0, Integer.MAX_VALUE).rewind(); // Rewind to reset position

        glClearBufferData(GL_SHADER_STORAGE_BUFFER, GL_R32I, GL_RED_INTEGER, GL_INT, clearValue);
        MemoryUtil.memFree(clearValue); // Free memory after use
        return ssDepthBuffer;
    }

    private int createColorBuffer() {
        int colorBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, colorBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) width * height * 16, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, colorBuffer);

        IntBuffer clearValue = MemoryUtil.memAllocInt(1);
        clearValue.put(0, 0).rewind();

        glClearBufferData(GL_SHADER_STORAGE_BUFFER, GL_RG32I, GL_RG_INTEGER, GL_INT, clearValue);
        MemoryUtil.memFree(clearValue);
        return colorBuffer;
    }

    private UniformsMap createUniformDepth() {
        shaderProgramDepth.bind();
        UniformsMap uniformsMapDepth = new UniformsMap(shaderProgramDepth.getProgramID());
        uniformsMapDepth.createUniform("projection");
        uniformsMapDepth.createUniform("view");
        uniformsMapDepth.createUniform("resolution");
        uniformsMapDepth.createUniform("pointCount");
        shaderProgramDepth.unbind();
        return uniformsMapDepth;
    }

    private void parseUniformDepth(Camera camera) {
        uniformsMapDepth.setUniform("projection", camera.getProjectionMatrix());
        uniformsMapDepth.setUniform("view", camera.getViewMatrix());
        uniformsMapDepth.setUniform("resolution", new Vector2i(width, height));
        uniformsMapDepth.setUniform("pointCount", pointCount);
    }

    private UniformsMap createUniformColor() {
        shaderProgramColor.bind();
        UniformsMap uniformsMapColor = new UniformsMap(shaderProgramColor.getProgramID());
        uniformsMapColor.createUniform("projection");
        uniformsMapColor.createUniform("view");
        uniformsMapColor.createUniform("resolution");
        uniformsMapColor.createUniform("pointCount");
        shaderProgramColor.unbind();
        return uniformsMapColor;
    }

    private void parseUniformColor(Camera camera) {
        uniformsMapColor.setUniform("projection", camera.getProjectionMatrix());
        uniformsMapColor.setUniform("view", camera.getViewMatrix());
        uniformsMapColor.setUniform("resolution", new Vector2i(width, height));
        uniformsMapColor.setUniform("pointCount", pointCount);
    }


    public void render(GuiLayer guiLayer,Camera camera) {

        shaderProgramDepth.bind();
        parseUniformDepth(camera);
        glDispatchCompute(1 + pointCount / 1024, 1, 1);
        shaderProgramDepth.unbind();
        glMemoryBarrier(GL_ALL_BARRIER_BITS);


        shaderProgramColor.bind();
        parseUniformColor(camera);
        glDispatchCompute(1 + pointCount / 1024, 1, 1);
        shaderProgramColor.unbind();
        glMemoryBarrier(GL_ALL_BARRIER_BITS);


        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shaderProgramResolve.bind();
        Vector3f col = guiLayer.color3;
        glUniform3f(glGetUniformLocation(shaderProgramResolve.getProgramID(), "backgroundColor"), col.x, col.y, col.z );

        glUniform1i(glGetUniformLocation(shaderProgramResolve.getProgramID(), "uOutput"), 0);
        glBindImageTexture(0, texture, 0, false, 0, GL_READ_WRITE, GL_RGBA8UI);
        glDispatchCompute(1 + width / 16, 1 + height / 16, 1);
        shaderProgramColor.unbind();
        glMemoryBarrier(GL_ALL_BARRIER_BITS);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void cleanUp() {
        shaderProgramDepth.cleanup();
        shaderProgramColor.cleanup();
        shaderProgramResolve.cleanup();
        glDeleteBuffers(depthBuffer);
        glDeleteBuffers(colorBuffer);
    }
}
