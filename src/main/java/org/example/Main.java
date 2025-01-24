package org.example;
import org.example.scene.Camera;
import org.example.scene.Window;

import static org.lwjgl.glfw.GLFW.*;


//test comment
public class Main implements IAppLogic {
    public static void main(String[] args){
        Main main = new Main();
        Engine gameEngine = new Engine("Point Cloud Shader", 1920, 1080, main);
        gameEngine.run();
    }

    @Override
    public void cleanup() {
        //nothing to clean up yet
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

        Camera camera = render.getCamera();
        float move = 0.01f * camera.getSpeed();
        if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS)  camera.moveDown(move);
        if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_SPACE) == GLFW_PRESS) camera.moveUp(move);
        if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_W) == GLFW_PRESS) camera.moveForward(move);
        if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_A) == GLFW_PRESS) camera.moveLeft(move);
        if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_S) == GLFW_PRESS) camera.moveBackwards(move);
        if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_D) == GLFW_PRESS) camera.moveRight(move);
        if (glfwGetKey(window.getWindowPointer(), GLFW_KEY_R) == GLFW_PRESS) camera.setRotation((float)Math.PI, 0);

        double[] nextXPos = new double[1];
        double[] nextYPos = new double[1];

        glfwGetCursorPos(window.getWindowPointer(), nextXPos, nextYPos);
        camera.addRotation((float) -((yPos - nextYPos[0])*0.01),(float) ((xPos - nextXPos[0])*0.01));
        xPos = nextXPos[0];
        yPos = nextYPos[0];
    }

    @Override
    public void update(Window window, WorldRender render) {
        //nothing to update yet
    }
}