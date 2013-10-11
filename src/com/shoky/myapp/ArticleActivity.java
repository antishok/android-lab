package com.shoky.myapp;

import android.app.Activity;
import android.os.Bundle;

public class ArticleActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/* 
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			finish();
			return;
	    }
	    */
		
		setContentView(R.layout.activity_article);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			ArticleFragment a = (ArticleFragment)getFragmentManager().findFragmentById(R.id.article_fragment);
			a.setButtonText( extras.getString("ARTICLE_NAME") );
		}
	}


}
