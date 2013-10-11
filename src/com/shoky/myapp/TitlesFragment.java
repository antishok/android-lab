package com.shoky.myapp;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class TitlesFragment extends Fragment implements OnClickListener {
	
	private TitlesListener listener;
	
	public interface TitlesListener {
		public void onTitlesClick(String text);
	}
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null)
			Log.d("TITLES", "- create");
		else
			Log.d("TITLES", "- create (restoring)");
		
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) 
	{
		Log.d("TITLES", "--- createview");
		View v = inflater.inflate(R.layout.fragment_titles, container, false);
		
		((Button)v.findViewById(R.id.button1)).setOnClickListener(this);
		((Button)v.findViewById(R.id.button2)).setOnClickListener(this);
		((Button)v.findViewById(R.id.button3)).setOnClickListener(this);
		
		return v;
	}
	
	
	@Override
	public void onAttach(Activity activity) {	
		Log.d("TITLES", "- attach");
		super.onAttach(activity);
		if (!(activity instanceof TitlesListener))
			throw new ClassCastException(activity.toString() + " must implement TitlesListener");
		
		listener = (TitlesListener)activity;
	}

	
	
	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}


	public void setButtonText(String s) {
		Button b = (Button)getView().findViewById(R.id.button1);
		b.setText(s);
	}
	
	@Override
	public void onClick(View v) {
		if (listener != null)
			listener.onTitlesClick( ((Button)v).getText().toString() );
	}

}
