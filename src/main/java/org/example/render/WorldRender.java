package org.example.render;


import org.example.Window;
import org.example.gui.GuiLayer;
import org.example.gui.GuiRender;
import org.example.scene.Camera;
import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_VERTEX_PROGRAM_POINT_SIZE;

public class WorldRender {

    private final Camera camera;
    private final GuiRender guiRender;
    private PointCloudRender pointCloudRender;

    private final FractalRender fractalRender;
    private final PointCloudRender.SceneSettings pointCloudSettings;


    public WorldRender(Window window) {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);

        camera = new Camera(window.getWidth(), window.getHeight());
        guiRender = new GuiRender(window);
        fractalRender = new FractalRender();
        pointCloudSettings = new PointCloudRender.SceneSettings();
        pointCloudSettings.width = window.getWidth();
        pointCloudSettings.height = window.getHeight();
        pointCloudRender = new PointCloudRender(pointCloudSettings);
    }


    public void resize(int width, int height) {
        camera.resize(width, height);
        pointCloudRender.resize(width, height);
        pointCloudSettings.width = width;
        pointCloudSettings.height = height;
    }


    public void update(GuiLayer guiLayer) {
        updatePointCloudSettings(guiLayer);

        if (guiLayer.newFunction && guiLayer.local3dFractal) {
            pointCloudRender.cleanup();
            pointCloudRender = new PointCloudRender(pointCloudSettings);
            pointCloudRender.initShaders(guiLayer);
            guiLayer.newFunction = false;
        }

        if (guiLayer.local3dFractalChangeMode) {
            camera.setPosition(0.0F, 0.0F, 0.0F);
            camera.setSpeed(1);
            camera.setRotation((float) Math.PI, 0);
        }
    }

    public void render(GuiLayer guiLayer, Window window) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(guiLayer.color3.x,guiLayer.color3.y,guiLayer.color3.z,1);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        if (guiLayer.local3dFractal) pointCloudRender.render(guiLayer, camera);
        else fractalRender.render(camera, guiLayer, window);

        guiRender.render(guiLayer);
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
