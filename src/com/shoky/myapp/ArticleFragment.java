package com.shoky.myapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ArticleFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.fragment_article, container, false);
	}
	
	public void setButtonText(String s) {
		TextView tv = (TextView)getView().findViewById(R.id.textView1);
		tv.setText(s);
	}
	
}
