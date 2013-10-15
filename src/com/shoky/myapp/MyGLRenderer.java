package com.shoky.myapp;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.shoky.myapp.opengl.Light;
import com.shoky.myapp.opengl.Mesh;
import com.shoky.myapp.opengl.Mx;
import com.shoky.myapp.opengl.Shaders;
import com.shoky.myapp.opengl.Shaders.Program;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Mesh mTriangle;
    private Mesh mCube;
    private Mesh mSphere;
    
    private Light mPointLight;
    
    private Program mProgram;
    private Program mPointProgram;

    private Mx mViewMatrix = new Mx();
    private Mx mProjMatrix = new Mx();
    private Mx mModelMatrix = new Mx();
    
    // Declare as volatile because we are updating it from another thread
    public volatile float mTouchInputX;
    public volatile float mTouchInputY;
    
    public MyGLRenderer(Context context) {
    	mPointLight = new Light(Light.Type.POSITIONAL, new float[] { 0.0f, 0.0f, -0.2f }, 
    			new float[] {0.2f, 0.1f, 0.1f, 1.0f}, // ambient
    			new float[] {0.8f, 0.7f, 0.8f, 1.0f}, // diffuse
    			new float[] {0.8f, 0.8f, 0.8f, 1.0f}); // specular

    	Shaders.loadAssets( context );

        // Set the initial camera position
        mViewMatrix.setLookAt(0, 0, 3.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }
        
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) 
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        mProgram = Shaders.makeLightingProgram();
        mPointProgram = Shaders.makePointProgram();
        
    	mTriangle = Mesh.newTriangle();
        mSphere = Mesh.newSphere(1f,30,30);
        mCube = Mesh.newCube();
    }

    @Override
    public void onDrawFrame(GL10 unused) 
    {
    	mViewMatrix
    		.setTranslate(0, 0, -4.5f)
    		.rotate(mTouchInputX,0,1,0)
    		.rotate(mTouchInputY,1,0,0)
    		.translate(0, 0, 1.5f); // tmp

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        long time = SystemClock.uptimeMillis() % 8000L;
      	float angle = (360.0f / 8000) * ((int) time);
      	
      	mPointLight.coords[0] = 0.5f*(float)Math.sin(angle * 2 * (float)Math.PI / 360);
      	mPointLight.coords[1] = 0.5f*(float)Math.sin(-angle * 4 * (float)Math.PI / 360);
      	
      	      	
      	mModelMatrix
      		.setTranslate(-0.5f, 0, -1.5f)
      		.rotate(angle, 0.4f, 0, 0.7f);
      	mCube.draw(mModelMatrix, mViewMatrix, mProjMatrix, mPointLight, mProgram);
    
      	mModelMatrix
      		.setTranslate(0.8f,  0, -1.0f)
      		.scaleUniform(0.6f);
        mSphere.draw(mModelMatrix, mViewMatrix, mProjMatrix,mPointLight, mProgram);

        GLES20.glDisable(GLES20.GL_CULL_FACE); // to see back face of triangle
        
        mModelMatrix
        	.setTranslate(0, 1.0f-mTouchInputY*0.005f, -1.0f)
        	.rotate(20 + mTouchInputY*2f, 0, 0, 1.0f);
        mTriangle.draw(mModelMatrix, mViewMatrix, mProjMatrix, mPointLight, mProgram);
        
        drawPointLight(mPointLight);
    }

    private void drawPointLight(Light light) {
    	// draw a little dot where the positional light is
		mModelMatrix.setTranslate(light.coords[0], light.coords[1], light.coords[2]);
		
    	mPointProgram.use();
    	
    	Mx mvpMatrix = new Mx();
    	Mx.scratch.setMultiply(mViewMatrix, mModelMatrix);
    	mvpMatrix.setMultiply(mProjMatrix, Mx.scratch);
    	        
        GLES20.glUniformMatrix4fv(mPointProgram.getUniformLocation("uMVPMatrix"), 1, false, mvpMatrix.mMatrix, 0);
        
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}

	@Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        mProjMatrix.setFrustum(-ratio, ratio, -1, 1, 2.5f, 15);
    }
}

