package org.example;

import org.example.gui.GuiLayer;
import org.example.render.WorldRender;

public interface IAppLogic {

    void cleanup();

    void init(Window window, WorldRender render);

    void input(Window window, WorldRender render);

    void update(Window window, WorldRender render);
}