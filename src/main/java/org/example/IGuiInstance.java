package org.example;

import org.example.gui.GuiLayer;
import org.example.scene.Window;


public interface IGuiInstance {
    void drawGui(GuiLayer guiLayer);

    boolean handleGuiInput(WorldRender scene, Window window);
}
