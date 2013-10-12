package com.shoky.myapp;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;

import com.shoky.myapp.opengl.Light;
import com.shoky.myapp.opengl.Mesh;
import com.shoky.myapp.opengl.Mx;
import com.shoky.myapp.opengl.Shaders;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    private Mesh mTriangle;
    private Mesh mCube;
    private Mesh mSphere;
    private int mProgram;
    private int mPointProgram;

    private Light mLight;

    private Mx mViewMatrix = new Mx();
    private Mx mProjMatrix = new Mx();
    private Mx mModelMatrix = new Mx();
    
    public static Mx tmpMx = new Mx();

    // Declare as volatile because we are updating it from another thread
    public volatile float mTouchInputX;
    public volatile float mTouchInputY;
    
    public MyGLRenderer() {
    	mLight = new Light(Light.Type.POSITIONAL, new float[] { 0.0f, 0.0f, 0.0f });
        mTriangle = Mesh.newTriangle();
        mSphere = Mesh.newSphere(1f,60,60);
        mCube = Mesh.newCube();
        
        // Set the camera position (View matrix)
        mViewMatrix.setLookAt(0, 0, 3.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }
        
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) 
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        
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
      	
      	mLight.coords[0] = 0.5f*(float)Math.sin(angle * 2 * (float)Math.PI / 360);
      	mLight.coords[1] = 0.5f*(float)Math.sin(-angle * 4 * (float)Math.PI / 360);
      	
      	mModelMatrix
      		.setTranslate(-0.2f, 0, -1.5f)
      		.rotate(angle, 0.4f, 0, 0.7f);
      	mCube.draw(mModelMatrix, mViewMatrix, mProjMatrix, mLight, mProgram);
    
      	mModelMatrix.setTranslate(1.0f,  0, -1.0f).scaleUniform(0.5f);
        mSphere.draw(mModelMatrix, mViewMatrix, mProjMatrix,mLight, mProgram);

        mModelMatrix
        	.setTranslate(0, 0.8f-mTouchInputY*0.02f, -1.0f)
        	.rotate(20 + mTouchInputY, 0, 0, 1.0f);
        mTriangle.draw(mModelMatrix, mViewMatrix, mProjMatrix, mLight, mProgram);
        
        drawPointLight();
    }

    private void drawPointLight() {
		mModelMatrix.setTranslate(mLight.coords[0], mLight.coords[1], mLight.coords[2]);
		
    	GLES20.glUseProgram(mPointProgram);
    	
    	Mx mvpMatrix = new Mx();
    	Mx.scratch.setMultiply(mViewMatrix, mModelMatrix);
    	mvpMatrix.setMultiply(mProjMatrix, Mx.scratch);    	
    	
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
    
    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}

