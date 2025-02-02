package org.example.render.wip;

import org.example.render.shader.ShaderProgramm;
import org.example.render.shader.UniformsMap;

import static java.sql.Types.NULL;
import static org.lwjgl.opengl.GL43.*;

public class CustomPointCloudRender {

    //private final ShaderProgramm shaderProgram;
    private final ShaderProgramm sbboSorting;

    private final int ssbo;
    private final int normalBuffer;;
    private final int fbo;
    private final int totalThreads;


    private int depthBuffer;
    private int colorBuffer;
    private  int outputBuffer;


    private UniformsMap uniformsMap;

    private final int width;
    private final int height;
    private final int texture;

    public CustomPointCloudRender(int ssbo, int normalBuffer, int totalThreads, int width, int height) {
        this.totalThreads = totalThreads;
        //shaderProgram = new ShaderProgramm("resources/shaders/PointCloudRender.comp", GL_COMPUTE_SHADER);
        sbboSorting = new ShaderProgramm("resources/shaders/MortonOrdering.comp", GL_COMPUTE_SHADER);
        //uniformsMap = new UniformsMap(shaderProgram.getProgramID());

        this.ssbo = ssbo;
        this.normalBuffer = normalBuffer;
        this.width = width;
        this.height = height;
        texture = createTexture();
        fbo = createFrameBuffer(texture);
        //setUniformsMap();
    }


    public void ssboSort() {
        createBuffers();
        for (int i = 0; i < 4; i++) {
            glUseProgram(sbboSorting.getProgramID());
            glUniform1i(glGetUniformLocation(sbboSorting.getProgramID(), "passCount"), i);
            glDispatchCompute(1+(totalThreads/256), 1, 1);
            glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);

        }
        sbboSorting.cleanup();
    }

    private void createBuffers() {
        depthBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, depthBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER,  32, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, depthBuffer);

        colorBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, colorBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, 64 * 2, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, colorBuffer);

        outputBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, outputBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long )totalThreads * (32 * 4 + 16), GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, outputBuffer);
    }

/*
    public void render(GuiLayer guiLayer, Camera camera) {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        shaderProgram.bind();
        parseUniform(guiLayer, camera);
        glDispatchCompute(100, 100, 1);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
        shaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void setUniformsMap() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramID());
        uniformsMap.createUniform("projection");
        uniformsMap.createUniform("view");
    }

    private void parseUniform(GuiLayer guiLayer, Camera camera) {
        uniformsMap.setUniform("projection", camera.getProjectionMatrix());
        uniformsMap.setUniform("view", camera.getViewMatrix());
    }

 */


    private int createTexture() {
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
        return texture;
    }


    private int createFrameBuffer(int texture) {
        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Frame Buffer incomplete");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return fbo;
    }


    public void cleanup() {
        //shaderProgram.cleanup();
        sbboSorting.cleanup();
        glDeleteBuffers(fbo);
        glDeleteBuffers(depthBuffer);
        glDeleteBuffers(colorBuffer);
    }
}
