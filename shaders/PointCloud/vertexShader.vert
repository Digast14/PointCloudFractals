#version 450 core


layout (std430, binding = 0) buffer Points {
    vec3 points[]; // Output vertices
};

layout (std430, binding = 1) buffer NormalBuffer {
    vec3 normals[]; // Output vertices
};

uniform mat4 view;
uniform mat4 projection;
uniform float minPointSize;
uniform float range;

out vec3 normalColor;


void main() {
    vec4 pos = vec4(points[gl_VertexID], 1.0);
    vec3 cameraPos = inverse(view)[3].xyz;
    vec3 relativePos = pos.xyz - cameraPos;
    float distanceToCamera = length(relativePos);

    normalColor = vec4(normals[gl_VertexID], 1).rgb;

    gl_Position = projection * view * pos;
    gl_PointSize = (range*minPointSize)/distanceToCamera;
}
