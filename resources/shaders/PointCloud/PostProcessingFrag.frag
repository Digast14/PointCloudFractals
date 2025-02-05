#version 430

uniform sampler2D colorTexture; // Previous frame's texture
uniform vec2 u_resolution;

out vec4 colorOut;


const float offset = 1.0 / 300.0;

void main()
{
    colorOut = texelFetch(colorTexture,ivec2( gl_FragCoord.xy), 0);
}




