package org.example;


import org.example.gui.GuiLayer;
import org.example.render.WorldRender;

import static org.lwjgl.glfw.GLFW.*;

public class Engine {

    private final Window window;
    private final WorldRender render;
    private final IAppLogic appLogic;
    private final GuiLayer guiLayer;


    public Engine(String WindowTitle, int width, int height, IAppLogic appLogic ) {
        window = new Window(WindowTitle, width, height, () -> {
            resize();
            return null;
        });

        guiLayer = new GuiLayer();
        render = new WorldRender(window);

        this.appLogic = appLogic;
        appLogic.init(window, render);
    }



    public void run(){
        int maxFPS = 60;
        double frameRate = 1.0d / (double) maxFPS;
        double previous = glfwGetTime();
        double steps = 0.0;

        init();

        while (!glfwWindowShouldClose(window.getWindowPointer())) {
            glfwPollEvents();

            //Manages FPS to be constant
            double current = glfwGetTime();
            double elapsed = current - previous;
            previous = current;
            steps += elapsed;
            while (steps >= frameRate) {
                steps -= frameRate;
            }

            appLogic.input(window, render);
            appLogic.update(window, render);
            render.update(guiLayer);
            render.render(guiLayer, window);

            sync(current);

            glfwSwapBuffers(window.getWindowPointer());
        }
        cleanup();
    }

    public void init(){
        render.getPointCloudRender().initShaders(guiLayer);
    }

    private void resize() {
        int width = window.getWidth();
        int height = window.getHeight();
        render.resize(width,height);
    }


    private void cleanup() {
        appLogic.cleanup();
        render.cleanup();
        window.cleanup();
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
}
