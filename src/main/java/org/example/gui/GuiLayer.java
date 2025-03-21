package org.example.gui;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSliderFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Locale;

public class GuiLayer {
    //Always available

    //by Order:
    //Input Function
    private final ImString functionInput = new ImString(128);
    public String function = "";
    public int polynomialDegree = 2;

    //Compile Function
    public boolean newFunction = false;

    //custom Q zero
    private boolean booleanQZeroC = false;
    public int qZeroC = 0;
    public Vector4f qZero = new Vector4f(0f, 0f, 0f, 0f);
    private final float[] qZeroStart = {0.0f, 0.0f, 0.0f, 0.0f};


    //3d Fractal Point cloud
    public boolean local3dFractal = false;
    public boolean local3dFractalChangeMode = false;

    //gameMode select
    public boolean gameMode = false;

    //reset Position
    public boolean resetPos = false;

    //timer Auto & change t parameter
    public float time = 0;
    private final float[] startFloat = {0.0f};

    private float counter = 0f;
    private double speed = 0.001;

    private boolean autoT = false;
    private final float[] startFloatSpeed = {0.0f};

    // Select Colors
    private final float[] col = {1.0f, 1.0f, 1.0f};
    private final float[] col2 = {1.0f, 1.0f, 1.0f};
    private final float[] col3 = {0.0f, 0.0f, 0.0f};
    public Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);
    public Vector3f color2 = new Vector3f(1.0f, 1.0f, 1.0f);
    public Vector3f color3 = new Vector3f(0.0f, 0.0f, 0.0f);

    //-----------------------------------------------------------------------
    //Shader Settings Depend on Mode
    //Global
    //max Iteration
    private final ImInt maxIterationStart = new ImInt(50);
    public int maxIteration = 50;

    //Nudge Value
    private final ImFloat nudgeValueStart = new ImFloat(0.000001f);
    public float nudgeValue = 0.000001f;

    //Only 2D
    private final int[] breakOutFactorStart = {10};
    public float breakOutFactor = 0.1f;


    //Only Ray marching
    //Max Iteration Range
    private final ImInt maxIterationStartRange = new ImInt(200);
    public int maxIterationRange = 200;

    //step Size
    private final ImFloat stepSizeStart = new ImFloat(0.025f);
    public float stepSize = 0.025f;

    //step Size multiplier
    private final ImFloat stepSizeMultStart = new ImFloat(0.001f);
    public float stepSizeMult = 0.001f;

    //Add Z cutoff
    private final float[] zCutOffStart = {100.0f};
    private boolean zCutoffState = false;
    public float zCutoff = 100;

    //-----------------------------------------------------------------------
    //Only 3D local Fractal
    //Range
    private final ImFloat FractalRange = new ImFloat(1);
    public float range = 1;

    //normalPrecision
    private final ImInt normalPrecisionStart = new ImInt(20);
    public int normalPrecision = 20;

    //normal step Size
    private final ImFloat normalStepSizeStart = new ImFloat(0.02f);
    public float normalStepSize = 0.02f;

    //quadSize
    private final int[] quadSizeStart = {4};
    public int quadSize = 4;

    //quadSize
    private final ImInt workGroupDimensionStart = new ImInt(16);

    //Reverse
    public int reverse = 0;

    //power level
    private final int[] powerStart = {2};
    public int power = 2;

    //fps
    private int fps = 0;

    public void setFPs(int fps) {
        this.fps = fps;
    }

    //drawMode
    public boolean drawMode;

    //jitter
    private final ImFloat jitterStrengthStart = new ImFloat(0.0f);
    public float jitterStrength = 0.0f;



    //blur
    private final ImInt blurStart = new ImInt(0);
    public int blur = 0;

    //VRAM
    private final ImInt VRamStart = new ImInt(1);
    public int VRam = 1;

    //point Count
    private int pointCount = 0;

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    //Custom Size
    private boolean customDimensions;
    public final int[] cDimensions = {16, 16, 16};


    //saving Images
    public boolean saveImage = false;
    private final ImString fileNameStart = new ImString("imageSaved.png");
    public String fileName = "imageSaved.png";
    

    //-----------------------------------------------------------------------
    public void gui() {

        ImGui.begin("Editor", ImGuiWindowFlags.AlwaysAutoResize);

        ImGui.text("fps: " + fps);


        if (local3dFractal) {
            ImGui.text(String.format(Locale.US, "Points: %,d", pointCount));
        }
        ImGui.pushItemWidth(250);
        ImGui.inputText("Input", functionInput);
        if (ImGui.button("compile Function")) {
            String func = functionInput.get();
            System.out.println("Text entered: " + func);
            FunctionMakerGLSL codeEdit = new FunctionMakerGLSL(func);
            function = codeEdit.getCode();
            System.out.println("GLSL Code injection: " + function);
            System.out.println("-------------------------");
            newFunction = true;
            polynomialDegree = codeEdit.getPolynomialDegree();
        }
        ImGui.pushItemWidth(0);
        if (ImGui.checkbox("custom q Zero", booleanQZeroC)) {
            booleanQZeroC = !booleanQZeroC;
            qZeroC = (booleanQZeroC) ? 1 : 0;
        }
        if (booleanQZeroC) {
            if (ImGui.inputFloat4("q Zero:", qZeroStart)) {
                qZero.x = qZeroStart[0];
                qZero.y = qZeroStart[1];
                qZero.z = qZeroStart[2];
                qZero.w = qZeroStart[3];
            }
        }

        if (ImGui.checkbox("Local Point Cloud Fractal", local3dFractal)) {
            local3dFractal = !local3dFractal;
            local3dFractalChangeMode = true;
        } else {
            local3dFractalChangeMode = false;
        }

        resetPos = ImGui.button("reset Pos");

        if (ImGui.sliderFloat("change t parameter", startFloat, -1.0f, 1.0f)) {
            time = startFloat[0];
        }

        if (ImGui.sliderInt("power n", powerStart, 0, 10)) {
            power = powerStart[0];
        }

        if (!local3dFractal) {
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

            if (ImGui.checkbox("Graph (WIP)", gameMode)) {
                gameMode = !gameMode;
            }

            if (ImGui.colorEdit3("color", col)) {
                color = new Vector3f(col[0], col[1], col[2]);
            }
            if (ImGui.colorEdit3("color2", col2)) {
                color2 = new Vector3f(col2[0], col2[1], col2[2]);
            }
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
                ImGui.spacing();

                if(ImGui.inputText("fileName", fileNameStart, ImGuiInputTextFlags.EnterReturnsTrue)){
                    fileName = fileNameStart.get();
                }
                ImGui.sameLine();
                if(ImGui.button("Save")){
                    saveImage = true;
                }
                if (local3dFractal) {
                    if (ImGui.inputFloat("Range", FractalRange)) {
                        range = FractalRange.floatValue();
                    }

                    if (ImGui.button("invert")) {
                        reverse = (reverse + 1) % 2;
                    }

                    ImGui.spacing();

                    if(ImGui.checkbox("drawMethod", drawMode)){
                        drawMode = !drawMode;
                    }

                    ImGui.spacing();


                    if (ImGui.inputInt("normal Precision", normalPrecisionStart)) {
                        normalPrecision = normalPrecisionStart.intValue();
                    }
                    if (ImGui.inputFloat("Normal Step Size", normalStepSizeStart)) {
                        normalStepSize = normalStepSizeStart.floatValue();
                    }

                    ImGui.spacing();


                    if (ImGui.sliderInt("Quadsize", quadSizeStart, 0, 15)) {
                        quadSize = quadSizeStart[0];
                    }
                    if (ImGui.inputFloat("jitter strength", jitterStrengthStart)) {
                        jitterStrength = jitterStrengthStart.floatValue();
                    }

                    ImGui.spacing();


                    if(!customDimensions){
                        if (ImGui.inputInt("Resolution", workGroupDimensionStart)) {
                            cDimensions[0] = workGroupDimensionStart.intValue();
                            cDimensions[1] = workGroupDimensionStart.intValue();
                            cDimensions[2] = workGroupDimensionStart.intValue();
                        }
                    }
                    if (ImGui.inputInt("Gb VRAM, ", VRamStart)) {
                        VRam = VRamStart.get();
                    }

                    ImGui.spacing();

                    if (ImGui.checkbox("Custom XYZ Resolution", customDimensions)) {
                        customDimensions = !customDimensions;
                    }
                    if (customDimensions) {
                        ImGui.inputInt3("Custom XYZ Input", cDimensions);
                    }


                    if(ImGui.inputInt("Post process mode", blurStart)){
                        blur = blurStart.get();
                    }



                }
            } else {
                if (ImGui.inputInt("Max Range Iteration", maxIterationStartRange)) {
                    maxIterationRange = maxIterationStartRange.intValue();
                }

                if (ImGui.inputFloat("Graph step Count", stepSizeStart)) {
                    stepSize = stepSizeStart.floatValue();
                }

                if (ImGui.inputFloat("Graph Step Range", stepSizeMultStart)) {
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
