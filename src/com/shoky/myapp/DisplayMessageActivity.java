package com.shoky.myapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DisplayMessageActivity extends Activity implements TitlesFragment.TitlesListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_display_message);
		
		if (savedInstanceState != null)
			return; // restoring from previous state, no need to create/update fragment

		String msg = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
		
		TitlesFragment f = (TitlesFragment) 
				getFragmentManager().findFragmentById(R.id.titles_fragment);
		
		f.setButtonText( msg );
		
	}
	
	@Override
	public void onTitlesClick(String text) {		
		ArticleFragment articleFrag = (ArticleFragment) 
				getFragmentManager().findFragmentById(R.id.article_fragment);
				
		text = "CLICKED: " + text;
		if (articleFrag != null && articleFrag.isInLayout()) 
		{
			// we're on tablet, article fragment is visible
			articleFrag.setButtonText(text);
		}
		else
		{
			// we're on handset, start article-activity
			Intent intent = new Intent(this, ArticleActivity.class);
			intent.putExtra("ARTICLE_NAME", text);
			startActivity(intent);
		}		
	}

}
