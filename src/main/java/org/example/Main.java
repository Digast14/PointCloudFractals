package org.example;

import imgui.ImGui;
import imgui.ImGuiIO;
import org.example.render.WorldRender;
import org.example.scene.Camera;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {


    public static void main(String[] args) {
        Main main = new Main();
        Engine gameEngine = new Engine("Point Cloud Shader", 1920, 1080, main);
        gameEngine.run();
    }


    private static double xPos;
    private static double yPos;

    @Override
    public void init(Window window, WorldRender render) {
        xPos = 0.0;
        yPos = 0.0;
    }

    @Override
    public void input(Window window, WorldRender render) {

        ImGuiIO imGuiIO = ImGui.getIO();
        if (!imGuiIO.getWantCaptureKeyboard()) {
            Camera camera = render.getCamera();
            float move = 0.02f;
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) camera.moveDown(move);
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_SPACE) == GLFW_PRESS) camera.moveUp(move);
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_W) == GLFW_PRESS) camera.moveForward(move);
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_A) == GLFW_PRESS) camera.moveLeft(move);
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_S) == GLFW_PRESS) camera.moveBackwards(move);
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_D) == GLFW_PRESS) camera.moveRight(move);
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_Q) == GLFW_PRESS) camera.speedUp(1.05F);
            ;
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_E) == GLFW_PRESS) camera.speedDown(1.05F);
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_R) == GLFW_PRESS) camera.setRotation((float) Math.PI, 0);
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_1) == GLFW_PRESS) camera.setBlackAndWhite();
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_2) == GLFW_PRESS) camera.setRGB();
            if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_3) == GLFW_PRESS) camera.setColor();

            double[] nextXPos = new double[1];
            double[] nextYPos = new double[1];

            glfwGetCursorPos(window.getWindowPointer(), nextXPos, nextYPos);
            if (glfwGetMouseButton(window.getWindowPointer(), GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
                camera.addRotation((float) ((yPos - nextYPos[0]) * 0.001), (float) -((xPos - nextXPos[0]) * 0.001));
            }
            xPos = nextXPos[0];
            yPos = nextYPos[0];
        }
    }

    @Override
    public void update(Window window, WorldRender render) {
        //nothing to update yet
    }

    @Override
    public void cleanup() {
        //nothing to clean up yet
    }
}