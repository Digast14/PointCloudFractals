package org.example.scene;

import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.concurrent.Callable;

import static java.sql.Types.NULL;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;


public class Window {

    private final long windowPointer;

    private int resX;
    private int resY;

    public Window(String title, int resX, int resY) {
        this.resX = resX;
        this.resY = resY;

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();

        windowPointer = glfwCreateWindow(resX, resY, title, NULL, NULL);

        glfwSetKeyCallback(windowPointer, (window, key, scancode, action, mods) -> {
            keyCallBack(key, action);
        });

        glfwMakeContextCurrent(windowPointer);
    }


    public void keyCallBack(int key, int action) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(windowPointer, true); // We will detect this in the rendering loop
        }
    }


    public void cleanup() {
        glfwFreeCallbacks(windowPointer);
        glfwDestroyWindow(windowPointer);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }


    public long getWindowPointer() {
        return windowPointer;
    }


    public int getWidth(){
        return resX;
    }


    public int getHeight(){
        return resY;
    }
}
