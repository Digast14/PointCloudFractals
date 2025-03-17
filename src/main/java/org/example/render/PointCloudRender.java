package org.example.render;

import org.example.gui.GuiLayer;
import org.example.scene.Camera;
import org.example.render.shader.ShaderProgramm;
import org.example.render.shader.UniformsMap;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Locale;

import static java.lang.Math.min;
import static java.sql.Types.NULL;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;


public class PointCloudRender {

    private ShaderProgramm computeShaderProgram;
    private ShaderProgramm shaderProgram;

    private final SceneSettings sceneSettings;

    private int ssbo;
    private int normalBuffer;
    private int globalIndexBuffer;
    private int pointCount;

    private float range;
    private final int[] dimensions = new int[3];
    private long totalThreads;
    private long maxPoints;

    private int gridDensity;

    private int width;
    private int height;
    private int texture;
    private int depthTexture;
    private int fbo;

    private final long GB = 1073741824L;

    private UniformsMap uniformsMap;
    private PostProcessRender postProcessRender;
    private CustomPointCloudRender customPointCloudRender;

    public PointCloudRender(SceneSettings sceneSettings) {
        this.sceneSettings = sceneSettings;
        setSettings();
    }

    private void setSettings() {
        dimensions[0] = sceneSettings.workGroupDimensionX;
        dimensions[1] = sceneSettings.workGroupDimensionY;
        dimensions[2] = sceneSettings.workGroupDimensionZ;

        range = sceneSettings.range;
        width = sceneSettings.width;
        height = sceneSettings.height;

        int totalThreadDimension = dimensions[0] * sceneSettings.threadDimension;
        totalThreads = (long) dimensions[0] * sceneSettings.threadDimension * dimensions[1] * sceneSettings.threadDimension * dimensions[2] * sceneSettings.threadDimension;
        maxPoints = (sceneSettings.vram * GB) / (4 * 3 * 2);
        gridDensity = (int) (totalThreadDimension / (range * 2));

        System.out.print("max Points: ");
        System.out.printf(Locale.US, "%,d", maxPoints);
        System.out.println();

        stbi_flip_vertically_on_write(true);
    }



    public void initShaders(GuiLayer guiLayer) {
        computeShaderProgram = new ShaderProgramm("/shaders/PointCloud/ComputeShader.comp", GL_COMPUTE_SHADER);
        shaderProgram = new ShaderProgramm("/shaders/PointCloud/vertexShader.vert", GL_VERTEX_SHADER, "/shaders/PointCloud/fragmentShader.frag", GL_FRAGMENT_SHADER);

        System.out.println("-------------------------");
        System.out.println("Compute Shader ID: " + computeShaderProgram.getProgramID());
        System.out.println("Vertex Shader ID: " + shaderProgram.getProgramID());
        System.out.println("-------------------------");


        dispatchCompute(guiLayer);
        dispatchVertex();

        guiLayer.setPointCount(pointCount);

        postProcessRender = new PostProcessRender(texture, depthTexture);
        customPointCloudRender = new CustomPointCloudRender(pointCount, texture, fbo, sceneSettings);
    }

    public void recompile(GuiLayer guiLayer) {
        computeShaderProgram.editShader(GL_COMPUTE_SHADER, 0, guiLayer.function);

        setSettings();
        glDeleteBuffers(ssbo);
        glDeleteBuffers(normalBuffer);
        globalIndexBuffer = createIndexBuffer();
        dispatchCompute(guiLayer);
        guiLayer.setPointCount(pointCount);

        customPointCloudRender.updatePointCount(pointCount);
    }

