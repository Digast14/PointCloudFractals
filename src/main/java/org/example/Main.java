package org.example;
import static org.lwjgl.glfw.GLFW.*;



public class Main  {
    public static void main(String[] args){
        runTest();
    }

    public static void runTest() {
        //hier Resolution bestimmen
        shaderUtils.init(1920,1080);
        //startLoop
        loop();
        // Clean up when closed
        shaderUtils.cleanUp();
        //ImGui.showDemoWindow();
    }


    public static void loop( ) {
        double frameRate = 1.0d / 30.0d;
        double previous = glfwGetTime();
        double steps = 0.0;

        glfwSetInputMode(shaderUtils.window.getWindowPointer(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);


        while (!glfwWindowShouldClose(shaderUtils.window.getWindowPointer())) {
            double current = glfwGetTime();
            double elapsed = current - previous;
            previous = current;
            steps += elapsed;
            while (steps >= frameRate) {
                steps -= frameRate;
            }

            getInputs();
            shaderUtils.render();
            sync(current);
        }
    }

    private static double xPos = 0;
    private static double yPos = 0;

    public static void getInputs(){
        float move = 0.01f * shaderUtils.range;
        Camera camera = shaderUtils.camera;
        if (glfwGetKey(shaderUtils.window.getWindowPointer(), GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS)  camera.moveDown(move);
        if (glfwGetKey(shaderUtils.window.getWindowPointer(), GLFW_KEY_SPACE) == GLFW_PRESS) camera.moveUp(move);
        if (glfwGetKey(shaderUtils.window.getWindowPointer(), GLFW_KEY_W) == GLFW_PRESS) camera.moveForward(move);
        if (glfwGetKey(shaderUtils.window.getWindowPointer(), GLFW_KEY_A) == GLFW_PRESS) camera.moveLeft(move);
        if (glfwGetKey(shaderUtils.window.getWindowPointer(), GLFW_KEY_S) == GLFW_PRESS) camera.moveBackwards(move);
        if (glfwGetKey(shaderUtils.window.getWindowPointer(), GLFW_KEY_D) == GLFW_PRESS) camera.moveRight(move);
        if (glfwGetKey(shaderUtils.window.getWindowPointer(), GLFW_KEY_R) == GLFW_PRESS) camera.setRotation((float)Math.PI, 0);


        double[] nextXPos = new double[1];
        double[] nextYPos = new double[1];


        glfwGetCursorPos(shaderUtils.window.getWindowPointer(), nextXPos, nextYPos);
        shaderUtils.camera.addRotation((float) -((yPos - nextYPos[0])*0.01),(float) ((xPos - nextXPos[0])*0.01));
        xPos = nextXPos[0];
        yPos = nextYPos[0];

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