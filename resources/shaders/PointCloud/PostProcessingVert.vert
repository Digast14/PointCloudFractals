#version 430

layout (location=3) in vec3 position;

void main()
{
    gl_Position = vec4(position, 1.0);
}