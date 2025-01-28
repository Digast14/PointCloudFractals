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
    private PointCloudRender pointCloudRender;

    private final FractalRender fractalRender;
    private final PointCloudRender.SceneSettings pointCloudSettings;




    public WorldRender(Window window) {
        GL.createCapabilities(); //Initialize OpenGL bindings
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        pointCloudSettings = new PointCloudRender.SceneSettings();
        camera = new Camera(window.getWidth(), window.getHeight());
        guiRender = new GuiRender(window);
        fractalRender = new FractalRender();
        pointCloudRender = new PointCloudRender(pointCloudSettings);



    }

    public void resize(int resX, int resY) {
        camera.resize(resX, resY);
    }


    public void update(GuiLayer guiLayer) {
        updatePointCloudSettings(guiLayer);

        if (guiLayer.newFunction && guiLayer.local3dFractal) {
            pointCloudRender.cleanup();
            pointCloudRender = new PointCloudRender(pointCloudSettings);
            pointCloudRender.initShaders(guiLayer);

        }

        if (guiLayer.local3dFractalChangeMode) {
            camera.setPosition(0.0F, 0.0F, 0.0F);
            camera.setSpeed(1);
        }
    }

    public void render(GuiLayer guiLayer, Window window) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        if (guiLayer.local3dFractal) pointCloudRender.render(guiLayer, camera);
        else fractalRender.render(camera, guiLayer, window);

        guiRender.render(guiLayer);

        int error = glGetError();
        if (error != GL_NO_ERROR) {
            System.err.println("OpenGL Error: " + error);
        }
    }




    private void updatePointCloudSettings(GuiLayer guiLayer) {
        pointCloudSettings.range = guiLayer.range;
        pointCloudSettings.workGroupDimension = guiLayer.workGroupDimension;
    }

    public Camera getCamera() {
        return camera;
    }

    public PointCloudRender getPointCloudRender() {
        return pointCloudRender;
    }


    public void cleanup() {
        pointCloudRender.cleanup();
        fractalRender.cleanup();
    }
}
