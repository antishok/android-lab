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
	
		            // reverse direction of rotation above the mid-line
		            if (y > getHeight() / 2) {
		              dx = dx * -1 ;
		            }
	
		            // reverse direction of rotation to left of the mid-line
		            if (x < getWidth() / 2) {
		              dy = dy * -1 ;
		            }
		            
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

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		mRenderer = new MyGLRenderer();
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
		mRenderer.mTouchInput += (dx + dy) * 180.0f / 320; //TOUCH_SCALE_FACTOR;        			
	}
}
