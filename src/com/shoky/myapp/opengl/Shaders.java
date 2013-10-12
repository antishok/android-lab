package com.shoky.myapp.opengl;

import android.opengl.GLES20;
import android.util.Log;

public class Shaders {
	public final static String CURRENT_SHADER = "phong";
	
    public final static String gouraudVertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "uniform mat4 uMVMatrix;" +
        "uniform mat4 uNormalMatrix;" + // transpose of inverse of mv matrix (needed if mv matrix contains non-uniform scales)
        "uniform vec4 uEcLightPos;" +

        "attribute vec4 aPosition;" +
        "attribute vec3 aNormal;" +
        "attribute vec4 aColor;" +
        "varying vec4 vColor;" +
                
        "void main() {" +
        "  vec4 globalAmbient, ambient, diffuse, specular = vec4(0);" +
        "  vec3 ecPosition = vec3(uMVMatrix * aPosition);" +
        "  vec3 ecNormal = normalize(vec3(uNormalMatrix * vec4(aNormal, 0.0)));" + // (can multiply by either mvMatrix or normalMatrix. normalMatrix is better when mvMatrix contains non-uniform scales)
        
        "  vec3 lightDir = normalize(vec3(uEcLightPos) - ecPosition);" + // L = vertex-to-lightpos vector, in eye-coordinates 
        "  float NdotL = max(dot(ecNormal, lightDir), 0.0);" +
        
        "  if (NdotL > 0.0) { " +
        "	 vec3 eyeDir = -ecPosition;" + // vertex-to-eye vector, in eye-coordinates
        "    vec3 lightHalfVector = normalize(eyeDir + lightDir);" +
    	"    float NdotHV = max(dot(ecNormal, lightHalfVector), 0.0);" +
    	"    specular = vec4(0.2,0.3,0.5,1.0) * vec4(0.8,0.8,0.8,1.0) * pow(NdotHV, 16.0);" + // gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV,gl_FrontMaterial.shininess);" +
    	"  }" +
    		
        "  globalAmbient = aColor * vec4(0.2, 0.2, 0.2, 1.0);" + // material-ambient * global-ambient
        "  ambient = aColor * vec4(0.2, 0.1, 0.1, 1.0);" + // material-ambient * light-ambient
		"  diffuse = aColor * vec4(0.8, 0.7, 0.8, 1.0);" + // material-diffuse * light-diffuse
		
		"  vColor = globalAmbient + ambient + NdotL * diffuse + specular;" +

        "  gl_Position = uMVPMatrix * aPosition;" +
        "}";
    
    public final static String gouraudFragmentShaderCode =
        "precision mediump float;" +
        "varying vec4 vColor;" +
        "void main() {" +
        //"  if (gl_FrontFacing) {" +
        "    gl_FragColor = vColor;" +
        //"  }" +
        "}";
    
    
    
    public final static String phongVertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "uniform mat4 uMVMatrix;" +
            "uniform mat4 uNormalMatrix;" + // transpose of inverse of mv matrix
            "uniform vec4 uEcLightPos;" +
            "attribute vec4 aPosition;" +
            "attribute vec3 aNormal;" +
            "attribute vec4 aColor;" +
            "varying vec4 vDiffuse, vAmbient;" +
            "varying vec3 vNormal, vHalfVector;" +
            "varying vec3 vLightDir;" +
            "void main() {" +
            "  " + 
            "  vec3 ecPosition = vec3(uMVMatrix * aPosition);" + // convert vertex position to eye-coordinates
            "  vLightDir = normalize(vec3(uEcLightPos) - ecPosition);" + // vector from vertex to lightPos
            "  vNormal = normalize(vec3(uNormalMatrix * vec4(aNormal, 0.0)));" +  // (can multiply by either mvMatrix or normalMatrix. normalMatrix is better when mvMatrix contains non-uniform scales)
            "  vec3 eyeDir = -ecPosition;" + // vector from vertex to eye (in eye coordinates, so it's just negative of vertexPos)
            "  vHalfVector = normalize(eyeDir + vLightDir);" + // half-vector between eye-vector and light-vector, pass on to fragment shader
            
