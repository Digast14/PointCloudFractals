package org.example.render.shader;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLDebugMessageCallback;

import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGLDebug {
    private static GLDebugMessageCallback debugMessageCallback;

    public static void setupDebugCallback() {
        if (!GL.getCapabilities().GL_KHR_debug) {
            System.out.println("OpenGL Debugging not supported!");
            return;
        }

        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS); // Ensure messages are processed in order

        debugMessageCallback = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {

            if (severity == GL_DEBUG_SEVERITY_NOTIFICATION) {
                return;
            }
            if (type == GL_DEBUG_TYPE_PERFORMANCE && severity == GL_DEBUG_SEVERITY_MEDIUM) {
                return; //skip error that notify about possible performance problems, especially about buffer being read by the CPU/
            }

            String msg = GLDebugMessageCallback.getMessage(length, message);
            System.err.println("OpenGL Debug Message: " + msg);

            // You can filter based on severity
            if (severity == GL_DEBUG_SEVERITY_HIGH) {
                throw new RuntimeException("Severe OpenGL error: " + msg);
            }
        });

        glDebugMessageCallback(debugMessageCallback, NULL);
    }

    public static void cleanup() {
        if (debugMessageCallback != null) {
            debugMessageCallback.free();
        }
    }
}
