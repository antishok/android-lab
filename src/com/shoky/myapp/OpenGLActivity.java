package com.shoky.myapp;



import com.shoky.myapp.MyGLSurfaceView.TouchDeltaListener;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

class MyGLSurfaceView extends GLSurfaceView {
	private TouchDeltaListener touchDeltaListener;
	private float mPreviousX;
	private float mPreviousY;
	
	public MyGLSurfaceView(Context context, MyGLRenderer renderer) {
		super(context);
		setEGLContextClientVersion(2);
		setRenderer(renderer);
	}
	
	public interface TouchDeltaListener {
		void onTouchDelta(float dx, float dy);
	}
	
	public void setTouchDeltaListener(TouchDeltaListener listener) {
		touchDeltaListener = listener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
	    float x = e.getX(), y = e.getY();

	    if (touchDeltaListener != null) {
		    switch (e.getAction()) {
		        case MotionEvent.ACTION_MOVE:
		            float dx = x - mPreviousX;
		            float dy = y - mPreviousY;
	
		            touchDeltaListener.onTouchDelta(dx, dy);
		            //requestRender();
		    }
	    }

	    mPreviousX = x;
	    mPreviousY = y;
	    return true;
	}

}

public class OpenGLActivity extends Activity implements TouchDeltaListener {
	
	private MyGLSurfaceView mGLView;
	private MyGLRenderer mRenderer;
	private final float TOUCH_SCALE_FACTOR = 0.3f;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		mRenderer = new MyGLRenderer(this);
		mGLView = new MyGLSurfaceView(this, mRenderer);
		mGLView.setTouchDeltaListener(this);
		
		//mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // Render the view only when there is a change in the drawing data

		setContentView(mGLView);
	}
	
	

	@Override
	protected void onPause() { 
		super.onPause();
		mGLView.onPause();
	}



	@Override
	protected void onResume() {
		super.onResume();
		mGLView.onResume();
	}



	@Override
	public void onTouchDelta(float dx, float dy) {
		mRenderer.mTouchInputX += dx * TOUCH_SCALE_FACTOR ;
		mRenderer.mTouchInputY += dy * TOUCH_SCALE_FACTOR;
	}
}
