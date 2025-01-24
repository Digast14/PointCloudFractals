package org.example;

import org.example.scene.Window;

public interface IAppLogic {

    void cleanup();

    void init(Window window, WorldRender render);

    void input(Window window, WorldRender render);

    void update(Window window, WorldRender render);
}