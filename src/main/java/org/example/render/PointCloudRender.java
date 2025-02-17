package org.example.render;

import org.example.gui.GuiLayer;
import org.example.scene.Camera;
import org.example.render.shader.ShaderProgramm;
import org.example.render.shader.UniformsMap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;


import static java.sql.Types.NULL;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL43.*;

public class PointCloudRender {

    private ShaderProgramm shaderProgram;
    private ShaderProgramm computeShaderProgram;
    private SceneSettings sceneSettings;

    private int ssbo;
    private int normalBuffer;
    private int globalIndexBuffer;
    private int pointCount;

    private float range;
    private int[] dimensions = new int[3];
    private int totalThreadDimension;
    private long totalThreads;
    private long availableMemory;

    private int width;
    private int height;
    private int texture;
    private int depthTexture;
    private int fbo;

    private UniformsMap uniformsMap;
    private PostProcessRender postProcessRender;

    private void setSettings(){
        dimensions[0] = sceneSettings.workGroupDimensionX;
        dimensions[1] = sceneSettings.workGroupDimensionY;
        dimensions[2] = sceneSettings.workGroupDimensionZ;

        totalThreadDimension = dimensions[0] * sceneSettings.threadDimension;
        totalThreads = (long) dimensions[0] * sceneSettings.threadDimension * dimensions[1] * sceneSettings.threadDimension * dimensions[2] * sceneSettings.threadDimension;
        availableMemory = (sceneSettings.vram * 1073741824L)/(4*3*2);
        System.out.print("max Points: ");
        System.out.printf(Locale.US, "%,d", availableMemory);
        System.out.println();


        range = sceneSettings.range;
        width = sceneSettings.width;
        height = sceneSettings.height;
    }

    public PointCloudRender(SceneSettings sceneSettings) {
        this.sceneSettings = sceneSettings;
        setSettings();
    }


    public void initShaders(GuiLayer guiLayer) {
        computeShaderProgram = new ShaderProgramm("/shaders/PointCloud/ComputeShader.comp", GL_COMPUTE_SHADER);
        shaderProgram = new ShaderProgramm("/shaders/PointCloud/vertexShader.vert", GL_VERTEX_SHADER, "/shaders/PointCloud/fragmentShader.frag", GL_FRAGMENT_SHADER);
        System.out.println("-------------------------");
        System.out.println("Compute Shader ID: " + computeShaderProgram.getProgramID());
        System.out.println("Vertex Shader ID: " + shaderProgram.getProgramID());
        System.out.println("-------------------------");
        createIndexBuffer();
        dispatchCompute(guiLayer);
        dispatchVertex();

        postProcessRender = new PostProcessRender(texture, depthTexture);

        guiLayer.setPointCount(pointCount);
    }

    public void recompile(GuiLayer guiLayer) {
        computeShaderProgram.editShader(GL_COMPUTE_SHADER, 0, guiLayer.function);

        setSettings();
        glDeleteBuffers(ssbo);
        glDeleteBuffers(normalBuffer);
        createIndexBuffer();
        dispatchCompute(guiLayer);
        guiLayer.setPointCount(pointCount);
    }

    private void createIndexBuffer() {
        globalIndexBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, globalIndexBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, 32, GL_DYNAMIC_COPY );

