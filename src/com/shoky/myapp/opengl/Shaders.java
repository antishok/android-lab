package com.shoky.myapp.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

public class Shaders {
	public final static String CURRENT_SHADER = "phong";
	
	public static Map<String, String> shaderSources = new HashMap<String, String>();
	
    final static int POSITION_HANDLE = 0;
    final static int NORMAL_HANDLE = 1;
    final static int COLOR_HANDLE = 2;
    final static int TEXTURE_HANDLE = 3;
	
    public static class Program {
    	public int mProgramHandle;
    	protected Map<String, Integer> mUniformLocations;
    	
    	Program(int programHandle, Map<String, Integer> uniformLocations) {
    		mProgramHandle = programHandle;
    		mUniformLocations = uniformLocations;
    	}
    	
    	public void use() {
    		GLES20.glUseProgram(mProgramHandle);
    	}
    	
    	public int getUniformLocation(String uniform) {
    		if (mUniformLocations == null)
    			return -1;
    		
    		return mUniformLocations.get(uniform);
    	}
    	
    	public static Program makeProgram(int vertShaderHandle, int fragShaderHandle, boolean bindVertexAttrs, boolean hasTexCoords, String[] uniforms)
    	{
    		int programHandle = GLES20.glCreateProgram();
    	    if (programHandle == 0)
    	    	return null;
    	    
    	    GLES20.glAttachShader(programHandle, vertShaderHandle);
    	    Utils.checkGlError("glAttachShader");
    	    GLES20.glAttachShader(programHandle, fragShaderHandle);
    	    Utils.checkGlError("glAttachShader");

	        if (bindVertexAttrs) { // glBindAttribLocation must be called before glLinkProgram
	            GLES20.glBindAttribLocation(programHandle, POSITION_HANDLE, "aPosition");
		        GLES20.glBindAttribLocation(programHandle, NORMAL_HANDLE, "aNormal");
		        GLES20.glBindAttribLocation(programHandle, COLOR_HANDLE, "aColor");
		        if (hasTexCoords)
		        	GLES20.glBindAttribLocation(programHandle, TEXTURE_HANDLE, "aTexCoords");
		        Utils.checkGlError("glBindAttribLocation");
	        }
    	    
    	    GLES20.glLinkProgram(programHandle);
    	    int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e("Shaders", "Could not link program: ");
                Log.e("Shaders", GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                return null;
            }
            
	        
	        Map<String, Integer> uniformLocations = null;
	        
	        if (uniforms != null) {
	        	uniformLocations = new HashMap<String, Integer>(uniforms.length);
	    		for (String uniform: uniforms) {
	    			uniformLocations.put(uniform, GLES20.glGetUniformLocation(programHandle, uniform));
	    		}
	    		Utils.checkGlError("glGetUniformLocation");	    		
            }
	        
	        return new Program(programHandle, uniformLocations);	        
    	}
    	
    }
    
    public static Program makeProgram(String shader, String[] uniforms, boolean bindVertexAttrs, boolean hasTexCoords) {
	    int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, shader + ".vert");
	    int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, shader + ".frag");
	    
	    return Program.makeProgram(vertexShader, fragmentShader, bindVertexAttrs, hasTexCoords, uniforms);
    }
    
    
    public static Program makeLightingProgram(boolean withTexture) {
    	String shader = "phong".equals(CURRENT_SHADER) ? "phong" : "gouraud";
    	String[] uniforms;
    	if (withTexture) {
    		uniforms = new String[] {
            		"uLight.ecPos", "uLight.diffuse", "uLight.ambient", "uLight.specular", "uMVMatrix", "uMVPMatrix", "uNormalMatrix", "uTexture"};
    		shader = shader + "-texture";
    	}
    	else {
    		uniforms = new String[] {
            		"uLight.ecPos", "uLight.diffuse", "uLight.ambient", "uLight.specular", "uMVMatrix", "uMVPMatrix", "uNormalMatrix"};    		
    	}
    	
    	return makeProgram(shader, uniforms, true, withTexture);
    }
    
    public static Program makePointProgram() {
    	return makeProgram("light-point", new String[] {"uMVPMatrix"}, false, false);
    }

    public static int loadShader(int type, String shaderName){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER) or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        if (shader == 0)
        	return 0;

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderSources.get(shaderName));
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

	public static boolean loadAssets(Context context) 
	{
		final String[] fileNames = new String[] { 
				"gouraud.vert", "gouraud.frag", 
				"phong.vert", "phong.frag",
				"phong-texture.vert", "phong-texture.frag",
				"light-point.vert", "light-point.frag" };
		
		try {
		
			for (String fileName: fileNames) {
				InputStream is = context.getAssets().open(fileName);
				byte[] buffer = new byte[is.available()];
				is.read(buffer);
				is.close();
				shaderSources.put(fileName, new String(buffer, "UTF-8"));								
			}
		} catch (IOException e) { 
			e.printStackTrace();
			return false;
		}
		
		return true;		
	}
}
