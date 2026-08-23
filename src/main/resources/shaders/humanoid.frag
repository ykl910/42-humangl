#version 410 core
in vec3 vNormal;
in vec3 vWorldPosition;
uniform vec3 uColor;
out vec4 fragColor;
void main() {
    vec3 lightDirection = normalize(vec3(-0.5, 1.0, 0.8));
    float diffuse = max(dot(normalize(vNormal), lightDirection), 0.0);
    float light = 0.28 + diffuse * 0.72;
    fragColor = vec4(uColor * light, 1.0);
}
