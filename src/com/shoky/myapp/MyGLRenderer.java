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

    private Light mLight;

    private Mx mViewMatrix = new Mx();
    private Mx mProjMatrix = new Mx();
    private Mx mModelMatrix = new Mx();

    // Declare as volatile because we are updating it from another thread
    public volatile float mTouchInput;
    
    public MyGLRenderer() {
    	mLight = new Light(Light.Type.POSITIONAL, new float[] { 0.0f, 0.0f, 2.0f });
        mTriangle = Mesh.newTriangle();
        mSphere = Mesh.newSphere(1f,60,60);
        mCube = Mesh.newCube();
        
        // Set the camera position (View matrix)
        mViewMatrix.setLookAt(0, 0, 4.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }
        
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) 
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        
        mProgram = Shaders.makeProgram();
    }

    @Override
    public void onDrawFrame(GL10 unused) 
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        long time = SystemClock.uptimeMillis() % 4000L;
      	float angle = 0.090f * ((int) time);
      	
      	mLight.coords[0] = 4*(float)Math.sin(angle * 2 * (float)Math.PI / 360);
      	mLight.coords[1] = 4*(float)Math.sin(-angle * 4 * (float)Math.PI / 360);
      	
      	mModelMatrix
      		.setTranslate(-1.7f, -2.8f, -6)
      		.rotate(angle, 0.4f, 0, 0.7f);
      	mCube.draw(mModelMatrix, mViewMatrix, mProjMatrix, mLight, mProgram);
    
      	mModelMatrix.setTranslate(0,  0, -1.0f);
        mSphere.draw(mModelMatrix, mViewMatrix, mProjMatrix,mLight, mProgram);

        mModelMatrix
        	.setTranslate(0, 0.8f-mTouchInput*0.02f, -0.5f)
        	.rotate(20 + mTouchInput, 0, 0, 1.0f);
        mTriangle.draw(mModelMatrix, mViewMatrix, mProjMatrix, mLight, mProgram);
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

