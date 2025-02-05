package org.example.render;

import org.example.render.shader.ShaderProgramm;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class PostProcessRender {

    private ShaderProgramm postProcessingProgram;

    private int texture;
    private int vaoId;
    private int vertexBuffer;
    private int indexBuffer;

    public PostProcessRender(int texture){
        this.texture = texture;
        postProcessingProgram = new ShaderProgramm("resources/shaders/PointCloud/PostProcessingVert.vert", GL_VERTEX_SHADER, "resources/shaders/PointCloud/PostProcessingFrag.frag", GL_FRAGMENT_SHADER);
        createBuffers();
    }

    private void createBuffers(){
        float[] vertices = {
                -1.0f, -1.0f, // Bottom-left
                1.0f, -1.0f, // Bottom-right
                1.0f, 1.0f, // Top-right
                -1.0f, 1.0f  // Top-left
        };
        int[] indices = {
                0, 1, 2, // First triangle
                2, 3, 0  // Second triangle
        };
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        // Positions VBO

        vertexBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        // Index VBO

        indexBuffer = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(3);
    }

    public void render(int width, int height){
        postProcessingProgram.bind();
        glUniform1i(glGetUniformLocation(postProcessingProgram.getProgramID(), "colorTexture"), 0);
        glUniform2f(glGetUniformLocation(postProcessingProgram.getProgramID(), "u_resolution"), width, height);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        postProcessingProgram.unbind();
    }

    public void cleanUp(){
        glDeleteBuffers(indexBuffer);
        glDeleteBuffers(vertexBuffer);
        postProcessingProgram.cleanup();
    }
}
