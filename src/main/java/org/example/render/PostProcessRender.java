package org.example.render;

import org.example.gui.GuiLayer;
import org.example.render.shader.ShaderProgramm;
import org.example.render.shader.UniformsMap;
import org.example.scene.Camera;
import org.joml.Vector2f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public class PostProcessRender {

    private ShaderProgramm postProcessingProgram;

    private int texture;
    private int depthTexture;
    private int cubemapTexture;

    private int vaoId;
    private int vertexBuffer;
    private int indexBuffer;

    private UniformsMap uniformsMap;

    public PostProcessRender(int texture, int depthTexture) {
        this.texture = texture;
        this.depthTexture = depthTexture;
        postProcessingProgram = new ShaderProgramm("shaders/PostProcessing/PostProcessingVert.vert", GL_VERTEX_SHADER, "shaders/PostProcessing/PostProcessingFrag.frag", GL_FRAGMENT_SHADER);
        createBuffers();
        createCubeMapTexture();
        createUniform();
    }


    private void createBuffers() {
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


    public void createCubeMapTexture() {
        cubemapTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapTexture);
        int[] width = {2048};
        int[] height = {2048};
        int[] nrChannels = {3};
        String[] faces = {"textures/sky/right.jpg", "textures/sky/left.jpg", "textures/sky/top.jpg", "textures/sky/bottom.jpg", "textures/sky/front.jpg", "textures/sky/back.jpg"};
        for (int i = 0; i < faces.length; i++) {
            ByteBuffer data = stbi_load(faces[i], width, height, nrChannels, 0);
            if(data != null){
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB, width[0], height[0], 0, GL_RGB, GL_UNSIGNED_BYTE, data);
                stbi_image_free(data);
                System.out.println("texture: " + faces[i] + " loaded successfully");
            }else{
                System.out.println("CubeMap texture failed to load:" + faces[i]);
            }
        }
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
    }

    private void createUniform(){
        uniformsMap = new UniformsMap(postProcessingProgram.getProgramID());
        uniformsMap.createUniform("mode");
        uniformsMap.createUniform("u_resolution");
        uniformsMap.createUniform("projection");
        uniformsMap.createUniform("backgroundColor");
        uniformsMap.createUniform("camPosition");
        uniformsMap.createUniform("view");

    }

    private void parseUniform(PointCloudRender.SceneSettings sceneSettings, GuiLayer guiLayer, Camera camera) {
        uniformsMap.setUniform("mode", guiLayer.blur);
        uniformsMap.setUniform("u_resolution", new Vector2f(sceneSettings.width, sceneSettings.height));
        uniformsMap.setUniform("projection", camera.getProjectionMatrix());
        uniformsMap.setUniform("backgroundColor", guiLayer.color3);
        uniformsMap.setUniform("camPosition", camera.getPosition());
        uniformsMap.setUniform("view", camera.getViewMatrix());

    }



    public void render(PointCloudRender.SceneSettings sceneSettings, GuiLayer guiLayer, Camera camera) {
        postProcessingProgram.bind();
        int id = postProcessingProgram.getProgramID();
        parseUniform(sceneSettings,guiLayer,camera);


        glUniform1i(glGetUniformLocation(id, "colorTexture"), 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);

        glUniform1i(glGetUniformLocation(id, "depthTexture"), 1);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, depthTexture);

        glUniform1i(glGetUniformLocation(id, "environmentMap"), 2);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapTexture);

        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

        postProcessingProgram.unbind();
    }

    public void cleanUp() {
        glDeleteBuffers(indexBuffer);
        glDeleteBuffers(vertexBuffer);
        postProcessingProgram.cleanup();
    }
}
