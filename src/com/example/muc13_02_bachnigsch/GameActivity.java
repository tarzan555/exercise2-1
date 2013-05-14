package com.example.muc13_02_bachnigsch;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

/**
 * 
 * @author Max Nigsch
 * @author Martin Bach
 * 
 * The Game activity - implements the game logic
 *
 */

public class GameActivity extends Activity {

	GestureHandler gestureHandler = new GestureHandler(this);
	
	
	public void gestureTest (View view){
		
		List<String> gestures = gestureHandler.recordGesture();
	
		System.out.println("Gesten: " + gestures);
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Activity Stuff
	 */
	
	
	
	@Override
	protected void onResume() {
		// call super method
		super.onResume();
		
		// bind service
		gestureHandler.bind();	
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// unbind service
		gestureHandler.unbind();

	}
	
	

}
