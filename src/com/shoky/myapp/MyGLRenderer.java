package com.shoky.myapp;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;

import com.shoky.myapp.opengl.Light;
import com.shoky.myapp.opengl.Mesh;
import com.shoky.myapp.opengl.Mx;
import com.shoky.myapp.opengl.Shaders;
import com.shoky.myapp.opengl.Utils;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    private Mesh mTriangle;
    private Mesh mCube;
    private Mesh mSphere;
    private int mProgram;
    private int mPointProgram;

    private Light mPointLight;
    private Light mDirectionalLight;

    private Mx mViewMatrix = new Mx();
    private Mx mProjMatrix = new Mx();
    private Mx mModelMatrix = new Mx();
    
    // Declare as volatile because we are updating it from another thread
    public volatile float mTouchInputX;
    public volatile float mTouchInputY;
    
    public MyGLRenderer() {
    	mPointLight = new Light(Light.Type.POSITIONAL, new float[] { 0.0f, 0.0f, -0.1f });
    	mDirectionalLight = new Light(Light.Type.DIRECTIONAL, new float[] { 0.0f, 0.0f, -1.0f });
        mTriangle = Mesh.newTriangle();
        mSphere = Mesh.newSphere(1f,20,20);
        mCube = Mesh.newCube();
        
        // Set the camera position (View matrix)
        mViewMatrix.setLookAt(0, 0, 3.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }
        
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) 
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //GLES20.glEnable(GLES20.GL_CULL_FACE);
        
        mProgram = Shaders.makeLightingProgram();
        mPointProgram = Shaders.makePointProgram();
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

        long time = SystemClock.uptimeMillis() % 8000L;
      	float angle = (360.0f / 8000) * ((int) time);
      	
      	mPointLight.coords[0] = 0.5f*(float)Math.sin(angle * 2 * (float)Math.PI / 360);
      	mPointLight.coords[1] = 0.5f*(float)Math.sin(-angle * 4 * (float)Math.PI / 360);
      	
      	mModelMatrix
      		.setTranslate(-0.5f, 0, -1.5f)
      		.rotate(angle, 0.4f, 0, 0.7f);
      	mCube.draw(mModelMatrix, mViewMatrix, mProjMatrix, mPointLight, mProgram);
    
      	mModelMatrix.setTranslate(0.8f,  0, -1.0f).scaleUniform(0.6f);
        mSphere.draw(mModelMatrix, mViewMatrix, mProjMatrix,mPointLight, mProgram);

        mModelMatrix
        	.setTranslate(0, 1.0f-mTouchInputY*0.005f, -1.0f)
        	.rotate(20 + mTouchInputY*2f, 0, 0, 1.0f);
        mTriangle.draw(mModelMatrix, mViewMatrix, mProjMatrix, mPointLight, mProgram);
        
        drawPointLight();
    }

    private void drawPointLight() {
    	// draw a little dot where the positional light is
		mModelMatrix.setTranslate(mPointLight.coords[0], mPointLight.coords[1], mPointLight.coords[2]);
		
    	GLES20.glUseProgram(mPointProgram);
    	
    	Mx mvpMatrix = new Mx();
    	Mx.scratch.setMultiply(mViewMatrix, mModelMatrix);
    	mvpMatrix.setMultiply(mProjMatrix, Mx.scratch);
    	
    	int lightPosHandle = GLES20.glGetAttribLocation(mPointProgram, "aLightPos");
    	Utils.checkGlError("glGetAttribLocation");
    	
    	GLES20.glVertexAttrib4f(lightPosHandle, 0, 0, 0, 1.0f);  	
    	
        int mvpMatrixHandle = GLES20.glGetUniformLocation(mPointProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix.mMatrix, 0);
        
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}

	@Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        mProjMatrix.setFrustum(-ratio, ratio, -1, 1, 2.5f, 15);
    }
}

