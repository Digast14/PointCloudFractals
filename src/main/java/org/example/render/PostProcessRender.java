package org.example.render;

import org.example.gui.GuiLayer;
import org.example.render.shader.ShaderProgramm;
import org.example.render.shader.UniformsMap;
import org.example.scene.Camera;
import org.joml.Vector2f;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public class PostProcessRender {

    private final ShaderProgramm postProcessingProgram;
    private final UniformsMap uniformsMap;

    private final int texture;
    private final int depthTexture;
    private final int cubeMapTexture;

    private int vaoId;
    private int vertexBuffer;
    private int indexBuffer;

    public PostProcessRender(int texture, int depthTexture) {
        postProcessingProgram = new ShaderProgramm("/shaders/PostProcessing/PostProcessingVert.vert", GL_VERTEX_SHADER, "/shaders/PostProcessing/PostProcessingFrag.frag", GL_FRAGMENT_SHADER);
        uniformsMap = createUniform();

        this.texture = texture;
        this.depthTexture = depthTexture;
        cubeMapTexture = createCubeMapTexture();

        createBuffers();
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


    private int createCubeMapTexture() {
        int cubeMapTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMapTexture);
        int[] width = {2048};
        int[] height = {2048};
        int[] nrChannels = {3};
        String[] faces = {"sky/right.jpg", "sky/left.jpg", "sky/top.jpg", "sky/bottom.jpg", "sky/front.jpg", "sky/back.jpg"};
        //String[] faces = {"/jpn/posx.jpg", "/jpn/negx.jpg", "/jpn/posy.jpg", "/jpn/negy.jpg", "/jpn/posz.jpg", "/jpn/negz.jpg"};

        System.out.println("-------------------------");
        for (int i = 0; i < faces.length; i++) {
            ByteBuffer data = loadTexture(faces[i], width, height, nrChannels);
            if (data != null) {
                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB, width[0], height[0], 0, GL_RGB, GL_UNSIGNED_BYTE, data);
                stbi_image_free(data);
                System.out.println("texture: " + faces[i] + " loaded successfully");
            } else {
                System.out.println("CubeMap texture failed to load:" + faces[i]);
            }
        }
        System.out.println("-------------------------");

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        return cubeMapTexture;
    }

    public static ByteBuffer loadTexture(String textureName, int[] width, int[] height, int[] channels) {
        InputStream textureStream = PostProcessRender.class.getResourceAsStream("/textures/" + textureName);
        if (textureStream == null) {
            throw new RuntimeException("Texture not found: " + textureName);
        }
        File tempFile;
        try {
            tempFile = File.createTempFile("myTexture", ".png");
            Files.copy(textureStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create texture" + e);
        }
        tempFile.deleteOnExit();
        return stbi_load(tempFile.getAbsolutePath(), width, height, channels, 0);
    }


    private UniformsMap createUniform() {
        UniformsMap uniformsMap = new UniformsMap(postProcessingProgram.getProgramID());
        uniformsMap.createUniform("mode");
        uniformsMap.createUniform("u_resolution");
        uniformsMap.createUniform("projection");
        uniformsMap.createUniform("backgroundColor");
        uniformsMap.createUniform("camPosition");
        uniformsMap.createUniform("view");
        return uniformsMap;
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
        parseUniform(sceneSettings, guiLayer, camera);

        glUniform1i(glGetUniformLocation(id, "colorTexture"), 0);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);

        glUniform1i(glGetUniformLocation(id, "depthTexture"), 1);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, depthTexture);

        glUniform1i(glGetUniformLocation(id, "environmentMap"), 2);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMapTexture);

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
