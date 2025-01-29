#version 430 core

layout (std430, binding = 0) buffer VoxelBuffer {
    vec4 vertices[];
};

layout (std430, binding = 1) buffer NormalBuffer {
    vec3 normals[]; // Output vertices
};

uniform mat4 view;
uniform mat4 projection;

out vec4 exColour;

void main() {
    gl_Position = projection * view * vertices[gl_VertexID];
    exColour = vec4(normals[gl_VertexID], 1);
}
