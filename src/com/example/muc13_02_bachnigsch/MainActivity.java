package com.example.muc13_02_bachnigsch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

/**
 * 
 * @author Martin Bach
 * @author Maximilian Nigsch
 * 
 * This MainActivity was mainly used for debugging purposes, it now only redirects to ListBTServerActivity
 *
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startActivity(new Intent(this, ListBTServerActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void listServer(View view) {
		Intent intent = new Intent(this, ListBTServerActivity.class);
		startActivity(intent);
	}
	
	public void gestureTest(View view) {
		Intent intent = new Intent(this, GameActivity.class);
		startActivity(intent);
	}

}