        ByteBuffer buffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
        buffer.putInt(0).flip();
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, buffer);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, globalIndexBuffer);
    }

    private void createPointBuffers() {
        ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) pointCount * 4 * 4, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo);

        normalBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, normalBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) pointCount * 4 * 4, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, normalBuffer);
    }

    private void setPointCountReset() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, globalIndexBuffer);
        pointCount = glMapBuffer(GL_SHADER_STORAGE_BUFFER, GL_READ_ONLY).asIntBuffer().get();

        glBufferData(GL_SHADER_STORAGE_BUFFER, 32, GL_DYNAMIC_DRAW);
        ByteBuffer buffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
        buffer.putInt(0).flip();
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, buffer);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, globalIndexBuffer);
    }


    private void dispatchCompute(GuiLayer guiLayer) {
        int id = computeShaderProgram.getProgramID();

        glUseProgram(id);
        parseComputeUniform(id, guiLayer);

        System.out.print("total threads: ");
        System.out.printf(Locale.US, "%,d", totalThreads);
        System.out.println();

        glDispatchCompute(dimensions[0], dimensions[1], dimensions[2]);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);

        //------------------------------Second Pass---------------------------------------------
        setPointCountReset();
        createPointBuffers();

        glUniform1i(glGetUniformLocation(id, "vertexArrayLength"), pointCount);
        glUniform1i(glGetUniformLocation(id, "u_pass"), 1);

        glDispatchCompute(dimensions[0], dimensions[1], dimensions[2]);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);

        computeShaderProgram.cleanup();
    }

    private void parseComputeUniform(int id,GuiLayer guiLayer){
        int gridDensity = (int) (totalThreadDimension / (range * 2));
        glUniform1i(glGetUniformLocation(id, "vertexArrayLength"), (int) availableMemory);
        glUniform1i(glGetUniformLocation(id, "gridDensity"), gridDensity);
        glUniform1f(glGetUniformLocation(id, "range"), range);
        glUniform3f(glGetUniformLocation(id, "ranges"), 1, 1 * ((float) dimensions[1] / dimensions[0]), 1 * ((float) dimensions[2] / dimensions[0]));
        glUniform1f(glGetUniformLocation(id, "timeSin"), guiLayer.time);
        glUniform1i(glGetUniformLocation(id, "u_qZeroC"), guiLayer.qZeroC);
        glUniform4f(glGetUniformLocation(id, "u_qZero"), guiLayer.qZero.x, guiLayer.qZero.y, guiLayer.qZero.z, guiLayer.qZero.w);
        glUniform1i(glGetUniformLocation(id, "u_maxIteration"), guiLayer.maxIteration);
        glUniform1f(glGetUniformLocation(id, "u_nudgeValue"), guiLayer.nudgeValue);
        glUniform1f(glGetUniformLocation(id, "u_breakoutFactor"), guiLayer.breakOutFactor);
        glUniform1i(glGetUniformLocation(id, "u_reverse"), guiLayer.reverse);
        glUniform1f(glGetUniformLocation(id, "u_normalPrecision"), guiLayer.normalPrecision);
        glUniform1f(glGetUniformLocation(id, "u_normalStepSize"), guiLayer.normalStepSize);
        glUniform1i(glGetUniformLocation(id, "u_power"), guiLayer.power);
        glUniform1i(glGetUniformLocation(id, "u_pass"), 0);
    }


    private void dispatchVertex() {
        shaderProgram.bind();
        uniformsMap = new UniformsMap(shaderProgram.getProgramID());
        uniformsMap.createUniform("projection");
        uniformsMap.createUniform("view");
        uniformsMap.createUniform("minPointSize");
        uniformsMap.createUniform("range");

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);

        createBuffers();

        shaderProgram.unbind();
    }

    private void createBuffers() {
        texture = createTexture();
        depthTexture = createDepthTexture();
        fbo = createFrameBuffer();
    }


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

    private int createDepthTexture() {
        int depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH24_STENCIL8, width, height, 0, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
        return depthTexture;
    }

    public void resize(int width, int height) {
        sceneSettings.width = width;
        sceneSettings.height = height;

        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH24_STENCIL8, width, height, 0, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, NULL);
        glBindTexture(GL_TEXTURE_2D, 0);
        this.width = width;
        this.height = height;
    }

    private int createFrameBuffer() {
        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Frame Buffer incomplete");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return fbo;
    }


    public void render(GuiLayer guiLayer, Camera camera) {
        shaderProgram.bind();
        parseUniform(guiLayer, camera);

        renderToFBO();

        shaderProgram.unbind();
        postProcessRender.render(sceneSettings, guiLayer, camera);
    }


    private void renderToFBO() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDrawArrays(GL_POINTS, 0, pointCount - 1);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }


    private void parseUniform(GuiLayer guiLayer, Camera camera) {
        uniformsMap.setUniform("projection", camera.getProjectionMatrix());
        uniformsMap.setUniform("view", camera.getViewMatrix());
        uniformsMap.setUniform("minPointSize", (float) guiLayer.quadSize);
        uniformsMap.setUniform("range", range);
    }

    public void cleanup() {
        glDeleteBuffers(ssbo);
        glDeleteBuffers(normalBuffer);
        glDeleteBuffers(globalIndexBuffer);
        glDeleteFramebuffers(fbo);
        shaderProgram.cleanup();
        postProcessRender.cleanUp();
    }

    public static class SceneSettings {
        public float range = 1f;
        public int threadDimension = 10;
        public int width = 1920;
        public int height = 1080;
        public int workGroupDimensionX = 16;
        public int workGroupDimensionY = 16;
        public int workGroupDimensionZ = 16;
        public int vram = 4;
    }
}
