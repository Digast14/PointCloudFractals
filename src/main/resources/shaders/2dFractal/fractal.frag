#version 430


uniform vec2 u_resolution;
uniform vec3 u_origin;
uniform vec3 u_direction;

uniform float u_info;

uniform int u_mode;
uniform int u_gameMode;
uniform int u_qZeroC;
uniform vec4 u_qZero;

uniform vec3 u_color;
uniform vec3 u_color2;
uniform vec3 u_color3;
uniform int u_polynomialDegree;

uniform int u_maxIteration;
uniform int u_maxIterationRange;
uniform float u_nudgeValue;
uniform float u_breakoutFactor;
uniform float u_stepSize;
uniform float u_stepSizeMult;
uniform float u_zCutoff;
uniform float u_time;
uniform int u_power;

int n = u_power;

out vec4 fragColor;

#define PI 3.14159265359

float timeSin = u_time;

vec3 colors[12] = vec3[12](
vec3(1.0, 0.0, 0.0),
vec3(0.0, 1.0, 0.0),
vec3(0.0, 0.0, 1.0),
vec3(1.0, 1.0, 0.0),
vec3(1.0, 0.0, 1.0),
vec3(0.0, 1.0, 1.0),
vec3(1.0, 0.0, 0.0),
vec3(0.0, 1.0, 0.0),
vec3(0.0, 0.0, 1.0),
vec3(1.0, 1.0, 0.0),
vec3(1.0, 0.0, 1.0),
vec3(0.0, 1.0, 1.0)
);

//math function
vec4 qmul(in vec4 a, in vec4 b) {
    if (a.y == 0 && a.z == 0 && a.w == 0) return a.x * b;
    if (b.y == 0 && b.z == 0 && b.w == 0) return a * b.x;

    return vec4(
    a.x * b.x - a.y * b.y - a.z * b.z - a.w * b.w,
    a.x * b.y + a.y * b.x + a.z * b.w - a.w * b.z,
    a.x * b.z - a.y * b.w + a.z * b.x + a.w * b.y,
    a.x * b.w + a.y * b.z - a.z * b.y + a.w * b.x
    );
}

vec4 qmul(in vec4 a, in float b) {
    return vec4(a.x * b, a.y * b, a.z * b, a.w * b);
}

vec4 qmul(in float b, in vec4 a) {
    return vec4(a.x * b, a.y * b, a.z * b, a.w * b);
}

vec4 qdiv(in vec4 a, in vec4 b) {
    float normSquared = b.x * b.x + b.y * b.y + b.z * b.z + b.w * b.w;
    if (normSquared == 0.0) return vec4(0.0);

    vec4 bConjugate = vec4(b.x, -b.yzw);

    return vec4(
    (a.x * bConjugate.x - a.y * bConjugate.y - a.z * bConjugate.z - a.w * bConjugate.w) / normSquared,
    (a.x * bConjugate.y + a.y * bConjugate.x + a.z * bConjugate.w - a.w * bConjugate.z) / normSquared,
    (a.x * bConjugate.z - a.y * bConjugate.w + a.z * bConjugate.x + a.w * bConjugate.y) / normSquared,
    (a.x * bConjugate.w + a.y * bConjugate.z - a.z * bConjugate.y + a.w * bConjugate.x) / normSquared
    );
}

//expanded functions
vec4 qsin(vec4 q) {
    if (q == vec4(0)) return vec4(0);
    float a = q.x;
    vec3 v = vec3(q.yzw);
    float vabs = length(v);
    return vec4(sin(a) * cosh(vabs), cos(a) * sinh((vabs)) * v / vabs);
}
vec4 qcos(vec4 q) {
    if (q == vec4(0)) return vec4(0);
    float a = q.x;
    vec3 v = vec3(q.yzw);
    float vabs = length(v);
    return vec4(cos(a) * cosh(vabs), -sin(a) * sinh((vabs)) * v / vabs);
}
vec4 qexp(vec4 q) {
    if (q == vec4(0)) return vec4(0);
    if (dot(q, q) == 0) return vec4(0);
    float expA = exp(q.x);
    vec3 v = vec3(q.yzw);
    float vabs = length(v);
    return vec4(expA * cos(vabs), expA * (v / vabs * sin(vabs)));
}

