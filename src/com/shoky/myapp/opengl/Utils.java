package com.shoky.myapp.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.opengl.GLES20;
import android.opengl.GLUtils;
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
    		if (coords != null)
    			totalSize += coords.length;
    	}
    	
    	float[] result = new float[totalSize];
    	int offset = 0, vertexNum, currSetNum;
    	int numVertices = allCoords[0].length / lengths[0];
    	for (vertexNum = 0; vertexNum < numVertices; vertexNum++) {    		
    		for (currSetNum = 0; currSetNum < numSets; currSetNum++) {
    			float[] coords = allCoords[currSetNum];
    			if (coords == null)
    				continue;
    			short curCoordsPerVertex = lengths[currSetNum];
    			System.arraycopy(coords, vertexNum * curCoordsPerVertex, result, offset, curCoordsPerVertex);
    			offset += curCoordsPerVertex;
    		}
    	}
    	return result;
    }

	public static int loadTexture(final Context context, final int resourceId)
	{
	    final int[] textureHandle = new int[1];
	
	    GLES20.glGenTextures(1, textureHandle, 0);
	
	    if (textureHandle[0] != 0)
	    {
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inScaled = false;   // No pre-scaling
	
	        // Read in the resource
	        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
	
	        // Bind to the texture in OpenGL
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
	
	        // Set filtering
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
	
	        // Load the bitmap into the bound texture.
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	
	        // Recycle the bitmap, since its data has been loaded into OpenGL.
	        bitmap.recycle();
	        
	        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
	    }
	
	    if (textureHandle[0] == 0)
	    {
	        throw new RuntimeException("Error loading texture.");
	    }
	
	    return textureHandle[0];
	}


}
