#version 450 core


struct customVec3{
    float x;
    float y;
    float z;
};

layout (std430, binding = 0) buffer Points {
    customVec3 points[];
};

layout (std430, binding = 5) buffer Points2 {
    customVec3 points2[];
};

layout (std430, binding = 1) buffer NormalBuffer {
    customVec3 normals[];
};

layout (std430, binding = 6) buffer NormalBuffer2 {
    customVec3 normals2[];
};



uniform mat4 view;
uniform mat4 projection;
uniform float minPointSize;
uniform float range;
uniform int gridDensity;
uniform float jitterStrength;

out vec3 normalColor;


float random(vec2 st) {
    return fract(sin(dot(st, vec2(12.9898, 78.233))) * 43758.5453123) * 2 - 1;
}

void main() {
    uint index = gl_VertexID;// Index of the current vertex
    vec4 pos;

    if(index < 357913941){
        pos = vec4(vec3(points[index].x,points[index].y,points[index].z), 1.0);
        normalColor = vec3(normals[index].x,normals[index].y,normals[index].z);
    }else{
        index -= 357913941;
        pos = vec4(vec3(points2[index].x,points2[index].y,points2[index].z), 1.0);
        normalColor = vec3(normals2[index].x,normals2[index].y,normals2[index].z);
    }

    //jitter
    pos.xyz += vec3(random(pos.xy), random(pos.xz + 0.31 * range), random(pos.yz + 0.17 * range)) / gridDensity * jitterStrength;

    if (minPointSize == 0) {
        gl_PointSize = 1;
    } else {
        vec3 cameraPos = inverse(view)[3].xyz;
        vec3 relativePos = pos.xyz - cameraPos;
        float distanceToCamera = length(relativePos);
        gl_PointSize = (range * minPointSize) / distanceToCamera;
    }

    gl_Position = projection * view * pos;
}
