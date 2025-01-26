package org.example.render;


import org.example.Window;
import org.example.gui.GuiLayer;
import org.example.gui.GuiRender;
import org.example.scene.Camera;
import org.lwjgl.opengl.GL;


import static org.lwjgl.opengl.GL11.*;

public class WorldRender {

    private final Camera camera;
    private final GuiRender guiRender;
    private final PointCloudRender pointCloudRender;
    private final FractalRender fractalRender;


    public WorldRender(Window window) {
        GL.createCapabilities(); //Initialize OpenGL bindings
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        camera = new Camera(window.getWidth(), window.getHeight());
        guiRender = new GuiRender(window);
        pointCloudRender = new PointCloudRender( new PointCloudRender.SceneSettings());
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
            pointCloudRender.render(camera, guiLayer);
        }else {
            fractalRender.render(camera, guiLayer, window);
        }
        guiRender.render(guiLayer);
    }


    public Camera getCamera() {
        return camera;
    }


    public void cleanup() {
        pointCloudRender.cleanup();
        fractalRender.cleanup();
    }
}
