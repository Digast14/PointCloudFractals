package org.example;



import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

//test message
public class shaderUtils {
    public static int shaderProgram;
    public static int computeShaderProgram;

    private static int ssbo;
    private static int normalBuffer;
    private static int globalIndexBuffer;
    private static int totalThreads;
    private static int threadDimension;
    public static int range;
    private static int workGroupDimension;

    public static Window window;
    public static Camera camera;
    public static Projection projection;

    private static UniformsMap uniformsMap;

    public static void init(int resX, int resY) {
        window = new Window(resX, resY);
        camera = new Camera();
        projection = new Projection(resX, resY);
        workGroupDimension = 24;
        threadDimension = workGroupDimension*10;
        totalThreads = threadDimension * threadDimension * threadDimension;
        range = 1;
        initShaders();
    }


    public static void initShaders() {
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

        int dispatchShader = createShaderFromSource(readFile("resources/shaders/ComputeShader.comp"), GL_COMPUTE_SHADER);
        computeShaderProgram = createProgram(dispatchShader);

        dispatchCompute();

        int vertexShader = createShaderFromSource(readFile("resources/shaders/vertexShader.vert"), GL_VERTEX_SHADER);
        int fragmentShader = createShaderFromSource(readFile("resources/shaders/fragmentShader.frag"), GL_FRAGMENT_SHADER);
        shaderProgram = createProgram(vertexShader, fragmentShader);


        shaderProgramInit();

        glBindBuffer(GL_ARRAY_BUFFER, ssbo);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

        glEnable(GL_DEPTH_TEST);
    }

    public static void dispatchCompute() {
        int gridDensity = threadDimension/(range*2);
        glUseProgram(computeShaderProgram);
        glUniform1i(glGetUniformLocation(computeShaderProgram, "vertexArrayLength"), totalThreads);
        glUniform1i(glGetUniformLocation(computeShaderProgram, "gridDensity"), gridDensity);
        glUniform1i(glGetUniformLocation(computeShaderProgram, "range"), range);
        glDispatchCompute(workGroupDimension, workGroupDimension, workGroupDimension);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
    }

    private static void shaderProgramInit() {
        glUseProgram(shaderProgram);
        uniformsMap = new UniformsMap(shaderProgram);
        uniformsMap.createUniform("projection");
        uniformsMap.createUniform("view");
        glEnable(GL_PROGRAM_POINT_SIZE);
        glPointSize(5);
    }


    public static void render() {
        //clear screen

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        //execute and draw Program
        glUseProgram(shaderProgram);

        uniformsMap.setUniform("projection", projection.getProjMatrix());
        uniformsMap.setUniform("view", camera.getViewMatrix());

        glDrawArrays(GL_POINTS, 0, totalThreads);

        glUseProgram(0);

        glfwSwapBuffers(window.getWindowPointer());
        glfwPollEvents();
    }

    public static void cleanUp() {
        glDeleteProgram(shaderProgram);
        glDeleteBuffers(ssbo);
        glfwFreeCallbacks(window.getWindowPointer());
        glfwDestroyWindow(window.getWindowPointer());
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static String readFile(String filePath) {
        String str;
        try {
            str = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException excp) {
            throw new RuntimeException("Error reading file [" + filePath + "]", excp);
        }
        return str;
    }

    private static int createShaderFromSource(String source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Failed to compile shader: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private static int createProgram(int shader) {
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, shader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program linking failed: " + glGetProgramInfoLog(shaderProgram));
        }
        return shaderProgram;
    }

    private static int createProgram(int fragmentShader, int vertexShader) {
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program linking failed: " + glGetProgramInfoLog(shaderProgram));
        }
        return shaderProgram;
    }
}
