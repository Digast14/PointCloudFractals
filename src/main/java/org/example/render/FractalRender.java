package org.example.render;

import org.example.Window;
import org.example.gui.GuiLayer;
import org.example.scene.Camera;
import org.example.render.shader.ShaderProgramm;
import org.example.render.shader.UniformsMap;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

public class FractalRender {

    private ShaderProgramm shaderProgram;
    private UniformsMap uniformsMap;

    private int vaoId;
    private int vertexBuffer;
    private int indexBuffer;


    public FractalRender() {
        shaderProgram = new ShaderProgramm("shaders/2dFractal/fractal.vert", GL_VERTEX_SHADER, "shaders/2dFractal/fractal.frag", GL_FRAGMENT_SHADER);
        System.out.println("Fractal fragment shader id: " + shaderProgram.getProgramID());
        createBuffers();
        createUniforms();
    }

    private void createBuffers() {
        // Set up vertex data
        float[] vertices = {
                -1.0f, -1.0f, // Bottom-left
                1.0f, -1.0f, // Bottom-right
                1.0f, 1.0f, // Top-right
                -1.0f, 1.0f  // Top-left
        };
        int[] indices2 = {
                0, 1, 2, // First triangle
                2, 3, 0  // Second triangle
        };
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        // Positions VBO
        vertexBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 2 * Float.BYTES, 0);
        // Index VBO
        indexBuffer = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices2, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
    }

    public void render(Camera camera, GuiLayer guiLayer, Window window) {
        shaderProgram.bind();

        parseUniform(camera, guiLayer, window);
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
        shaderProgram.unbind();

        shaderChanges(guiLayer);
    }

    private void shaderChanges(GuiLayer guiLayer) {
        if (guiLayer.newFunction) {
            shaderProgram.editShader(GL_FRAGMENT_SHADER, GL_VERTEX_SHADER, guiLayer.function);
            createUniforms();
            guiLayer.newFunction = false;
        }

    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramID());
        uniformsMap.createUniform("u_resolution");
        uniformsMap.createUniform("u_info");
        uniformsMap.createUniform("u_origin");
        uniformsMap.createUniform("u_direction");
        uniformsMap.createUniform("u_time");
        uniformsMap.createUniform("u_color");
        uniformsMap.createUniform("u_color2");
        uniformsMap.createUniform("u_color3");
        uniformsMap.createUniform("u_mode");
        uniformsMap.createUniform("u_gameMode");
        uniformsMap.createUniform("u_maxIteration");
        uniformsMap.createUniform("u_maxIterationRange");
        uniformsMap.createUniform("u_nudgeValue");
        uniformsMap.createUniform("u_breakoutFactor");
        uniformsMap.createUniform("u_stepSize");
        uniformsMap.createUniform("u_stepSizeMult");
        uniformsMap.createUniform("u_zCutoff");
        uniformsMap.createUniform("u_qZeroC");
        uniformsMap.createUniform("u_qZero");
        uniformsMap.createUniform("u_polynomialDegree");
        uniformsMap.createUniform("u_power");

    }


    private void parseUniform(Camera camera, GuiLayer guiLayer, Window window) {
        if (guiLayer.resetPos) camera.setPosition(0, 0, 0);

        uniformsMap.setUniform("u_resolution", new Vector2f(window.getWidth(), window.getHeight()));
        uniformsMap.setUniform("u_info", camera.getSpeed());
        uniformsMap.setUniform("u_origin", camera.getPosition());
        uniformsMap.setUniform("u_mode", camera.getMode());
        uniformsMap.setUniform("u_direction", camera.getDirection());
        uniformsMap.setUniform("u_time", guiLayer.time);
        uniformsMap.setUniform("u_color", guiLayer.color);
        uniformsMap.setUniform("u_color2", guiLayer.color2);
        uniformsMap.setUniform("u_color3", guiLayer.color3);
        uniformsMap.setUniform("u_maxIteration", guiLayer.maxIteration);
        uniformsMap.setUniform("u_maxIterationRange", guiLayer.maxIterationRange);
        uniformsMap.setUniform("u_nudgeValue", guiLayer.nudgeValue);
        uniformsMap.setUniform("u_breakoutFactor", guiLayer.breakOutFactor);
        uniformsMap.setUniform("u_stepSize", guiLayer.stepSize);
        uniformsMap.setUniform("u_stepSizeMult", guiLayer.stepSizeMult);
        uniformsMap.setUniform("u_zCutoff", guiLayer.zCutoff);
        uniformsMap.setUniform("u_qZeroC", guiLayer.qZeroC);
        uniformsMap.setUniform("u_qZero", guiLayer.qZero);
        uniformsMap.setUniform("u_polynomialDegree", guiLayer.polynomialDegree);
        uniformsMap.setUniform("u_power", guiLayer.power);

        if (guiLayer.gameMode) {
            uniformsMap.setUniform("u_gameMode", 1);
        } else {
            uniformsMap.setUniform("u_gameMode", 0);
            camera.setRotation(0, (float) -(Math.PI / 2.0));
        }
    }

    public void cleanup() {
        glDeleteVertexArrays(vertexBuffer);
        glDeleteVertexArrays(indexBuffer);
    }

}
