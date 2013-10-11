package com.shoky.myapp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	
	public final static String EXTRA_MESSAGE = "com.shoky.myapp.MESSAGE";
	public final static int CAM_REQUEST = 123;
	private static final int CAMLAB_REQUEST = 321;
	
	private ImageFragment mImgFrag = null;
	private String mPicFilePath = null;
	EditText mEditText;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	Log.d("ZZZ", "- CREATE");
    	
        setContentView(R.layout.activity_main);
        
    	mEditText = (EditText) findViewById(R.id.edit_message);

        mImgFrag = (ImageFragment)getFragmentManager().findFragmentById(R.id.image_fragment);
		mImgFrag.setRetainInstance(true);

        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        	getActionBar().setHomeButtonEnabled(false);        
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) 
		{
			case R.id.action_search:
				
				android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
			     android.hardware.Camera.getCameraInfo(0, info);
			     int rotation = getWindowManager().getDefaultDisplay().getRotation();
			     int degrees = 0;
			     switch (rotation) {
			         case Surface.ROTATION_0: degrees = 0; break;
			         case Surface.ROTATION_90: degrees = 90; break;
			         case Surface.ROTATION_180: degrees = 180; break;
			         case Surface.ROTATION_270: degrees = 270; break;
			     }
			     
			     Log.d("CAMCAM", String.format("displayOrientation(%s) camFAcing(%d) camOrientation(%d)", degrees, info.facing, info.orientation));
			     
				openCamLab();
		
				return true;
				
			case R.id.action_search2:				
				takePic();			    
				return true;
				
			case R.id.action_opengl:
				openOpenGL();
				return true;
				
			case R.id.action_settings:
				//openSettings();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
    
    private void openOpenGL() {
    	Intent i = new Intent(this, OpenGLActivity.class);    	
    	i.putExtra(EXTRA_MESSAGE, mEditText.getText().toString());
    	startActivity(i);
	}

	public void openCamLab()
    {
    	Intent i = new Intent(this, CamLabActivity.class);    	
    	i.putExtra(EXTRA_MESSAGE, mEditText.getText().toString());
    	startActivityForResult(i, CAMLAB_REQUEST);
    }
	
	public static File createTempJpeg(String prefix, String folderName) 
	{
		File picsDir = new File(
				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				folderName);
			
		picsDir.mkdirs();
		
		File f = null;
		
		try {	
			f = File.createTempFile(prefix, ".jpg", picsDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return f;
	}
    
	public void takePic() 
	{
		if ( !isIntentAvailable(this, MediaStore.ACTION_IMAGE_CAPTURE) )
			return;
		
		File f = createTempJpeg("picpic", "shokypics");
		
		if (f == null)
			return;
		
		Log.w("WEWE", "Opening Cam on file: " + f.getAbsolutePath());
		
		mPicFilePath = f.getAbsolutePath();
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
		startActivityForResult(intent, CAM_REQUEST);
	}   
    

	public void sendMessage(View view) {
    	Intent intent = new Intent(this, DisplayMessageActivity.class);    	
    	String msg = mEditText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, msg);
    	startActivity(intent);
		
    }
		
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {		
		super.onActivityResult(requestCode, resultCode, intent);
		
		if (requestCode == CAM_REQUEST && resultCode == RESULT_OK) 
		{
			showJpeg(mPicFilePath);
		}
		else if (requestCode == CAMLAB_REQUEST && resultCode == RESULT_OK) 
		{
			Log.i("MAIN", "CAMLAB RETURNED OK");
			showJpeg(intent.getExtras().getString("jpegPath"));
		}
	}

	public void showJpeg(String jpegPath) 
	{
		if (jpegPath != null) 
		{
			View imgView = mImgFrag.getView().findViewById(R.id.imageView1);
			
			new AsyncTask<Object, Void, Bitmap>() 
			{
				protected Bitmap doInBackground(Object... params) {
					return scaleImageToSize((String)params[0], (Integer)params[1], (Integer)params[2]);
				}
				
				protected void onPostExecute(Bitmap b) {
					if (b != null)
						mImgFrag.setImageBitmap( b );
				}
			}.execute(jpegPath, imgView.getWidth(), imgView.getHeight());
		}
	}

	// scales image file to a view
	public Bitmap scaleImageToSize(String filePath, int targetW, int targetH) 
	{
		if (targetW == 0 || targetH == 0) {
			Log.w("MAIN", "scaleImageToSize - target dimensions are 0");
			return null;
		}
		
		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
  
		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
  
		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		
		return BitmapFactory.decodeFile(filePath, bmOptions);
	}
	
	
	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}	
}
