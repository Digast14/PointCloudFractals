package org.example.gui;

import imgui.ImGui;
import imgui.flag.ImGuiSliderFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class GuiLayer {

    private float[] startFloat = {0.0f};
    private float[] startFloatSpeed = {0.0f};

    private final float[] col = {1.0f, 1.0f, 1.0f};
    private final float[] col2 = {1.0f, 1.0f, 1.0f};
    private final float[] col3 = {0.0f, 0.0f, 0.0f};

    private boolean autoT = false;
    public boolean gameMode = false;
    private boolean booleanQZeroC = false;
    public int qZeroC = 0;
    public Vector4f qZero = new Vector4f(0f, 0f, 0f, 0f);
    private final float[] qZeroStart = {0.0f, 0.0f, 0.0f, 0.0f};


    private float counter = 0f;
    private double speed = 0.001;


    private final ImString text = new ImString(128); // Persistent buffer


    public float time = 0;
    public Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);
    public Vector3f color2 = new Vector3f(1.0f, 1.0f, 1.0f);
    public Vector3f color3 = new Vector3f(0.0f, 0.0f, 0.0f);

    public String function = "";
    public int polynomialDegree = 2;
    public boolean newFunction = false;
    public boolean resetPos = false;


    public int maxIteration = 50;
    public int maxIterationRange = 200;

    public float nudgeValue = 0.0001f;
    public float breakOutFactor = 0.1f;
    public float stepSize = 0.025f;
    public float stepSizeMult = 0.001f;
    public float zCutoff = 100;


    private final ImInt maxIterationStart = new ImInt();
    private final ImInt maxIterationStartRange = new ImInt();
    private final ImFloat nudgeValueStart = new ImFloat();
    private final ImFloat stepSizeStart = new ImFloat();
    private final ImFloat stepSizeMultStart = new ImFloat();
    private final float[] zCutOffStart = {100.0f};
    public boolean zCutoffState = false;

    private final int[] breakOutFactorStart = {10};



    public void gui() {

        ImGui.begin("Editor", ImGuiWindowFlags.AlwaysAutoResize);


        ImGui.inputText("Input", text);
        if (ImGui.button("compile Function")) {
            System.out.println("Text entered: " + text.get());
            //FunctionMakerGLSL codeEdit = new FunctionMakerGLSL(text.get());
            //function = codeEdit.code;
            System.out.println(function);
            newFunction = true;
            //polynomialDegree = codeEdit.highestPolynomial;
            System.out.println("highest polynomial degree ist:" + polynomialDegree);
        } else newFunction = false;

        if (ImGui.checkbox("custom q Zero", booleanQZeroC)) {
            booleanQZeroC = !booleanQZeroC;
            qZeroC = (booleanQZeroC) ? 1 : 0;
        }
        if(booleanQZeroC){
            if(ImGui.inputFloat4("q Zero:", qZeroStart)){
                qZero.x = qZeroStart[0];
                qZero.y = qZeroStart[1];
                qZero.z = qZeroStart[2];
                qZero.w = qZeroStart[3];
            }
        }


        if (ImGui.checkbox("gameMode", gameMode)) {
            gameMode = !gameMode;
        }

        resetPos = ImGui.button("reset Pos");


        if (ImGui.checkbox("timerAuto", autoT)) {
            autoT = !autoT;
        }

        if (autoT) {
            if (ImGui.sliderFloat("change speed", startFloatSpeed, 0.0f, 10.0f)) {
                speed = startFloatSpeed[0] * 0.001;
            } else {
                counter += (float) speed;
                startFloat[0] = (float) Math.sin(counter);
                time = startFloat[0];
            }
        }


        if (ImGui.sliderFloat("change t parameter", startFloat, -1.0f, 1.0f)) {
            time = startFloat[0];
        }

        if (ImGui.colorEdit3("color", col)) {
            color = new Vector3f(col[0], col[1], col[2]);
        }
        if (ImGui.colorEdit3("color2", col2)) {
            color2 = new Vector3f(col2[0], col2[1], col2[2]);
        }
        if (ImGui.colorEdit3("backgroundColor", col3)) {
            color3 = new Vector3f(col3[0], col3[1], col3[2]);
        }


        if (ImGui.treeNode("Shader Settings")) {
            if (ImGui.inputInt("Max Iteration", maxIterationStart)) {
                maxIteration = maxIterationStart.intValue();
            }
            if (ImGui.inputFloat("nude Value", nudgeValueStart)) {
                nudgeValue = nudgeValueStart.floatValue();
            }
            if (!gameMode) {
                if (ImGui.dragInt("breakout factor", breakOutFactorStart, 1, 0, 100, "%d%%", ImGuiSliderFlags.AlwaysClamp)) {
                    breakOutFactor = breakOutFactorStart[0] / 100f;
                }

            } else {
                if (ImGui.inputInt("Max Range Iteration", maxIterationStartRange)) {
                    maxIterationRange = maxIterationStartRange.intValue();
                }

                if (ImGui.inputFloat("Step Size", stepSizeStart)) {
                    stepSize = stepSizeStart.floatValue();
                }

                if (ImGui.inputFloat("Step Size Mult", stepSizeMultStart)) {
                    stepSizeMult = stepSizeMultStart.floatValue();
                }

                if (ImGui.checkbox("add z cutoff", zCutoffState)) {
                    zCutoffState = !zCutoffState;
                }
                if (zCutoffState) {
                    if (ImGui.sliderFloat("Change z cutoff", zCutOffStart, -10, 10)) {
                        zCutoff = zCutOffStart[0];
                    }
                } else {
                    zCutOffStart[0] = 100f;
                    zCutoff = zCutOffStart[0];
                }
            }
            ImGui.treePop();
        }
        ImGui.end();
    }

}
