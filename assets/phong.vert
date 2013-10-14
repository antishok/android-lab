struct Light {
  vec4 ecPos;
  vec4 ambient;  
  vec4 diffuse;
  vec4 specular;  
};


uniform mat4 uMVPMatrix;
uniform mat4 uMVMatrix;
uniform mat4 uNormalMatrix; // transpose of inverse of mv matrix

uniform Light uLight;

attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec4 aColor;

varying vec4 vDiffuse, vAmbient;
varying vec3 vNormal, vHalfVector;
varying vec3 vLightDir;

void main() {
   
  vec3 ecPosition = vec3(uMVMatrix * aPosition);           // convert vertex position to eye-coordinates
  vLightDir = normalize(vec3(uLight.ecPos) - ecPosition);   // vector from vertex to lightPos
  vNormal = normalize(vec3(uNormalMatrix * vec4(aNormal, 0.0)));  // (can multiply by either mvMatrix or normalMatrix. normalMatrix is better when mvMatrix contains non-uniform scales)
  vec3 eyeDir = -ecPosition;   // vector from vertex to eye (in eye coordinates, so it's just negative of vertexPos)
  vHalfVector = normalize(eyeDir + vLightDir);   // half-vector between eye-vector and light-vector, pass on to fragment shader
            
  vec4 globalAmbient = aColor * vec4(0.2, 0.2, 0.2, 1.0); // material-ambient * global-ambient
  vAmbient = globalAmbient + aColor * uLight.ambient; // global + material-ambient * light-ambient
  vDiffuse =  aColor * uLight.diffuse; // material-diffuse * light-diffuse
    		
  gl_Position = uMVPMatrix * aPosition;
}

