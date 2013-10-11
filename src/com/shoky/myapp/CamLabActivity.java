package com.shoky.myapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class CamLabActivity extends Activity {
	
	Camera mCamera;
	CamPreviewSurface mPreviewSurface;
	RelativeLayout layout;
	int mViewWidth, mViewHeight;
	
	GestureDetectorCompat mDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		layout = new RelativeLayout(this) 
		{
			protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			    mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
			    mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
			    this.setMeasuredDimension(mViewWidth, mViewHeight);
			    Log.d("CAMLAB", String.format("onMeasure: %d, %d", mViewWidth, mViewHeight));
			}

			public boolean onTouchEvent(MotionEvent event) {
				if (mDetector != null) {
					mDetector.onTouchEvent(event);
					return true;
				}
				return super.onTouchEvent(event);
			}			
		};
		
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		mPreviewSurface = new CamPreviewSurface( this );
		
		RelativeLayout.LayoutParams previewParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		previewParams.addRule(RelativeLayout.CENTER_IN_PARENT);		
		mPreviewSurface.setLayoutParams(previewParams);
		
		layout.addView(mPreviewSurface);
		
		mDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
			public boolean onDown(MotionEvent e) { 
	            return true;
	        }
			
			public void onLongPress(MotionEvent e) {
				takePic();
			}
		});
		
		setContentView( layout );
	}
	
	public void takePic() {
		if (!mPreviewSurface.mPreviewStarted)
			return;
		
		Log.i("CAMLAB", "Taking pic");
		
		mCamera.takePicture(null, null, new Camera.PictureCallback() {			
			public void onPictureTaken(byte[] jpegData, Camera camera) {
				Log.i("CAMLAB", "TOOK PIC. SIZE: " + (jpegData != null ? jpegData.length : "NULL!"));
				
				File f = MainActivity.createTempJpeg("camlab", "shokypics");
				try {
					FileOutputStream stream = new FileOutputStream(f);
					stream.write(jpegData);
					stream.close();					
					Log.i("CAMLAB", "SAVED JPEG: " + f.getAbsolutePath());
					
					Intent intent = new Intent();
					intent.putExtra("jpegPath", f.getAbsolutePath());
					setResult(RESULT_OK, intent);
					
				} catch (Exception e) {
					e.printStackTrace();
					setResult(1);
				}
				
				releaseCameraAndPreview();				
				finish();
				
				//camera.startPreview();	
			}
		});
		
	}
	
	protected void onResume() {
		super.onResume();
		
		openCamAsync(0);
	}

	protected void onPause() {
		super.onPause();

		releaseCameraAndPreview();
	}
	
	public void openCamAsync(int camId) 
	{
		releaseCameraAndPreview();
		
		new AsyncTask<Integer, Void, Camera>() 
		{
			protected Camera doInBackground(Integer... camIds) {
				try {
					return Camera.open(camIds[0]); 
			    } catch (Exception e) {
			        Log.e("CAMLAB", "failed to open Camera");
			        e.printStackTrace();
			    }
				return null;
			}

			protected void onPostExecute(Camera camera) 
			{
				onCameraOpened(camera);				
			}
		}.execute(camId);
	}
	
	protected void onCameraOpened(Camera camera)
	{
		mCamera = camera;
		if (mCamera == null) {
			Log.e("CAMLAB", "ERROR: CAM BAD");
			return;
		}
		
		Log.i("CAMLAB", "opened cam successfully");
					
		setCameraDisplayOrientation(this, 0, mCamera);
		
		boolean bDeviceIsPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		
		// normalize to work with landscape coordinates regardless of device orientation:
		int vWidth = bDeviceIsPortrait ? mViewHeight : mViewWidth;
		int vHeight = bDeviceIsPortrait ? mViewWidth : mViewHeight;
		

		Camera.Parameters camParams = mCamera.getParameters();				
        List<Size> camSupportedPreviewSizes = camParams.getSupportedPreviewSizes();
        
		Size oldCamPreviewSize = camParams.getPreviewSize();
		Size newCamPreviewSize = getOptimalPreviewSize(camSupportedPreviewSizes, vWidth, vHeight);
		
		if (newCamPreviewSize != null)
		{
			if (!oldCamPreviewSize.equals(newCamPreviewSize)) {
				camParams.setPreviewSize( newCamPreviewSize.width,  newCamPreviewSize.height );
				mCamera.setParameters(camParams);
			}
			
			Log.d("CAMLAB", "old cam preview size: " + oldCamPreviewSize.width + " " + oldCamPreviewSize.height);
			Log.d("CAMLAB", "new cam preview size: " + newCamPreviewSize.width + " " + newCamPreviewSize.height);		
			
			double camRatio = (double)newCamPreviewSize.width / newCamPreviewSize.height;
			double viewRatio = (double)vWidth / vHeight;
			
			int surfaceWidth, surfaceHeight;
			
			// scale preview layout size to best-fit in viewport:
			if (camRatio < viewRatio) {
				surfaceHeight = LayoutParams.MATCH_PARENT;
				double factor = (double)newCamPreviewSize.height / vHeight;
				surfaceWidth = (int)Math.round((double)newCamPreviewSize.width / factor);
			}
			else {
				surfaceWidth = LayoutParams.MATCH_PARENT;
				double factor = (double)newCamPreviewSize.width / vWidth;
				surfaceHeight = (int)Math.round((double)newCamPreviewSize.height / factor);
			}

			if (bDeviceIsPortrait) 
			{ // swap back to portrait coordinates
				int tmp = surfaceWidth;
				surfaceWidth = surfaceHeight;
				surfaceHeight = tmp;
			}
			
			RelativeLayout.LayoutParams surfaceParams = (RelativeLayout.LayoutParams) mPreviewSurface.getLayoutParams();
			surfaceParams.width = surfaceWidth;
			surfaceParams.height = surfaceHeight;
			mPreviewSurface.setLayoutParams(surfaceParams);
			
			Log.d("C", String.format("new preview-surface size(%d %d) camR(%f) viewR(%f)", surfaceParams.width, surfaceParams.height, camRatio, viewRatio));
		}
		
		mPreviewSurface.setCamera(mCamera);	
	}
	
	private void releaseCameraAndPreview() {
	    if (mPreviewSurface != null)
	    	mPreviewSurface.setCamera(null);
	    if (mCamera != null) {
	        mCamera.release();
	        mCamera = null;
	    }
	}


	public static Size getOptimalPreviewSize(List<Size> camSupportedPreviewSizes, int targetWidth, int targetHeight) 
	{
		final double ASPECT_TOLERANCE = 0.05;
		final double targetRatio = (double)targetWidth / targetHeight;
		Size optimalPreviewSize = null;
		int minDiff = Integer.MAX_VALUE;
		
		for (Size size : camSupportedPreviewSizes) {
		    double ratio = (double) size.width / size.height;
		    if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
		    int currDiff = Math.abs(size.height - targetHeight);
		    if (currDiff < minDiff) {
		    	optimalPreviewSize = size;
		        minDiff = currDiff;
		    }
		}
		
		if (optimalPreviewSize == null) {
			minDiff = Integer.MAX_VALUE;
			for (Size size: camSupportedPreviewSizes) 
			{
				int currDiff = Math.abs(size.height - targetHeight); 
				if (currDiff < minDiff) {
					optimalPreviewSize = size;
					minDiff = currDiff;
				}
			}
		}
		
		return optimalPreviewSize;
	}




	public static int setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
	     Camera.CameraInfo info = new Camera.CameraInfo();
	     Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     camera.setDisplayOrientation(result);
	     return result;
	 }
	
	
	class CamPreviewSurface extends SurfaceView implements SurfaceHolder.Callback {

	    SurfaceHolder mHolder;
		private Camera mPreviewCamera;
		public boolean mPreviewStarted = false;

		CamPreviewSurface(Context context) {
	        super(context);

	        // Install a SurfaceHolder.Callback, so we get notified when the underlying surface is created and destroyed.
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // deprecated in api 11
	    }
	    
	    public void setCamera(Camera camera) 
	    {
	    	if (mPreviewCamera == camera) { return; }
	        
	        stopPreviewAndFreeCamera(); // clear old camera & preview
	        
	        mPreviewCamera = camera;
	        if (mPreviewCamera != null) {

	        	//requestLayout();
	            
	            mPreviewCamera.startPreview();
	            mPreviewStarted = true;
	            
	            try {
					mPreviewCamera.setPreviewDisplay(mHolder);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    }
	    
	    

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			if (mPreviewCamera == null)
				return;
			
		    try {
				mPreviewCamera.setPreviewDisplay(mHolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
		    // Surface will be destroyed when we return, so stop the preview.
		    if (mPreviewCamera != null) {
		        /*
		          Call stopPreview() to stop updating the preview surface.
		        */
		        mPreviewCamera.stopPreview();
		        mPreviewStarted = false;
		    }
		}
		
	
		/**
		  * When this function returns, mCamera will be null.
		  */
		private void stopPreviewAndFreeCamera() {
		
		    if (mPreviewCamera != null) {
		        /*
		          Call stopPreview() to stop updating the preview surface.
		        */
		        mPreviewCamera.stopPreview();
		        mPreviewStarted = false;

		    
		        /*
		          Important: Call release() to release the camera for use by other applications. 
		          Applications should release the camera immediately in onPause() (and re-open() it in
		          onResume()).
		        */
		        mPreviewCamera.release();
		    
		        mPreviewCamera = null;
		    }
		}		

		@Override
		protected void onLayout(boolean arg0, int arg1, int arg2, int arg3,int arg4) {
		}
	
	}

	

}
