package org.example;

import org.example.gui.GuiRender;
import org.example.scene.Camera;
import org.example.scene.SceneRender;
import org.example.scene.Window;

import static org.lwjgl.opengl.GL11.*;

public class WorldRender {

    private final Camera camera;
    private SceneRender sceneRender;
    private GuiRender guiRender;

    public WorldRender(Window window) {
        camera = new Camera(window.getWidth(), window.getHeight());
        sceneRender = new SceneRender( new SceneRender.SceneSettings());
        guiRender = new GuiRender(window);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    public void render(IGuiInstance guiInstance){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        sceneRender.render(camera);
        guiRender.render(guiInstance);
    }

    public Camera getCamera(){
        return camera;
    }

    public void cleanup(){
        sceneRender.cleanup();
        guiRender.cleanup();
    }
}