            "  vec4 globalAmbient = aColor * vec4(0.2, 0.2, 0.2, 1.0);" + // material-ambient * global-ambient
            "  vAmbient = globalAmbient + aColor * vec4(0.2, 0.1, 0.1, 1.0);" + // global + material-ambient * light-ambient
    		"  vDiffuse =  aColor * vec4(0.8, 0.7, 0.8, 1.0);" + // material-diffuse * light-diffuse
    		
            "  gl_Position = uMVPMatrix * aPosition;" +
            "}";
    
    public final static String phongFragmentShaderCode =
    		"precision mediump float;" +
    		//"uniform highp vec4 uEcLightPos;" +
            "varying vec4 vDiffuse, vAmbient;" +
            "varying vec3 vNormal, vHalfVector;" +
            "varying vec3 vLightDir;" +
            "void main() {" +
            //"  if (!gl_FrontFacing) { return; }" +
            "  vec3 lightDir = normalize(vLightDir);" +
            "  vec3 n = normalize(vNormal);" +
            "  vec4 color = vAmbient;" +
            "  float NdotL, NdotHV;" +
            
            "  NdotL = max(dot(n, lightDir), 0.0);" +
            "  if (NdotL > 0.0) {" +
            "    color += vDiffuse * NdotL;" +
            "    vec3 halfV = normalize(vHalfVector);" +
            "    NdotHV = max(dot(n,halfV), 0.0);" +
            "    color += vec4(0.2,0.3,0.5,1.0) * vec4(0.8,0.8,0.8,1.0) * pow(NdotHV, 16.0);" + //gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV, gl_FrontMaterial.shininess);" +
            "  }" +
            
            "  gl_FragColor = color;" +
            "}";
    
    
    
    public final static String pointVertexShader =
        	"uniform mat4 uMVPMatrix;"		
    	  + "attribute vec4 aLightPos;		"
          + "void main()"
          + "{                              "
          + "   gl_Position = uMVPMatrix * aLightPos;"
          + "   gl_PointSize = 5.0;         "
          + "}                              ";        
    public final static String pointFragmentShader = 
        	"precision mediump float;       "
          + "void main()                    "
          + "{                              "
          + "   gl_FragColor = vec4(1.0,    " 
          + "   1.0, 1.0, 1.0);             "
          + "}                              ";


	
    public static int makeProgram(int vertShaderHandle, int fragShaderHandle) {    		    
	    int program = GLES20.glCreateProgram();
	    if (program == 0)
	    	return 0;
	    
	    GLES20.glAttachShader(program, vertShaderHandle);
	    Utils.checkGlError("glAttachShader");
	    GLES20.glAttachShader(program, fragShaderHandle);
	    Utils.checkGlError("glAttachShader");
	    
	    GLES20.glLinkProgram(program);
	    int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("Shaders", "Could not link program: ");
            Log.e("Shaders", GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            return 0;
        }
	    return program;
    }
    
    public static int makeLightingProgram() {
	    int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
	            "phong".equals(CURRENT_SHADER) ? Shaders.phongVertexShaderCode : Shaders.gouraudVertexShaderCode);
	    int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
	    		"phong".equals(CURRENT_SHADER) ? Shaders.phongFragmentShaderCode : Shaders.gouraudFragmentShaderCode);
	    
    	return makeProgram(vertexShader, fragmentShader);
    }
    
    public static int makePointProgram() {    	
	    // prepare shaders and OpenGL program
	    int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, Shaders.pointVertexShader);
	    int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, Shaders.pointFragmentShader);
	    return makeProgram(vertexShader, fragmentShader);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER) or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        if (shader == 0)
        	return 0;

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        int[] compiledStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiledStatus, 0);
        if (compiledStatus[0] != GLES20.GL_TRUE) {
        	Log.e("Shaders", "Could not compile shader " + type + ":");
            Log.e("Shaders", GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

}