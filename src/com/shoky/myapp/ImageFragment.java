package com.shoky.myapp;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageFragment extends Fragment {
	
	private Bitmap mBitmap = null;
	private ImageView iv = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_image, container, false);
		iv = (ImageView)v.findViewById(R.id.imageView1);
		if (mBitmap != null)
			iv.setImageBitmap(mBitmap);
		return v;
	}
	
	public void setImageBitmap(Bitmap b) {
		mBitmap = b;
		iv.setImageBitmap(mBitmap);
	}

}
