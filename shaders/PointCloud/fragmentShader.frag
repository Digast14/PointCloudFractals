#version 330 core

out vec4 FragColor;

in vec3 exColour;

void main()
{
    FragColor = vec4(exColour,1);

}