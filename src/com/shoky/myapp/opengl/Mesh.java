package com.shoky.myapp.opengl;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class Mesh {
    protected FloatBuffer mVertexBuffer; // vec3
    protected FloatBuffer mNormalsBuffer; // vec3
    protected FloatBuffer mColorBuffer; // vec4
    protected ShortBuffer mDrawListBuffer;
    
    protected static final int COORDS_PER_VERTEX = 3;
    protected static final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    protected static final int colorStride = 4 * 4; // floatsPerColor * sizeof(float)
    
    protected final int mNumVertices;

    public Mesh(final float[] vertexCoords, final float[] normalCoords, final float[] colors, final short[] drawOrder) {
    	mVertexBuffer = Utils.allocFloatBuffer(vertexCoords);
    	mNormalsBuffer = Utils.allocFloatBuffer(normalCoords);
    	mColorBuffer = Utils.allocFloatBuffer(colors);
    	mDrawListBuffer = Utils.allocShortBuffer(drawOrder);    	
    	mNumVertices = drawOrder.length;
    }
    
    public void draw(Mx modelMatrix, Mx viewMatrix, Mx projMatrix, Light pointLight, int program) {
    	Mx mvMatrix = new Mx();
    	Mx mvpMatrix = new Mx();
    	
    	mvMatrix.setMultiply(viewMatrix, modelMatrix);
    	mvpMatrix.setMultiply(projMatrix, mvMatrix);
    	
    	Mx normalMatrix = new Mx(mvMatrix).invert().transpose();
    	
        GLES20.glUseProgram(program);

        // TODO: store the location handles on startup (also - bind them manually with glBindAttribLocation)
        // TODO: use interleaved vertex-attribute array
        // TODO: use VBOs
        
        int positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        int normalHandle = GLES20.glGetAttribLocation(program, "aNormal");
        int colorHandle = GLES20.glGetAttribLocation(program, "aColor");
        Utils.checkGlError("glGetAttribLocation");
        int lightEcPosHandle = GLES20.glGetUniformLocation(program, "uLight.ecPos");
        int lightDiffuseHandle = GLES20.glGetUniformLocation(program, "uLight.diffuse");
        int lightAmbientHandle = GLES20.glGetUniformLocation(program, "uLight.ambient");
        int lightSpecularHandle = GLES20.glGetUniformLocation(program, "uLight.specular");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");       
        int normalMxHandle = GLES20.glGetUniformLocation(program, "uNormalMatrix");
        Utils.checkGlError("glGetUniformLocation");
        
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);
        
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glVertexAttribPointer(normalHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, mNormalsBuffer);

        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, colorStride, mColorBuffer);
        
        GLES20.glUniform4fv(lightEcPosHandle, 1, viewMatrix.transformVector(pointLight.coords), 0);
        GLES20.glUniform4fv(lightDiffuseHandle, 1, pointLight.diffuse, 0);
        GLES20.glUniform4fv(lightAmbientHandle, 1, pointLight.ambient, 0);
        GLES20.glUniform4fv(lightSpecularHandle, 1, pointLight.specular, 0);
        
               
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix.mMatrix, 0);        
        GLES20.glUniformMatrix4fv(normalMxHandle, 1, false, normalMatrix.mMatrix, 0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uMVMatrix"), 1, false, mvMatrix.mMatrix, 0);

        Utils.checkGlError("glUniformMatrix4fv");

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumVertices,
                              GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        GLES20.glDisableVertexAttribArray(colorHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
    
    public static Mesh newCube() {
       return new Mesh(new float[] { 
       		-0.5f,  0.5f, 0.5f,   // top left
               -0.5f, -0.5f, 0.5f,   // bottom left
               0.5f, -0.5f, 0.5f,   // bottom right
               0.5f,  0.5f, 0.5f,
               
       		0.5f,  0.5f, -0.5f,
               0.5f,  -0.5f, -0.5f,
               -0.5f, -0.5f, -0.5f,
               -0.5f, 0.5f, -0.5f,
               
       		-0.5f,  0.5f, -0.5f,
               -0.5f, -0.5f, -0.5f,
               -0.5f, -0.5f, 0.5f,
       		-0.5f,  0.5f, 0.5f, 
               
               0.5f,  0.5f, 0.5f,
               0.5f, -0.5f, 0.5f,
               0.5f, -0.5f, -0.5f,
               0.5f,  0.5f, -0.5f,

       		-0.5f,  0.5f, -0.5f,
       		-0.5f,  0.5f, 0.5f,
               0.5f,  0.5f, 0.5f,
               0.5f,  0.5f, -0.5f,
       
               -0.5f, -0.5f, 0.5f,
               -0.5f, -0.5f, -0.5f,
               0.5f, -0.5f, -0.5f,
               0.5f, -0.5f, 0.5f                
       }, 
       new float[] {
       		0, 0,  1.0f,   0, 0,  1.0f,   0, 0,  1.0f,   0, 0,  1.0f, 
       		0, 0, -1.0f,   0, 0, -1.0f,   0, 0, -1.0f,   0, 0, -1.0f, 
       		-1.0f, 0, 0,   -1.0f, 0, 0,   -1.0f, 0, 0,   -1.0f, 0, 0, 
       		 1.0f, 0, 0,    1.0f, 0, 0,    1.0f, 0, 0,    1.0f, 0, 0,
       		 0, 1.0f, 0,    0, 1.0f, 0,    0, 1.0f, 0,    0, 1.0f, 0, 
       		0, -1.0f, 0,   0, -1.0f, 0,   0, -1.0f, 0,   0, -1.0f, 0        		
       }, 
       new float[] { 
       		0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,
       		0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,
       		0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,
       		0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,
       		0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,
       		0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f,   0.2f, 0.709803922f, 0.898039216f, 1.0f
       },
       new short[] { 
       		0, 1, 2, 0, 2, 3, 
       		4, 5, 6, 4, 6, 7, 
       		8, 9, 10, 8, 10, 11,
       		12, 13, 14, 12, 14, 15,
       		16,17,18,16,18,19,
       		20,21,22,20,22,23 });
       
    }
    
    private static void setCoords3(float x, float y, float z, float[] buf, int offset) {
    	buf[offset] = x; buf[offset+1] = y; buf[offset+2] = z;
    }
    
    private static void setCoords3(int x, int y, int z, short[] buf, int offset) {
    	buf[offset] = (short)x; buf[offset+1] = (short)y; buf[offset+2] = (short)z;
    }
    
    private static void setCoords4(float x, float y, float z, float w, float[] buf, int offset) {
    	buf[offset] = x; buf[offset+1] = y; buf[offset+2] = z; buf[offset+3] = w;
    }

    public static Mesh newSphere(float radius, int longLines, int latLines) {
        int stackNum, sliceNum;		// current stack number and slice number
    	float x, z;	// x,z coordinates of current long-line
    	float y, r;	// y-coordinate and radius of current lat-line. radius is of current circle in x-z plane
    	float ny, nr;	// y-coordinate and radius of next lat-line
    	float xzParam, yrParam;	// params for cos/sin in x-z plane and y-r plane
    	
    	// sin/cos tables for x-z plane, to avoid recomputation for each lat-line:
    	float xzSinTable[] = new float[longLines];
    	float xzCosTable[] = new float[longLines];
    	for (sliceNum = 0; sliceNum < longLines; sliceNum++) {
    		xzParam = ((float)sliceNum / longLines) * 2.0f * (float)Math.PI;
    		xzSinTable[sliceNum] = (float)Math.sin( xzParam );
    		xzCosTable[sliceNum] = (float)Math.cos( xzParam );
    	}
    	
    	final int numCoords = 2 + 2*longLines + 2*longLines*(latLines-1);
    	float vertexCoords[] = new float[numCoords * 3];
    	float normalCoords[] = new float[numCoords * 3];
    	float colorCoords[] = new float[numCoords * 4];
    	short drawOrder[] = new short[6 * latLines * longLines];
    	float[] color = new float[] {0.8f, 0.3f, 0.5f, 1.0f};
    	
    	int offset = 0, colorOffset = 0;
    	
    	setCoords3(0, 1,      0, normalCoords, offset);
    	setCoords3(0, radius, 0, vertexCoords, offset);
    	setCoords4(color[0],color[1],color[2],color[3], colorCoords, colorOffset);
    	offset += 3; colorOffset += 4;
    	
		yrParam = (1.0f / (latLines+1)) * (float)Math.PI;
		y = ny = (float)Math.cos( yrParam );
		r = nr = radius * (float)Math.sin( yrParam );

		for (sliceNum = 0; sliceNum < longLines; sliceNum++) 
		{			
			x = xzCosTable[sliceNum];
			z = xzSinTable[sliceNum];

	    	setCoords3(x,   y,        z,   normalCoords, offset);
	    	setCoords3(x*r, y*radius, z*r, vertexCoords, offset);
	    	setCoords4(color[0],color[1],color[2],color[3], colorCoords, colorOffset);
	    	offset += 3; colorOffset += 4;	    	
		}
		
		// loop from 1st lat-line to 1-before-last lat-line
		for (stackNum = 1; stackNum < latLines; stackNum++) 
		{
			y = ny;
			r = nr;

			yrParam = ((float)(stackNum+1) / (latLines+1)) * (float)Math.PI;
			ny = (float)Math.cos( yrParam );
			nr = radius * (float)Math.sin( yrParam );

			for (sliceNum = 0; sliceNum < longLines; sliceNum++) 
			{
				x = xzCosTable[sliceNum];
				z = xzSinTable[sliceNum];

		    	setCoords3(x,   y,        z,   normalCoords, offset);
		    	setCoords3(x*r, y*radius, z*r, vertexCoords, offset);
		    	setCoords4(color[0],color[1],color[2],color[3], colorCoords, colorOffset);
		    	offset += 3; colorOffset += 4;	    	
		    	setCoords3(x,    ny,        z,   normalCoords, offset);
		    	setCoords3(x*nr, ny*radius, z*nr, vertexCoords, offset);
		    	setCoords4(color[0],color[1],color[2],color[3], colorCoords, colorOffset);
		    	offset += 3; colorOffset += 4;
			}
		}
		
		
    	setCoords3(0, -1,      0, normalCoords, offset);
    	setCoords3(0, -radius, 0, vertexCoords, offset);
    	setCoords4(color[0],color[1],color[2],color[3], colorCoords, colorOffset);
    	offset += 3; colorOffset += 4;
    	
		y = ny;
		r = nr;

		for (sliceNum = 0; sliceNum < longLines; sliceNum++) 
		{
			x = xzCosTable[sliceNum];
			z = xzSinTable[sliceNum];

	    	setCoords3(x,   y,        z,   normalCoords, offset);
	    	setCoords3(x*r, y*radius, z*r, vertexCoords, offset);
	    	setCoords4(color[0],color[1],color[2],color[3], colorCoords, colorOffset);
	    	offset += 3; colorOffset += 4;	    	
		}


		
		int triCount = 0;
		
		for (sliceNum = 0; sliceNum < longLines-1; sliceNum++)
		{
			setCoords3(0,sliceNum+2,sliceNum+1,drawOrder,triCount*3); triCount++;
		}
		
		
		setCoords3(0,1,sliceNum+1,drawOrder,triCount*3); triCount++;				
		
		int drawOffset = longLines + 1;
		
		for (stackNum = 1; stackNum < latLines; stackNum++) 
		{
			for (sliceNum = 0; sliceNum < longLines-1; sliceNum++)
			{
				setCoords3(drawOffset,drawOffset+3,drawOffset+1,drawOrder,triCount*3); triCount++;				
				setCoords3(drawOffset,drawOffset+2,drawOffset+3,drawOrder,triCount*3); triCount++;
				drawOffset += 2;
			}
			setCoords3(drawOffset,drawOffset-(2*longLines-2)+1,drawOffset+1,drawOrder,triCount*3); triCount++;				
			setCoords3(drawOffset,drawOffset-(2*longLines-2),drawOffset-(2*longLines-2)+1,drawOrder,triCount*3); triCount++;
			drawOffset += 2;
		}
		
		for (sliceNum = 0; sliceNum < longLines-1; sliceNum++)
		{
			setCoords3(drawOffset,drawOffset+sliceNum+1,drawOffset+sliceNum+2,drawOrder,triCount*3);
			triCount++;
		}
		
		setCoords3(drawOffset,drawOffset+sliceNum+1,drawOffset+1,drawOrder,triCount*3); triCount++;

		return new Mesh(vertexCoords, normalCoords, colorCoords, drawOrder);
    }
    
    public static Mesh newTriangle() {
    	return new Mesh(new float[] { // in counterclockwise order:
    	         0.0f,  0.622008459f, 0.0f,   // top
     	         -0.5f, -0.311004243f, 0.0f,   // bottom left
     	          0.5f, -0.311004243f, 0.0f    // bottom right
      	},
      	new float[] {0, 0, 1.0f, 0, 0, 1.0f, 0, 0, 1.0f},
      	new float[] { 
      		0.63671875f, 0.76953125f, 0.22265625f, 1.0f, 
      		0.76953125f, 0.22265625f, 0.63671875f, 1.0f, 
      		0.76953125f, 0.63671875f, 0.22265625f, 1.0f },
      	new short[] {0, 1, 2});                      
    }
}