package org.example;


import imgui.ImGui;
import imgui.ImGuiIO;
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

        render = new WorldRender(window);
        guiLayer = new GuiLayer();

        this.appLogic = appLogic;
        appLogic.init(window, render);

    }

    private void resize() {
        int width = window.getWidth();
        int height = window.getHeight();
        render.resize(width,height);
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

            ImGuiIO imGuiIO = ImGui.getIO();
            if(!imGuiIO.getWantCaptureMouse() || !imGuiIO.getWantCaptureKeyboard()){
                appLogic.input(window, render);
            }
            appLogic.update(window, render);
            render.render(guiLayer, window);

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
