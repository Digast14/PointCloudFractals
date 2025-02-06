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

out vec4 exColour;

void main() {
    vec4 pos = vec4(points[gl_VertexID], 1.0);
    gl_Position = projection * view * pos;

    vec3 worldPos = pos.xyz;
    vec3 cameraPos = inverse(view)[3].xyz;
    vec3 relativePos = worldPos - cameraPos;
    float distanceToCamera = length(relativePos);
    /*
    if(distanceToCamera> range/2.0) gl_PointSize = minPointSize;
    else gl_PointSize = 1/distanceToCamera;
*/

    gl_PointSize = (range*minPointSize)/distanceToCamera;

    exColour = vec4(normals[gl_VertexID], 1);
}
