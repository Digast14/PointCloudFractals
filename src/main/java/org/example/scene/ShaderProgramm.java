package org.example.scene;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glShaderSource;


public class ShaderProgramm {

    private int programID;
    private String source1;
    private String source2;
    private Map<Integer, String> sourceCodes;

    public ShaderProgramm(String path, int type, String path2, int type2) {
        sourceCodes = new HashMap<>();
        sourceCodes.put(type, path);
        sourceCodes.put(type2, path2);
        int shader1 = createShaderFromSource(readFile(path), type);
        int shader2 = createShaderFromSource(readFile(path2), type2);
        programID = createProgram(shader1, shader2);
        glDeleteShader(shader1);
        glDeleteShader(shader2);
    }

    public ShaderProgramm(String path, int type) {
        sourceCodes = new HashMap<>();
        sourceCodes.put(type, path);
        int shader1 = createShaderFromSource(readFile(path), type);
        programID = createProgram(shader1);
        glDeleteShader(shader1);
    }

    public void editShader(int editType, int otherType, String replacement) {
        glDeleteProgram(programID);
        String source = readFile(sourceCodes.get(editType));
        String sourceEdit = source.replace("/**/qsin(q)", replacement);

        int shaderEdit = glCreateShader(editType);
        glShaderSource(shaderEdit, sourceEdit);
        glCompileShader(shaderEdit);

        if (glGetShaderi(shaderEdit, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Failed to compile shader: " + glGetShaderInfoLog(shaderEdit));
            shaderEdit = glCreateShader(editType);
            glShaderSource(shaderEdit, source);
            glCompileShader(shaderEdit);
        }

        if(sourceCodes.size()==1){
            int shader1 = shaderEdit;
            programID = createProgram(shader1);
            glDeleteShader(shader1);
        }

        if(sourceCodes.size()==2){
            int shader1 = shaderEdit;
            int shader2 = createShaderFromSource(readFile(sourceCodes.get(otherType)),otherType);
            programID = createProgram(shader1, shader2);
            glDeleteShader(shader1);
        }
        System.out.println("succesful edit");
    }




    private static int createShaderFromSource(String source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Failed to compile shader: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private static int createProgram(int shader) {
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, shader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program linking failed: " + glGetProgramInfoLog(shaderProgram));
        }
        return shaderProgram;
    }

    private static int createProgram(int fragmentShader, int vertexShader) {
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program linking failed: " + glGetProgramInfoLog(shaderProgram));
        }
        return shaderProgram;
    }

    public static String readFile(String filePath) {
        String str;
        try {
            str = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException excp) {
            throw new RuntimeException("Error reading file [" + filePath + "]", excp);
        }
        return str;
    }

    public int getProgramID() {
        return programID;
    }

    public void bind() {
        glUseProgram(programID);
    }

    public void unbind() {
        glUseProgram(0);
    }


    public void cleanup() {
        unbind();
        if (programID != 0) {
            glDeleteProgram(programID);
        }
    }



    /*
    public static int shaderMaker(String path, int type) {
        int shader = createShaderFromSource(readFile(path), type);
        int program = createProgram(shader);
        glDeleteShader(shader);
        return program;
    }

    public static int shaderMaker(String path, int type, String path2, int type2) {
        int shader1 = createShaderFromSource(readFile(path), type);
        int shader2 = createShaderFromSource(readFile(path2), type2);
        int program = createProgram(shader1, shader2);
        glDeleteShader(shader1);
        glDeleteShader(shader2);
        return program;
    }

    public static String readFile(String filePath) {
        String str;
        try {
            str = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException excp) {
            throw new RuntimeException("Error reading file [" + filePath + "]", excp);
        }
        return str;
    }

    private static int createShaderFromSource(String source, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Failed to compile shader: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private static int createProgram(int shader) {
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, shader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program linking failed: " + glGetProgramInfoLog(shaderProgram));
        }
        return shaderProgram;
    }

    private static int createProgram(int fragmentShader, int vertexShader) {
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader program linking failed: " + glGetProgramInfoLog(shaderProgram));
        }
        return shaderProgram;
    }
     */
}

