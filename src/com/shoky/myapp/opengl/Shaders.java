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
	            "phong".equals(CURRENT_SHADER) ? "phong.vert" : "gouraud.vert");
	    int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
	    		"phong".equals(CURRENT_SHADER) ? "phong.frag" : "gouraud.frag");
	    
    	return makeProgram(vertexShader, fragmentShader);
    }
    
    public static int makePointProgram() {    	
	    int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, "light-point.vert");
	    int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, "light-point.frag");
	    return makeProgram(vertexShader, fragmentShader);
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
