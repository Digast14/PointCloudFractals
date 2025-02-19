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
uniform int gridDensity;
uniform float jitterStrength;

out vec3 normalColor;


float random (vec2 st) {
    return fract(sin(dot(st, vec2(12.9898,78.233))) * 43758.5453123)*2-1;
}

void main() {
    vec4 pos = vec4(points[gl_VertexID], 1.0);
    pos.xyz += vec3(random(pos.xy), random(pos.xz + 0.31 * range), random(pos.yz + 0.17 * range)) / gridDensity * jitterStrength;

    if(minPointSize==0){
        gl_PointSize = 1;
    }else{
        vec3 cameraPos = inverse(view)[3].xyz;
        vec3 relativePos = pos.xyz - cameraPos;
        float distanceToCamera = length(relativePos);
        gl_PointSize = (range*minPointSize)/distanceToCamera;
    }

    normalColor = normals[gl_VertexID];
    gl_Position = projection * view * pos;

}
