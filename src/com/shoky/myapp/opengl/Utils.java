package com.shoky.myapp.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.util.Log;

public class Utils {

	public static void checkGlError(String glOperation) {
	    int error;
	    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
	        Log.e("Utils", glOperation + ": glError " + error);
	        throw new RuntimeException(glOperation + ": glError " + error);
	    }
	}

	public static FloatBuffer allocFloatBuffer(float[] coords) {
		// initialize vertex byte buffer for shape coordinates
	    return (FloatBuffer)ByteBuffer.allocateDirect(coords.length * 4)  // (# of coordinate values * 4 bytes per float)
	    		.order(ByteOrder.nativeOrder())
	    		.asFloatBuffer()
	    		.put(coords)
	    		.position(0);
	}

	public static ShortBuffer allocShortBuffer(short drawOrder[]) {
	    return (ShortBuffer)ByteBuffer.allocateDirect(drawOrder.length * 2) // (# of coordinate values * 2 bytes per short)
	    		.order(ByteOrder.nativeOrder())
	    		.asShortBuffer()
	    		.put(drawOrder)
	    		.position(0);
	
	}

}