vec4 qln(vec4 q) {
    if (q == vec4(0)) return vec4(0);
    if (dot(q, q) == 0) return vec4(0);
    float qabs = length(q);
    float ln = log(qabs);
    float a = q.x;
    vec3 v = vec3(q.yzw);
    float vabs = length(v);
    return vec4(ln, (v / vabs) * acos(a / qabs));
}

vec4 qpow(vec4 q, float n) {
    if (n == 2) return qmul(q, q);
    if (n.x == 3) return qmul(qmul(q, q), q);
    return qexp(n * qln(q));
}

vec4 qpow(vec4 q, vec4 n) {
    if (n.x == 2) return qmul(q, q);
    if (n.x == 3) return qmul(qmul(q, q), q);
    return qexp(n.x * qln(q));
}



//Polynomial degree
const float roootN = 3;

// Function to calculate roots of unity (ony for standard Newton Fractals)
vec4 roots[int(roootN)];
void calculateRootsOfUnity(int m, out vec4 roots[int(roootN)]) {
    for (int i = 0; i < m; i++) {
        float angle = 2.0 * PI * float(i) / float(m);
        roots[i] = vec4(cos(angle), sin(angle), 0, 0);
    }
}


// Function placeholder (name javaFunction(z))
vec4 javaFunction(vec4 q, vec4 c) {
    return /**/qsin(q);
}



vec3 NewtonFractalQuaternion(in vec4 c) {
    float tolerance = 0.1;
    vec4 z;
    if (u_qZeroC == 0) z = c;
    else z = u_qZero;
    int maxIteration = 200;
    for (int iteration = 0; iteration < maxIteration; iteration++) {
        z = javaFunction(z, c);
        for (int i = 0; i < roootN; i++) {
            if (length(z - roots[i]) < tolerance) {
                return colors[i] * (1 - iteration / float(maxIteration));
            }
        }
    }
    return vec3(0.0);
}


vec3 NewtonMethod2(in vec4 c) {
    vec4 z;
    float breakOut = 64.0;
    if (u_qZeroC == 0) z = c;
    else z = u_qZero;

    int iteration = 0;

    for (; iteration < u_maxIteration; iteration++) {
        z = javaFunction(z, c);
        if (length(z) > breakOut && (iteration > u_maxIteration * u_breakoutFactor)) break;
    }
    float sit = iteration - log2(log2(length(z)) / (log2(breakOut))) / log2(float(u_polynomialDegree));
    vec3 col = 0.5 + 0.5 * cos(3.0 + sit * 0.075 * u_polynomialDegree + vec3(0.0, 0.6, 1.0));
    return col;
}

vec3 NewtonMethod(in vec4 c) {
    vec4 z;
    vec4 zNudge;

    if (u_qZeroC == 0) {
        z = c;
        zNudge = c + c * u_nudgeValue;
    } else {
        z = u_qZero;
        zNudge = z;
    }
    for (int iteration = 0; iteration < u_maxIteration; iteration++) {

        z = javaFunction(z, c);
        zNudge = javaFunction(zNudge, c + c * u_nudgeValue);
        if (length(z - zNudge) > 1 && (iteration > u_maxIteration * u_breakoutFactor)) {
            float mixValue = (1 - (iteration) / float(u_maxIteration * 0.9));
            return mix(u_color, u_color2, mixValue) * mixValue;
        }
    }
    return u_color3;
}


