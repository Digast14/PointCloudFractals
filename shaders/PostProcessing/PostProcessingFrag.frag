#version 430

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

uniform samplerCube environmentMap;
uniform mat4 projection;
uniform mat4 view;

uniform vec3 camPosition;

out vec4 FragColor;

uniform vec2 u_resolution;
uniform vec3 backgroundColor;
uniform int mode;




const float offset = 1.0 / 600.0;


void main() {

    vec2 uv = gl_FragCoord.xy / u_resolution;
    vec4 pixelColor = texture(colorTexture, uv);
    // colorOut = pixelColor;


    if (mode == 0) {    //show normal Map
        FragColor = texture(colorTexture, uv);
    }
    else if (mode == 1) //simple normal Lighting
    {
        vec3 light = vec3(10.0, 10.0, -10.0);
        vec3 normal = texture(colorTexture, uv).xyz;
        float lightNormal = dot(normalize(light), normal);
        FragColor = vec4(lightNormal);

    }
    else if (mode == 2) //simple
    {
        float depth = texture(depthTexture, uv).r;
        depth = pow(depth, 12.0);
        FragColor = vec4(vec3(1 - depth), 1);
    }
    else if (mode == 3) //reflections!
    {
        //if (pixelColor.rgb == backgroundColor) return;
        float depth = texture(depthTexture, uv).r;
        vec3 normal = texture(colorTexture, uv).rgb * 2 - 1;

        //from screenSpace to ClipSpace
        vec4 clipSpacePos = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
        //from ClipSpace to WorldSpace
        vec4 worldPos = inverse(view) * inverse((projection)) * clipSpacePos;
        vec3 worldPost2 = worldPos.xyz / worldPos.w;

        //calcuate view Direction
        vec3 viewDir = worldPost2 - camPosition;
        viewDir = normalize(viewDir);

        if (pixelColor.rgb == backgroundColor) {
            FragColor = vec4(texture(environmentMap, -viewDir).rgb, 1.0);
        } else {
            vec3 reflectedDir = reflect(-viewDir, normal);
            FragColor = vec4(texture(environmentMap, reflectedDir).rgb, 1.0);
        }
    }
    else if (mode == 4) //blur (for some reason)
    {
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
            sampleTex[i] = vec3(texture(colorTexture, uv + offsets[i]));
        }
        vec3 col = vec3(0.0);
        for (int i = 0; i < 9; i++)
        col += sampleTex[i] * kernel[i];

        FragColor = vec4(col, 1.0);
    }
    else FragColor = pixelColor;
}
