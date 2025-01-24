package org.example;

import org.example.scene.Scene;
import org.example.scene.Window;

import static org.lwjgl.glfw.GLFW.*;

public class Engine {

    private final Window window;
    private final Scene scene;
    private final IAppLogic appLogic;


    public Engine(String WindowTitle, int width, int height, IAppLogic appLogic) {
        window = new Window(WindowTitle, width, height);
        scene = new Scene(width, height, new Scene.SceneSettings());
        this.appLogic = appLogic;
        appLogic.init(window, scene);

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
            appLogic.update(window, scene);
            appLogic.input(window, scene);
            scene.render();
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
        scene.cleanup();
        window.cleanup();
    }
}
