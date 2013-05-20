package com.example.muc13_02_bachnigsch;

import com.example.muc13_02_bachnigsch.bt.ConnectedThread;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Max Nigsch
 * @author Martin Bach
 * 
 *         The Game activity - implements the game logic
 * 
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GameActivity extends Activity {
    // class-name for debug output
    private static final String TAG = "GameActivity";

    // enum for differentiating between roles of players
    private enum PlayerRole {
	PERFORMER, RECEIVER
    };

    private PlayerRole currentRole;

    // all possible gestures
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int CIRCLERIGHT = 5;
    public static final int CIRCLELEFT = 6;
    public static final int SQUARE = 7;
    public static final int SQUAREANGLE = 8;
    public static final String[] gestureNames = { "null", "up", "down", "left",
	    "right", "circle_right", " circle_left", "square", "square_angle" };

    public static final int RECEIVED_GESTURE = 7;
    
    private Vibrator mVibrator;

    // thread that manages communication
    private ConnectedThread ct;
    
    // current timeFrame in millisecons
    private long timeFrame;
    private int roundNumber;

    private GestureHandler gestureHandler = new GestureHandler(this);
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private String performedGestureName;
    private String performedGestureNames;
    private String receivedGestureName;
    private String receivedGestureNames;

    /**
     * @deprecated method was used for debuging purposes
     * 
     * method called on button "Gestentest" clicked
     * 
     * @param view
     */
    @Deprecated
    public void gestureTest(View view) {
	textView2 = (TextView) findViewById(R.id.textView2);
	textView2
		.setText("Geste: " + gestureHandler.getPerformedGestures(System.currentTimeMillis(), 5000));
    }

    /**
     * Game Logic
     */

    private int points;
    private int player;

    public void startGame(View view) {

	// initial situation: server is PERFORMER, client is RECEIVER

	// play loop
	while (true) {

	    if (currentRole == PlayerRole.PERFORMER) {

		/**
		 * It's player 1 's turn
		 */

		// player has to perform a gesture
		textView2 = (TextView) findViewById(R.id.textView2);
		textView2.setText("Du bist dran - mache eine Geste!");

		// get performed gesture from Gesture Handler
		performedGestureName = gestureHandler.getLastPerformedGesture();
		Log.v(TAG, "Player 1: performedGesture = "
			+ performedGestureName);

		// TODO: send performed gesture name via bluetooth

		// TODO: recieve repeated gesture name via bluetooth
		receivedGestureNames = "right left up";

		// compare receied with performed gesture

		// player 1 looses
		if (receivedGestureNames.contains(performedGestureName)) {
		    // in next loop you are player 2
		    player = 2;
		    Log.v(TAG, "Player 1 looses");
		    Log.v(TAG, "Next turn: Player 2");
		}

		// player 1 wins
		else {
		    points++;
		    // in next loop you are player 1
		    player = 1;

		    Log.v(TAG, "Player 2 looses");
		    Log.v(TAG, "Next turn: Player 1");
		}
		textView3 = (TextView) findViewById(R.id.textView3);
		textView3.setText("Du hast " + points + " Punkte");
	    }

	    if (currentRole == PlayerRole.RECEIVER) {

		/**
		 * It's player 2's turn
		 */

		textView2 = (TextView) findViewById(R.id.textView2);
		textView2.setText("Warte auf Geste von Gegner");

		// TODO: receive gesture via bluetooth
		// wait for gesture via bluetooth from other player
		receivedGestureName = "right";

		textView2.setText("Geste empfangen - wiederhole sie!");

		// get performed gestures
		performedGestureNames = gestureHandler
			.getPerformedGestures(System.currentTimeMillis(), 5000);

		Log.v(TAG, "Player 2: performedGestures = "
			+ performedGestureNames);

		// gestures are according - player 2 wins
		if (performedGestureNames.contains(receivedGestureName)) {
		    points++;
		    // in next loop you are player 1
		    player = 1;

		    Log.v(TAG, "Player 2 wins");
		}

		// gestures are different - player 2 looses
		else {
		    // in next loop you are player 2
		    player = 2;

		    Log.v(TAG, "Player 1 wins");
		}

		textView3 = (TextView) findViewById(R.id.textView3);
		textView3.setText("Du hast " + points + " Punkte");
	    }
	}
    }

    /**
     * this method gets called as soon as a gesture is performed and recognized
     * 
     * tests whether player is PERFORMER or RECEIVER in case of PERFORMER
     * nothing is done in case of RECEIVER: name of gesture is send via BT to
     * PERFORMER
     * 
     * @param gestureName
     * @param timestamp
     */
    public void onGestureRecognized(String gestureName, long timestamp) {
	if (currentRole == PlayerRole.RECEIVER) {
	    ct.write("gesture:" + gestureName);
	}
    }

    /**
     * this method gets called as soon as a gesture was received via BT
     * should only be called when currentRole is PERFORMER
     * 
     * @param gesture
     * @param timestamp
     */
    public void onGestureReceived(int gesture, long timestamp) {
	if (currentRole == PlayerRole.PERFORMER) {
	    // check if gesture is in time
	    performedGestureNames = gestureHandler.getPerformedGestures(timestamp, 2000);
	    if (performedGestureNames.contains(gestureNames[gesture])) {
		// you loose
		ct.write("result:win");
		currentRole = PlayerRole.RECEIVER;
		
		// notify user
		mVibrator.vibrate(500);
	    } else {
		// you win
		ct.write("result:loss");
		points++;
		currentRole = PlayerRole.PERFORMER;
	    }
	    roundNumber++;
	    updateView();
	}
	Log.v(TAG, "received gesture=" + gesture);
    }
    
    /**
     * this method gets called as soon as a result was received via BT
     * should only be called if currentRole is RECEIVER
     * 
     * @param win is true in case of win, false in case of loss
     */
    public void onResultReceived(boolean win) {
	if(currentRole == PlayerRole.RECEIVER) {
	    if(win) {
		points++;
		currentRole = PlayerRole.PERFORMER;
	    } else {
		currentRole = PlayerRole.RECEIVER;
		
		// notify user
		mVibrator.vibrate(500);
	    }
	    roundNumber++;
	    updateView();
	}
	Log.v(TAG, "received result="+win);
    }

    /**
     * Updates all elements in View
     */
    public void updateView() {
	if (currentRole == PlayerRole.PERFORMER) {
	    textView2.setText("Du bist dran - mache eine Geste!");
	} else {
	    textView2.setText("Warte auf Geste von Gegner");
	}
	textView3.setText("Du hast " + points + " Punkte");
	textView4.setText("Runde "+roundNumber);
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

	/*
	 * TextViews
	 */
	textView2 = (TextView) findViewById(R.id.textView2);
	textView3 = (TextView) findViewById(R.id.textView3);
	textView4 = (TextView) findViewById(R.id.textView4);
	
	// initialize variables
	points = 0;
	timeFrame = 2000;	// start with 2s timeframe
	roundNumber = 1;
	
	// get systemvibrator
	mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
	if(!mVibrator.hasVibrator()) {
	    Toast toast = Toast.makeText(getApplicationContext(), "Device not able to vibrate!", Toast.LENGTH_LONG);
	    toast.show();
	}
	

	// decide whether we start as "performer" or "receiver"
	if (ListBTServerActivity.ct != null) {
	    // receiver
	    ct = ListBTServerActivity.ct;
	    currentRole = PlayerRole.RECEIVER;
	    Log.v(TAG, "Im the receiver");
	} else if (StartingServerActivity.ct != null) {
	    // performer
	    ct = StartingServerActivity.ct;
	    currentRole = PlayerRole.PERFORMER;
	    Log.v(TAG, "Im the performer");
	}
	ct.registerActivity(this);
	ct.start();

	updateView();

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