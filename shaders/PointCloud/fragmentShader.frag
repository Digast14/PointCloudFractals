#version 330 core

out vec4 FragColor;

in vec3 normalColor;

void main() {
    FragColor = vec4(normalColor,1);
}