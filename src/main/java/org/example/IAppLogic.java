package org.example;

import org.example.scene.Scene;
import org.example.scene.Window;

public interface IAppLogic {

    void cleanup();

    void init(Window window, Scene scene);

    void input(Window window, Scene scene);

    void update(Window window, Scene scene);
}