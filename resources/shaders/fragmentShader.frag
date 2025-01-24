#version 330 core

out vec4 colorOut;
in  vec3 exColour;

void main() {
    colorOut = vec4(exColour,1); // Red color
}