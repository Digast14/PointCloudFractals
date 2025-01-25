package org.example.scene;


import org.joml.*;

import java.lang.Math;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Camera {

    private final Vector3f direction;
    private final Vector3f position;
    private final Vector3f right;
    private final Vector2f rotation;
    private final Vector3f up;
    private Vector3f realDirection;

    private final Matrix4f viewMatrix;
    private final Projection projection;
    private float speedMult;
    private int mode;


    public Camera(int resX, int resY) {
        direction = new Vector3f();
        right = new Vector3f();
        up = new Vector3f();
        position = new Vector3f();
        viewMatrix = new Matrix4f();
        rotation = new Vector2f();
        projection = new Projection(resX, resY);
        realDirection = new Vector3f();

        speedMult = 1;
        mode = 0;

    }
    //-----------------------------------------------
    public float getSpeed() {
        return speedMult;
    }

    public void speedUp(float inc) {
        speedMult *= inc;
    }

    public void speedDown(float inc) {
        speedMult /= inc;
    }

    public void setSpeed(float speed){
        speedMult = speed;
    }
    //-----------------------------------------------
    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        recalculate();
    }
    //-----------------------------------------------
    public int getMode() {
        return mode;
    }

    public void setBlackAndWhite() {
        mode = 0;
    }

    public void setColor() {
        mode = 1;
    }

    public void setRGB() {
        mode = 2;
    }
    //-----------------------------------------------
    public void setRotation(float x, float y) {
        rotation.set(x, y);
        recalculate();
    }

    public void addRotation(float x, float y) {
        if (rotation.x + x > Math.PI / 2.0 && rotation.x + x < Math.PI * 3 / 2.0) {
            realDirection = new Vector3f((float) (cos(-rotation.y) * cos(-rotation.x)), (float) (sin(-rotation.y) * cos(-rotation.x)), (float) sin(-rotation.x));
            rotation.add(x, y);
            recalculate();
        }
    }
    //-----------------------------------------------
    public void moveBackwards(float inc) {
        viewMatrix.positiveZ(direction).negate().mul(inc/ speedMult);
        position.sub(direction);
        recalculate();
    }

    public void moveDown(float inc) {
        viewMatrix.positiveY(up).mul(inc/ speedMult);
        position.sub(up);
        recalculate();
    }

    public void moveForward(float inc) {
        viewMatrix.positiveZ(direction).negate().mul(inc/ speedMult);
        position.add(direction);
        recalculate();
    }

    public void moveLeft(float inc) {
        viewMatrix.positiveX(right).mul(inc/ speedMult);
        position.sub(right);
        recalculate();
    }

    public void moveRight(float inc) {
        viewMatrix.positiveX(right).mul(inc / speedMult);
        position.add(right);
        recalculate();
    }

    public void moveUp(float inc) {
        viewMatrix.positiveY(up).mul(inc/ speedMult);
        position.add(up);
        recalculate();
    }
    //-----------------------------------------------

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    private void recalculate() {
        viewMatrix.identity()
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .translate(-position.x, -position.y, -position.z);
    }

    public Matrix4f getProjectionMatrix() {
        return projection.getProjMatrix();
    }

    public void resize(int resX, int resY) {
        projection.updateProjMatrix(resX, resY);
    }

    public Vector3f getDirection() {
        return realDirection;
    }
}