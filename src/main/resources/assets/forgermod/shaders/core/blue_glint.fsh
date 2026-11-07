#version 150

#moj_import <minecraft:fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float GlintAlpha;

in float vertexDistance;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    float intensity = max(color.r, max(color.g, color.b));
    color.rgb = intensity * vec3(0.15, 0.45, 1.0);
    float fade = linear_fog_fade(vertexDistance, FogStart, FogEnd) * GlintAlpha;
    fragColor = vec4(color.rgb * fade, color.a);
}
