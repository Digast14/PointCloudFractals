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
    private final SceneRender sceneRender;
    private final GuiRender guiRender;

    public WorldRender(Window window) {
        GL.createCapabilities(); //Initialize OpenGL bindings
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        camera = new Camera(window.getWidth(), window.getHeight());
        sceneRender = new SceneRender( new SceneRender.SceneSettings());
        guiRender = new GuiRender(window);
    }

    public void resize(int resX, int resY){
        camera.resize(resX, resY);
    }


    public void render(GuiLayer guiLayer, Window window){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        sceneRender.render(camera);
        guiRender.render(guiLayer);
    }


    public Camera getCamera(){
        return camera;
    }


    public void cleanup(){
        sceneRender.cleanup();
    }
}
