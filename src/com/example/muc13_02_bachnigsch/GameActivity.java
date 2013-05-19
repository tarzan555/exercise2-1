package com.example.muc13_02_bachnigsch;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

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
	TextView textView2;
	TextView textView3;
	String performedGestureName;
	String performedGestureNames;
	String receivedGestureName;
	String receivedGestureNames;


	// method called on button "Gestentest" clicked
	public void gestureTest (View view){
		textView2 = (TextView)findViewById(R.id.textView2);
		textView2.setText("Geste: " + gestureHandler.getPerformedGestures(5000));
	}



	/**
	 * Game Logic
	 */


	int points;
	int player;


	public void startGame(View view){


		// TODO: get to know wether your are server or client

		// initaial situation: server is player 1, client is player 2
		player = 1; 


		// play loop
		for (int i = 0 ; i < 11 ; i++){

			if(player == 1){

				/**
				 * It's player 1 's turn
				 */				

				// player has to perform a gesture
				textView2 = (TextView)findViewById(R.id.textView2);
				textView2.setText("Du bist dran - mache eine Geste!");

				// get performed gesture from Gesture Handler
				performedGestureName = gestureHandler.getLastPerformedGesture();

				// TODO: send performed gesture name via bluetooth

				// TODO: recieve repeated gesture name via bluetooth
				receivedGestureNames = "right left up";

				// compare receied with performed gesture

				// player 1 looses
				if (receivedGestureNames.contains(performedGestureName)){
					// in next loop you are player 2
					player = 2;			
				}

				// player 1 wins
				else {
					points ++;
					// in next loop you are player 1
					player = 1;
				}				
				textView3 = (TextView)findViewById(R.id.textView3);
				textView3.setText("Du hast " + points + " Punkte");				
			}


			if(player == 2){

				/**
				 * It's player 2's turn
				 */

				textView2 = (TextView)findViewById(R.id.textView2);
				textView2.setText("Warte auf Geste von Gegner");

				// TODO: receive gesture via bluetooth
				// wait for gesture via bluetooth from other player
				receivedGestureName = "right";

				textView2.setText("Geste empfangen - wiederhole sie!");

				// get performed gestures
				performedGestureNames = gestureHandler.getPerformedGestures(5000);


				// gestures are according - player 2 wins
				if (performedGestureNames.contains(receivedGestureName)){
					points ++;
					// in next loop you are player 1
					player = 1;					
				}

				// gestures are different - player 2 looses
				else {
					// in next loop you are player 2
					player = 2;
				}

				textView3 = (TextView)findViewById(R.id.textView3);
				textView3.setText("Du hast " + points + " Punkte");
			}			
		}
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