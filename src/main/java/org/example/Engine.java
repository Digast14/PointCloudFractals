package org.example;

import org.example.scene.Window;

import static org.lwjgl.glfw.GLFW.*;

public class Engine {

    private final Window window;
    private final WorldRender render;
    private final IAppLogic appLogic;
    private IGuiInstance guiInstance;


    public Engine(String WindowTitle, int width, int height, IAppLogic appLogic, IGuiInstance guiInstance) {
        window = new Window(WindowTitle, width, height);
        render = new WorldRender(window);
        this.appLogic = appLogic;
        this.guiInstance = guiInstance;
        appLogic.init(window, render);
    }

    public void run(){
        double frameRate = 1.0d / 30.0d;
        double previous = glfwGetTime();
        double steps = 0.0;

        while (!glfwWindowShouldClose(window.getWindowPointer())) {
            glfwPollEvents();

            double current = glfwGetTime();
            double elapsed = current - previous;
            previous = current;
            steps += elapsed;
            while (steps >= frameRate) {
                steps -= frameRate;
            }

            boolean inputConsumed = (guiInstance != null) ? guiInstance.handleGuiInput(render, window) : false;
            appLogic.input(window, render, inputConsumed);
            appLogic.update(window, render);
            render.render(guiInstance);

            sync(current);

            glfwSwapBuffers(window.getWindowPointer());
        }
        cleanup();
    }

    private static void sync(double loopStartTime) {
        float loopSlot = 1f / 50;
        double endTime = loopStartTime + loopSlot;
        while (glfwGetTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException _) {
            }
        }
    }

    private void cleanup() {
        appLogic.cleanup();
        render.cleanup();
        window.cleanup();
    }
}
