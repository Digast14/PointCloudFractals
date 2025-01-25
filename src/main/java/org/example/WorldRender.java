package org.example;


import org.example.gui.GuiLayer;
import org.example.gui.GuiRender;
import org.example.scene.Camera;
import org.example.scene.SceneRender;
import org.example.scene.Window;
import org.lwjgl.opengl.GL;


import static org.lwjgl.opengl.GL11.*;

public class WorldRender {

    private final Camera camera;
    private final GuiRender guiRender;
    private final SceneRender sceneRender;
    private final FractalRender fractalRender;


    public WorldRender(Window window) {
        GL.createCapabilities(); //Initialize OpenGL bindings
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        camera = new Camera(window.getWidth(), window.getHeight());
        guiRender = new GuiRender(window);
        sceneRender = new SceneRender( new SceneRender.SceneSettings());
        fractalRender = new FractalRender();
    }

    public void resize(int resX, int resY) {
        camera.resize(resX, resY);
    }


    public void render(GuiLayer guiLayer, Window window) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        if(guiLayer.local3dFractalChangeMode){
            camera.setPosition(0.0F,0.0F,0.0F);
            camera.setSpeed(1);
        }

        if (guiLayer.local3dFractal) {
            sceneRender.render(camera, guiLayer);
        }else {
            fractalRender.render(camera, guiLayer, window);
        }
        guiRender.render(guiLayer);
    }


    public Camera getCamera() {
        return camera;
    }


    public void cleanup() {
        sceneRender.cleanup();
        fractalRender.cleanup();
    }
}
