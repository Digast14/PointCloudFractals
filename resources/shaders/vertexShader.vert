#version 430 core



layout (std430, binding = 0) buffer Points {
    vec4 points[]; // Output vertices
};

layout (std430, binding = 1) buffer NormalBuffer {
    vec3 normals[]; // Output vertices
};

uniform mat4 view;
uniform mat4 projection;

out vec4 exColour;

void main() {

    vec4 pos = points[gl_VertexID];
    gl_Position = (projection * view) * pos;
    exColour = vec4(normals[gl_VertexID], 1);

}
