#version 430

uniform sampler2D colorTexture; // Previous frame's texture
uniform vec2 u_resolution;
uniform int blur;

out vec4 colorOut;


const float offset = 1.0 / 300.0;

void main()
{
    if (blur == 0) {
        colorOut = texture(colorTexture, gl_FragCoord.xy / u_resolution);
    } else if (blur == 2) {
       // vec3 light = vec3(10.0, 10.0, -10.0);
        //vec3 normal = texture(colorTexture, gl_FragCoord.xy / u_resolution).xyz;
        //float lightNormal = dot(normalize(light), normal);
        //colorOut = vec4(lightNormal);

    } else {
        vec2 offsets[9] = vec2[](
        vec2(-offset, offset), // top-left
        vec2(0.0f, offset), // top-center
        vec2(offset, offset), // top-right
        vec2(-offset, 0.0f), // center-left
        vec2(0.0f, 0.0f), // center-center
        vec2(offset, 0.0f), // center-right
        vec2(-offset, -offset), // bottom-left
        vec2(0.0f, -offset), // bottom-center
        vec2(offset, -offset)  // bottom-right
        );

        float kernel[9] = float[](
        1.0 / 16, 2.0 / 16, 1.0 / 16,
        2.0 / 16, 4.0 / 16, 2.0 / 16,
        1.0 / 16, 2.0 / 16, 1.0 / 16
        );

        vec3 sampleTex[9];
        for (int i = 0; i < 9; i++)
        {
            sampleTex[i] = vec3(texture(colorTexture, gl_FragCoord.xy / u_resolution + offsets[i]));
        }
        vec3 col = vec3(0.0);
        for (int i = 0; i < 9; i++)
        col += sampleTex[i] * kernel[i];

        colorOut = vec4(col, 1.0);
    }
}
