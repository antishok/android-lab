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
	
	
    public static float[] interleave(final float[][] allCoords, final short[] lengths) {
    	int numSets = allCoords.length;
    	int totalSize = 0;
    	for (float[] coords: allCoords) {
    		totalSize += coords.length;
    	}
    	
    	float[] result = new float[totalSize];
    	int offset = 0, vertexNum, currSetNum;
    	int numVertices = allCoords[0].length / lengths[0];
    	for (vertexNum = 0; vertexNum < numVertices; vertexNum++) {    		
    		for (currSetNum = 0; currSetNum < numSets; currSetNum++) {
    			float[] coords = allCoords[currSetNum];
    			short curCoordsPerVertex = lengths[currSetNum];
    			System.arraycopy(coords, vertexNum * curCoordsPerVertex, result, offset, curCoordsPerVertex);
    			offset += curCoordsPerVertex;
    		}    		
    	}
    	return result;
    }


}
