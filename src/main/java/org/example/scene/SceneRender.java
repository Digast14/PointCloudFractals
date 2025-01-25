package org.example.scene;

import org.example.gui.GuiLayer;

import java.sql.SQLOutput;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL42.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;

public class SceneRender {

    private ShaderProgramm shaderProgram;
    private ShaderProgramm computeShaderProgram;

    private int ssbo;
    private int normalBuffer;
    private int globalIndexBuffer;

    private final float range;
    private final int workGroupDimension;
    private final int totalThreadDimension;
    private final int totalThreads;
    private final int quadSize;

    private UniformsMap uniformsMap;

    public SceneRender(SceneSettings sceneSettings){
        workGroupDimension = sceneSettings.workGroupDimension;
        totalThreadDimension = workGroupDimension * sceneSettings.threadDimension;
        totalThreads = totalThreadDimension * totalThreadDimension * totalThreadDimension;
        range = sceneSettings.range;
        quadSize = sceneSettings.quadSize;

        initShaders();
    }


    private void initShaders(){
        computeShaderProgram = new  ShaderProgramm("resources/shaders/ComputeShader.comp", GL_COMPUTE_SHADER);
        shaderProgram = new ShaderProgramm("resources/shaders/vertexShader.vert", GL_VERTEX_SHADER, "resources/shaders/fragmentShader.frag", GL_FRAGMENT_SHADER);

        System.out.println("Compute Shader ID: " + computeShaderProgram.getProgramID());
        System.out.println("Vertex Shader ID: " + shaderProgram.getProgramID());

        createBuffers();
        dispatchCompute();
        dispatchVertex();
    }

    private void createBuffers(){
        ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) totalThreads * 32, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo);

        normalBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, normalBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) totalThreads * 32, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, normalBuffer);

        globalIndexBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, globalIndexBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER,  32, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, globalIndexBuffer);
    }

    private void dispatchCompute() {
        int gridDensity = (int) (totalThreadDimension/(range*2));
        int id = computeShaderProgram.getProgramID();
        glUseProgram(id);
        glUniform1i(glGetUniformLocation(id, "vertexArrayLength"), totalThreads);
        glUniform1i(glGetUniformLocation(id, "gridDensity"), gridDensity);
        glUniform1f(glGetUniformLocation(id, "range"), range);
        glDispatchCompute(workGroupDimension, workGroupDimension, workGroupDimension);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
        computeShaderProgram.cleanup();
    }

    private void dispatchVertex() {
        int id = shaderProgram.getProgramID();
        glUseProgram(id);
        uniformsMap = new UniformsMap(id);
        uniformsMap.createUniform("projection");
        uniformsMap.createUniform("view");

        glBindBuffer(GL_ARRAY_BUFFER, ssbo);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

        glPointSize(quadSize);
    }

    private void parseUniform(Camera camera){
        uniformsMap.setUniform("projection", camera.getProjectionMatrix());
        uniformsMap.setUniform("view", camera.getViewMatrix());
    }

    public void render(Camera camera, GuiLayer guiLayer){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shaderProgram.bind();
        parseUniform(camera);
        glDrawArrays(GL_POINTS, 0, totalThreads);
        shaderProgram.unbind();

        recompile(guiLayer);
    }

    private void recompile(GuiLayer guiLayer){
        if(guiLayer.newFunction){
            shaderProgram.cleanup();
            glDeleteBuffers(ssbo);
            glDeleteBuffers(normalBuffer);
            glDeleteBuffers(globalIndexBuffer);

            computeShaderProgram = new  ShaderProgramm("resources/shaders/ComputeShader.comp", GL_COMPUTE_SHADER);
            computeShaderProgram.editShader(GL_COMPUTE_SHADER, 0, guiLayer.function);
            shaderProgram = new ShaderProgramm("resources/shaders/vertexShader.vert", GL_VERTEX_SHADER, "resources/shaders/fragmentShader.frag", GL_FRAGMENT_SHADER);
            createBuffers();
            dispatchCompute();
            dispatchVertex();
        }
    }

    public void cleanup() {
        shaderProgram.cleanup();
        glDeleteBuffers(ssbo);
        glDeleteBuffers(normalBuffer);
        glDeleteBuffers(globalIndexBuffer);
    }

    public static class SceneSettings{
        public float range = 1f;
        public int workGroupDimension = 24;
        public int threadDimension = 10;
        public int quadSize = 5;
    }
}