//Raymarching
vec3 rayMarch(vec3 origin, vec3 dir) {
    float t = 0.0;
    for (int i = 0; i < u_maxIterationRange; i++) {
        vec3 pos = origin + t * dir;
        t += u_stepSize + i * u_stepSizeMult;

        if (pos.z > u_zCutoff) continue;
        vec3 color;

        if (u_mode == 0)color = NewtonMethod(vec4(pos.xyz, 0));
        if (u_mode == 1)color = NewtonMethod2(vec4(pos.xyz, 0));
        if (u_mode == 2)color = NewtonFractalQuaternion(vec4(pos.xyz, 0));

        if (color != vec3(0.0)) return color;
    }
    return u_color3;
}

//Graph Drawing
float Graph(in float x) {
    vec4 z;
    if (u_qZeroC == 0) z = vec4(x, 0, 0, 0);
    else z = vec4(u_qZero.x, 0, 0, 0);
    for (int iteration = 0; iteration < u_maxIteration; iteration++) {
        z = javaFunction(z, vec4(x, 0, 0, 0));
    }
    return -z.x;
}

float plot2D(vec2 st, float lineWidth) {
    float minDist = 1e12;

    for (float offset = -1;  offset <= 1; offset += 3/u_stepSize) {
        float xCoord = st.x + offset * lineWidth;
        float yCoord = Graph(xCoord);
        float dist = length(st - vec2(xCoord, yCoord)); // True Euclidean distance
        minDist = min(minDist, dist);
    }
    return minDist;
}

vec3 grid2D(in vec2 _st, in float _width) {
    float axisDetail = _width;
    if (abs(_st.x) < axisDetail || abs(_st.y) < axisDetail)
    return  u_color;
    if (abs(mod(_st.x, 1.0)) < axisDetail || abs(mod(_st.y, 1.0)) < axisDetail)
    return  u_color2;
    if (abs(mod(_st.x, 0.25)) < axisDetail || abs(mod(_st.y, 0.25)) < axisDetail)
    return 1.0 - vec3(0.95, 0.95, 1.0);
    return vec3(0.0);
}

//camera function
vec3 rotateByVec3(in vec3 vector, in vec3 axis, in float angle) {
    axis = normalize(axis);
    float cosAngle = cos(angle);
    return vector * cosAngle + (cross(axis, vector)) * sin(angle) + axis * (dot(axis, vector)) * (1 - cosAngle);
}


void main() {
    calculateRootsOfUnity(int(roootN), roots);

    float aspectRatio = u_resolution.x / u_resolution.y;
    vec2 uv = vec2(((gl_FragCoord.x / u_resolution.y) - (aspectRatio) * 0.5), -((gl_FragCoord.y / u_resolution.y) - 0.5));
    vec3 ro = vec3(-u_origin.z, u_origin.x, u_origin.y);
    vec4 pixelCoord = vec4(uv / u_info + ro.xy, 0, 0);
    vec3 color;

    if (u_gameMode == 0) {
        if (u_mode == 0)color = NewtonMethod(pixelCoord);
        if (u_mode == 1)color = NewtonMethod2(pixelCoord);
        if (u_mode == 2)color = NewtonFractalQuaternion(pixelCoord);
    } else if (u_gameMode == 1) {
        float dist = plot2D(pixelCoord.xy, u_stepSizeMult);
        float intensity = smoothstep(0., 1., 1. - dist * 50);
        intensity = pow(intensity,1./2.2);
        color = vec3(intensity);
        color += grid2D(pixelCoord.xy, 0.005);
    } else {
        vec3 rd = u_direction;
        vec3 camLeftNormal = cross(u_direction, vec3(0.0, 0.0, 1.0));
        vec3 camUpNormal = cross(u_direction, camLeftNormal);
        //rotate up or down
        rd = rotateByVec3(rd, camUpNormal, uv.x);
        //roate left or right
        rd = rotateByVec3(rd, camLeftNormal, uv.y);
        color = rayMarch(ro, rd);
    }
    fragColor = vec4(color, 1);
}

