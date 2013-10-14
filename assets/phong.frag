precision mediump float;

struct Light {
  highp vec4 ecPos;
  highp vec4 ambient;  
  highp vec4 diffuse;
  highp vec4 specular;
};

uniform Light uLight;

varying vec4 vDiffuse, vAmbient;
varying vec3 vNormal, vHalfVector;
varying vec3 vLightDir;

void main() {
  //  if (!gl_FrontFacing) { return; }
  vec3 lightDir = normalize(vLightDir);
  vec3 n = normalize(vNormal);
  vec4 color = vAmbient;
  vec3 halfV;
  float NdotL, NdotHV;
            
  NdotL = max(dot(n, lightDir), 0.0);
  if (NdotL > 0.0) {
    color += vDiffuse * NdotL;
    halfV = normalize(vHalfVector);
    NdotHV = max(dot(n,halfV), 0.0);
    color += vec4(0.2,0.3,0.5,1.0) * uLight.specular * pow(NdotHV, 16.0); //gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV, gl_FrontMaterial.shininess);
  }
            
  gl_FragColor = color;
}
