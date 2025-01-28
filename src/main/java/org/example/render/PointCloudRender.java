package org.example.render;

import org.example.gui.GuiLayer;
import org.example.scene.Camera;
import org.example.render.shader.ShaderProgramm;
import org.example.render.shader.UniformsMap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL43.*;

public class PointCloudRender {

    private ShaderProgramm shaderProgram;
    private ShaderProgramm computeShaderProgram;

    private int ssbo;
    private int normalBuffer;
    private int globalIndexBuffer;

    private final float range;
    private final int workGroupDimension;
    private final int totalThreadDimension;
    private final int totalThreads;

    private UniformsMap uniformsMap;

    public PointCloudRender(SceneSettings sceneSettings) {
        workGroupDimension = sceneSettings.workGroupDimension;
        totalThreadDimension = workGroupDimension * sceneSettings.threadDimension;
        totalThreads = totalThreadDimension * totalThreadDimension * totalThreadDimension;
        range = sceneSettings.range;
    }


    public void initShaders(GuiLayer guiLayer) {
        computeShaderProgram = new ShaderProgramm("resources/shaders/ComputeShader.comp", GL_COMPUTE_SHADER);
        if (!guiLayer.function.isEmpty()) computeShaderProgram.editShader(GL_COMPUTE_SHADER, 0, guiLayer.function);
        shaderProgram = new ShaderProgramm("resources/shaders/vertexShader.vert", GL_VERTEX_SHADER, "resources/shaders/fragmentShader.frag", GL_FRAGMENT_SHADER);

        System.out.println("Compute Shader ID: " + computeShaderProgram.getProgramID());
        System.out.println("Vertex Shader ID: " + shaderProgram.getProgramID());

        createBuffers();
        dispatchCompute(guiLayer);
        dispatchVertex();
    }

    private void createBuffers() {
        ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) totalThreads * 32 * 4, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo);

        normalBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, normalBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) totalThreads * 32 * 3, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, normalBuffer);

        globalIndexBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, globalIndexBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, 32, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, globalIndexBuffer);

        ByteBuffer buffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
        buffer.putInt(0).flip();
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, buffer);

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, globalIndexBuffer);
    }

    private void dispatchCompute(GuiLayer guiLayer) {
        int gridDensity = (int) (totalThreadDimension / (range * 2));
        int id = computeShaderProgram.getProgramID();
        glUseProgram(id);
        glUniform1i(glGetUniformLocation(id, "vertexArrayLength"), totalThreads);
        glUniform1i(glGetUniformLocation(id, "gridDensity"), gridDensity);
        glUniform1f(glGetUniformLocation(id, "range"), range);

        glUniform1f(glGetUniformLocation(id, "timeSin"), guiLayer.time);
        glUniform1i(glGetUniformLocation(id, "u_qZeroC"), guiLayer.qZeroC);
        glUniform4f(glGetUniformLocation(id, "u_qZero"), guiLayer.qZero.x, guiLayer.qZero.y, guiLayer.qZero.z, guiLayer.qZero.w);
        glUniform1i(glGetUniformLocation(id, "u_maxIteration"), guiLayer.maxIteration);
        glUniform1f(glGetUniformLocation(id, "u_nudgeValue"), guiLayer.nudgeValue);
        glUniform1f(glGetUniformLocation(id, "u_breakoutFactor"), guiLayer.breakOutFactor);
        glUniform1i(glGetUniformLocation(id, "u_reverse"), guiLayer.reverse);
        glUniform1f(glGetUniformLocation(id, "u_normalPrecision"), guiLayer.normalPrecision);
        glUniform1i(glGetUniformLocation(id, "u_power"), guiLayer.power);

        System.out.println(workGroupDimension);
        System.out.println(totalThreadDimension);
        System.out.println(totalThreads);

        glDispatchCompute(workGroupDimension, workGroupDimension, workGroupDimension);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);

        computeShaderProgram.cleanup();
    }

    private void dispatchVertex() {
        shaderProgram.bind();
        uniformsMap = new UniformsMap(shaderProgram.getProgramID());
        uniformsMap.createUniform("projection");
        uniformsMap.createUniform("view");

        glBindBuffer(GL_ARRAY_BUFFER, ssbo);

        shaderProgram.unbind();
    }


    public void render(GuiLayer guiLayer, Camera camera) {
        shaderProgram.bind();
        parseUniform(camera);
        glPointSize(guiLayer.quadSize);
        glDrawArrays(GL_POINTS, 0, totalThreads);
        shaderProgram.unbind();
    }

    private void parseUniform(Camera camera) {
        uniformsMap.setUniform("projection", camera.getProjectionMatrix());
        uniformsMap.setUniform("view", camera.getViewMatrix());

    }

    public void cleanup() {
        glDeleteBuffers(ssbo);
        glDeleteBuffers(normalBuffer);
        glDeleteBuffers(globalIndexBuffer);
        shaderProgram.cleanup();
    }

    public static class SceneSettings {
        public float range = 1f;
        public int workGroupDimension = 16;
        public int threadDimension = 10;
    }
}
