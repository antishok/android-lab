uniform mat4 uMVPMatrix;
uniform mat4 uMVMatrix;
uniform mat4 uNormalMatrix; // transpose of inverse of mv matrix (needed if mv matrix contains non-uniform scales)
uniform vec4 uEcLightPos; // positional light
//uniform vec4 uEcDirectionalLightDir;

attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec4 aColor;
        
varying vec4 vColor;
                
void main() {
  vec4 globalAmbient, ambient, diffuse, specular = vec4(0);
  vec3 ecPosition = vec3(uMVMatrix * aPosition);
  vec3 ecNormal = normalize(vec3(uNormalMatrix * vec4(aNormal, 0.0))); // (can multiply by either mvMatrix or normalMatrix. normalMatrix is better when mvMatrix contains non-uniform scales)
        
  vec3 lightDir = normalize(vec3(uEcLightPos) - ecPosition); // L = vertex-to-lightpos vector, in eye-coordinates 
  //vec3 directionalLightDir = normalize(vec3(-uEcDirectionalLightDir));
  float NdotL = max(dot(ecNormal, lightDir), 0.0);
        
  if (NdotL > 0.0) {
  	vec3 eyeDir = -ecPosition; // vertex-to-eye vector, in eye-coordinates
  	vec3 lightHalfVector = normalize(eyeDir + lightDir);
  	float NdotHV = max(dot(ecNormal, lightHalfVector), 0.0);
    specular = vec4(0.2,0.3,0.5,1.0) * vec4(0.8,0.8,0.8,1.0) * pow(NdotHV, 16.0); // gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV,gl_FrontMaterial.shininess);
  }
    		
  globalAmbient = aColor * vec4(0.2, 0.2, 0.2, 1.0); // material-ambient * global-ambient
  ambient = aColor * vec4(0.2, 0.1, 0.1, 1.0); // material-ambient * light-ambient
  diffuse = aColor * vec4(0.8, 0.7, 0.8, 1.0); // material-diffuse * light-diffuse

  vColor = globalAmbient + ambient + NdotL * diffuse + specular;

  gl_Position = uMVPMatrix * aPosition;
}
