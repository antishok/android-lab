package com.shoky.myapp.opengl;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import com.shoky.myapp.opengl.Shaders.Program;
import android.opengl.GLES20;


public class Mesh {
    protected int mVertexBufferHandle;
    protected int mIndexBufferHandle;
    
    protected static final int POSITION_COORDS_PER_VERTEX = 3;
    protected static final int NORMAL_COORDS_PER_VERTEX = 3;
    protected static final int COLOR_COORDS_PER_VERTEX = 4;
    protected static final int vertexStride = (POSITION_COORDS_PER_VERTEX + NORMAL_COORDS_PER_VERTEX + COLOR_COORDS_PER_VERTEX) * 4; // 4 bytes per vertex
        
    protected final int mNumVertices;

    public Mesh(final float[] positionCoords, final float[] normalCoords, final float[] colors, final short[] indexList) 
    {
    	float[] interleaved = Utils.interleave(  // interleave the position, normal, and color arrays into one array
    			new float[][] {positionCoords, normalCoords, colors}, 
    			new short[]   {POSITION_COORDS_PER_VERTEX, NORMAL_COORDS_PER_VERTEX, COLOR_COORDS_PER_VERTEX});
    	FloatBuffer vertexBuffer = Utils.allocFloatBuffer(interleaved);
    	ShortBuffer indexBuffer = Utils.allocShortBuffer(indexList);
    	
    	int[] vbo = new int[2]; 
    	GLES20.glGenBuffers(2, vbo, 0);
    	mVertexBufferHandle = vbo[0];
    	mIndexBufferHandle = vbo[1];
    	
    	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBufferHandle);
    	GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
    	
    	GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferHandle);
    	GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 2, indexBuffer, GLES20.GL_STATIC_DRAW);
    	
    	GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    	GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    	
    	mNumVertices = indexList.length;
    }
    
    public void draw(Mx modelMatrix, Mx viewMatrix, Mx projMatrix, Light pointLight, Program program) {
    	Mx mvMatrix = new Mx();
    	Mx mvpMatrix = new Mx();
    	
    	mvMatrix.setMultiply(viewMatrix, modelMatrix);
    	mvpMatrix.setMultiply(projMatrix, mvMatrix);
    	
    	Mx normalMatrix = new Mx(mvMatrix).invert().transpose();
    	
        program.use();
        
        GLES20.glUniform4fv(program.getUniformLocation("uLight.ecPos"), 1, viewMatrix.transformVector(pointLight.coords), 0);
        GLES20.glUniform4fv(program.getUniformLocation("uLight.diffuse"), 1, pointLight.diffuse, 0);
        GLES20.glUniform4fv(program.getUniformLocation("uLight.ambient"), 1, pointLight.ambient, 0);
        GLES20.glUniform4fv(program.getUniformLocation("uLight.specular"), 1, pointLight.specular, 0);
        Utils.checkGlError("glUniform4fv");
                
        GLES20.glUniformMatrix4fv(program.getUniformLocation("uMVMatrix"), 1, false, mvMatrix.mMatrix, 0);
        GLES20.glUniformMatrix4fv(program.getUniformLocation("uMVPMatrix"), 1, false, mvpMatrix.mMatrix, 0);        
        GLES20.glUniformMatrix4fv(program.getUniformLocation("uNormalMatrix"), 1, false, normalMatrix.mMatrix, 0);
        Utils.checkGlError("glUniformMatrix4fv");

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexBufferHandle);
        
        GLES20.glEnableVertexAttribArray(Shaders.POSITION_HANDLE);
        GLES20.glEnableVertexAttribArray(Shaders.NORMAL_HANDLE);
        GLES20.glEnableVertexAttribArray(Shaders.COLOR_HANDLE);
        GLES20.glVertexAttribPointer(Shaders.POSITION_HANDLE, POSITION_COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, 0);
        GLES20.glVertexAttribPointer(Shaders.NORMAL_HANDLE, NORMAL_COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, 4 * POSITION_COORDS_PER_VERTEX);
        GLES20.glVertexAttribPointer(Shaders.COLOR_HANDLE, COLOR_COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, 4 * (POSITION_COORDS_PER_VERTEX + NORMAL_COORDS_PER_VERTEX));

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndexBufferHandle);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumVertices, GLES20.GL_UNSIGNED_SHORT, 0);        
        Utils.checkGlError("glDrawElements");
        
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glDisableVertexAttribArray(Shaders.COLOR_HANDLE);
        GLES20.glDisableVertexAttribArray(Shaders.NORMAL_HANDLE);
        GLES20.glDisableVertexAttribArray(Shaders.POSITION_HANDLE);
    }
    
    public void releaseBuffers() {  // TODO: call this from somewhere
    	GLES20.glDeleteBuffers(2, new int[] {mVertexBufferHandle, mIndexBufferHandle}, 0);
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