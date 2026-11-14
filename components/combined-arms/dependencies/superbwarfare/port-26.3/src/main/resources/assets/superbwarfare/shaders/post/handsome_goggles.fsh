#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform HandsomeConfig {
    float RainbowTime;
};

layout(location = 0) out vec4 fragColor;

vec3 hue2rgb(float h) {
    float r = abs(h * 6.0 - 3.0) - 1.0;
    float g = 2.0 - abs(h * 6.0 - 2.0);
    float b = 2.0 - abs(h * 6.0 - 4.0);
    return clamp(vec3(r, g, b), 0.0, 1.0);
}

float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

void main() {
    float pixRes = 1024.0;
    vec2 pixCoord = floor(texCoord * vec2(pixRes, pixRes * 0.5625)) / vec2(pixRes, pixRes * 0.5625);

    vec4 sceneColor = texture(InSampler, pixCoord);
    float gray = dot(sceneColor.rgb, vec3(0.299, 0.587, 0.114));
    float boosted = pow(gray, 0.4);

    float flowSpeed = 0.25;
    float hue = fract((1.0 - pixCoord.x) - RainbowTime * flowSpeed);
    vec3 finalColor = hue2rgb(hue) * boosted;

    float grain = (random(pixCoord + RainbowTime) - 0.5) * 0.12;
    finalColor += grain;

    vec2 uv = texCoord * (1.0 - texCoord.yx);
    float vig = pow(clamp(uv.x * uv.y * 15.0, 0.0, 1.0), 0.3);
    finalColor *= (0.45 + 0.55 * vig);

    fragColor = vec4(finalColor, 1.0);
}