    private int createIndexBuffer() {
        int globalIndexBuffer = glGenBuffers();

        ByteBuffer buffer = MemoryUtil.memAlloc(4);
        buffer.putInt(0).rewind();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, globalIndexBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, GL_DYNAMIC_COPY);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, globalIndexBuffer);

        MemoryUtil.memAddress(buffer);
        return globalIndexBuffer;
    }

    //up to 8GB storage usage before second Buffer is created. max Size 16 GB
    private void createPointBuffers() {
        long maxStorage = GB*4-1;

        ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, min((long) pointCount * 12, maxStorage), GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo);

        if((long)pointCount * 12 > maxStorage){
            int ssbo2 = glGenBuffers();
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo2);
            glBufferData(GL_SHADER_STORAGE_BUFFER, min((long) pointCount * 12 - maxStorage, maxStorage), GL_DYNAMIC_DRAW);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, ssbo2);
        }


        normalBuffer = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, normalBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, min((long) pointCount * 12 , maxStorage), GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, normalBuffer);

        if((long)pointCount * 12 > maxStorage){
            int normalBuffer2 = glGenBuffers();
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, normalBuffer2);
            glBufferData(GL_SHADER_STORAGE_BUFFER, min((long) pointCount * 12 - maxStorage, maxStorage), GL_DYNAMIC_DRAW);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 6, normalBuffer2);
        }
    }

    private void setPointCountReset() {
        ByteBuffer buffer = MemoryUtil.memAlloc(4);
        buffer.putInt(0).rewind();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, globalIndexBuffer);
        pointCount = glMapBuffer(GL_SHADER_STORAGE_BUFFER, GL_READ_ONLY).asIntBuffer().get();

        glBufferData(GL_SHADER_STORAGE_BUFFER, buffer, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, globalIndexBuffer);

        MemoryUtil.memAddress(buffer);
    }


    private void dispatchCompute(GuiLayer guiLayer) {
        int id = computeShaderProgram.getProgramID();

        globalIndexBuffer = createIndexBuffer();

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

    private void parseComputeUniform(int id, GuiLayer guiLayer) {
        glUniform1i(glGetUniformLocation(id, "vertexArrayLength"), (int) maxPoints);
        glUniform1i(glGetUniformLocation(id, "gridDensity"), gridDensity);
        glUniform1f(glGetUniformLocation(id, "range"), range);
        glUniform3i(glGetUniformLocation(id, "ranges"), dimensions[0], dimensions[1], dimensions[2]);
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
        uniformsMap.createUniform("gridDensity");
        uniformsMap.createUniform("jitterStrength");

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);

        createBuffers();

        shaderProgram.unbind();
    }

    private void parseVertexUniform(GuiLayer guiLayer, Camera camera) {
        uniformsMap.setUniform("projection", camera.getProjectionMatrix());
        uniformsMap.setUniform("view", camera.getViewMatrix());
        uniformsMap.setUniform("minPointSize", (float) guiLayer.quadSize);
        uniformsMap.setUniform("range", range);
        uniformsMap.setUniform("gridDensity", gridDensity);
        uniformsMap.setUniform("jitterStrength", guiLayer.jitterStrength);
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

        customPointCloudRender.resize(width,height);
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
        parseVertexUniform(guiLayer, camera);

        if (guiLayer.drawMode) renderToFBO();
        else customPointCloudRender.render(guiLayer, camera);

        shaderProgram.unbind();
        postProcessRender.render(sceneSettings, guiLayer, camera);

        if(guiLayer.saveImage){
            guiLayer.saveImage = false;
            ShaderProgramm.renderToPNG(guiLayer.fileName, width, height);
        }
    }


    private void renderToFBO() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDrawArrays(GL_POINTS, 0, pointCount);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }


    public void cleanup() {
        glDeleteBuffers(ssbo);
        glDeleteBuffers(normalBuffer);
        glDeleteBuffers(globalIndexBuffer);
        glDeleteFramebuffers(fbo);
        shaderProgram.cleanup();
        postProcessRender.cleanUp();
        customPointCloudRender.cleanUp();
    }

    public static class SceneSettings {
        public float range = 1f;
        public int threadDimension = 10;
        public int width = 1920;
        public int height = 1080;
        public int workGroupDimensionX = 16;
        public int workGroupDimensionY = 16;
        public int workGroupDimensionZ = 16;
        public int vram = 1;
    }
}
