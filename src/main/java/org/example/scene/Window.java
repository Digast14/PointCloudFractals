package org.example.scene;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.tinylog.Logger;

import java.util.concurrent.Callable;

import static java.sql.Types.NULL;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;


public class Window {

    private final long windowPointer;

    private int resX;
    private int resY;
    private Callable<Void> resizeFunc;

    public Window(String title, int resX, int resY, Callable<Void> resizeFunc) {
        this.resizeFunc = resizeFunc;
        this.resX = resX;
        this.resY = resY;

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();

        windowPointer = glfwCreateWindow(resX, resY, title, NULL, NULL);

        glfwSetKeyCallback(windowPointer, (window, key, scancode, action, mods) -> {
            keyCallBack(key, action);
        });
        glfwSetFramebufferSizeCallback(windowPointer, (window, w, h) -> {
            resized(w, h);
        });


        glfwMakeContextCurrent(windowPointer);
    }


    public void keyCallBack(int key, int action) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(windowPointer, true); // We will detect this in the rendering loop
        }
    }

    protected void resized(int width, int height) {
        resX = width;
        resY = height;
        System.out.println("resize!");
        try {
            resizeFunc.call();
        } catch (Exception excp) {
            Logger.error("Error calling resize callback", excp);
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


    public int getWidth() {
        return resX;
    }


    public int getHeight() {
        return resY;
    }
}
