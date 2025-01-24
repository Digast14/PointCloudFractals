package org.example;

import org.example.scene.Camera;
import org.example.scene.SceneRender;

import static org.lwjgl.opengl.GL11.*;

public class WorldRender {

    private final Camera camera;
    private SceneRender sceneRender;

    public WorldRender(int width, int height) {
        camera = new Camera(width, height);
        sceneRender = new SceneRender( new SceneRender.SceneSettings());
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    public void render( ){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        sceneRender.render(camera);
    }

    public Camera getCamera(){
        return camera;
    }

    public void cleanup(){
        sceneRender.cleanup();
    }
}
