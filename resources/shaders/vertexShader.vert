#version 430 core

layout(std430, binding = 0) buffer VoxelBuffer {
    vec4 vertices[];
};

layout (std430, binding = 1) buffer NormalBuffer  {
    vec3 normals[]; // Output vertices
};

out vec3 exColour;

uniform mat4 view;
uniform mat4 projection;


void main() {
    gl_Position = projection * view  * vertices[gl_VertexID];
    exColour = normals[gl_VertexID];
}
