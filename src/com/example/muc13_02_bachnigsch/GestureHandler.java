package com.example.muc13_02_bachnigsch;

import java.util.SortedMap;
import java.util.TreeMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import de.dfki.ccaal.gestures.Distribution;
import de.dfki.ccaal.gestures.IGestureRecognitionListener;
import de.dfki.ccaal.gestures.IGestureRecognitionService;

/**
 * 
 * @author Max Nigsch
 * @author Martin Bach
 * 
 *         The Gesture activity
 * 
 */

public class GestureHandler {
    // class-name for debug output
    private static final String TAG = "GestureHandler";

    private GameActivity gameActivity;
    private long mTimeStamp;
    // Map for holding pairs of timestamp and gesture accordingly
    private TreeMap<Long, String> mTreeMap = new TreeMap<Long, String>();

    /**
     * Gesture Stuff
     */
    private IGestureRecognitionService mRecService;
    private String mGestureName;

    // create gestureListener
    private IBinder mGestureListenerStub = new IGestureRecognitionListener.Stub() {
	@Override
	public void onGestureRecognized(Distribution distr) {
	    mGestureName = distr.getBestMatch();
	    mTimeStamp = System.currentTimeMillis();
	    // put gesture name with timestamp into treemap
	    mTreeMap.put(mTimeStamp, mGestureName);
	    
	    gameActivity.onGestureRecognized(mGestureName, mTimeStamp);

	}

	@Override
	public void onGestureLearned(String gestureName) throws RemoteException {
	    // we dont wanna do anything here
	}

	@Override
	public void onTrainingSetDeleted(String trainingSet)
		throws RemoteException {
	    // we dont wanna do anything here
	}
    };

    // create a service connection to the recognition service
    private ServiceConnection mGestureConn = new ServiceConnection() {
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
	    mRecService = IGestureRecognitionService.Stub.asInterface(service);
	    try {
		// register listener
		mRecService.registerListener(IGestureRecognitionListener.Stub
			.asInterface(mGestureListenerStub));
		// start the recognition service in recognition mode with given
		// training set.
		mRecService.startClassificationMode("muc");
		Log.d(TAG, "GestureSet contains following gestures: "+mRecService.getGestureList("muc"));

	    } catch (RemoteException e) {
		e.printStackTrace();
	    }

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
	    // we dont wanna do anything here	    
	}
	
    };

    // Constructor
    public GestureHandler(GameActivity gameActivity) {
	this.gameActivity = gameActivity;
    }

    /**
     * returns TreeMap consisting of all recognized gestures
     * 
     * @return map containing all timestamps and recognized gestures
     */
    public TreeMap<Long, String> getGesture() {
	return mTreeMap;
    }

    /**
     * returns single string containing all gestures in past given milliseconds
     * 
     * @param timestamp 
     * @param timeSlot a timeslot in milliseconds
     * @return string containing all gestures performed in given timeslot
     */
    public String getPerformedGestures(long timestamp, long timeSlot) {
	SortedMap<Long, String> mSubMap = mTreeMap.subMap(
		timestamp - timeSlot, timestamp);
	StringBuilder builder = new StringBuilder();
	for (Long key : mSubMap.keySet()) {
	    builder.append(mSubMap.get(key) + " ");
	}
	return builder.toString();
    }

    /**
     * method that returns last performed gesture
     * 
     * @return last performed gesture
     */
    public String getLastPerformedGesture() {
	return mTreeMap.get(mTreeMap.lastKey());
    }

    /**
     * binds to service of gestureRecognition stuff
     */
    protected void bind() {
	Intent gestureBindIntent = new Intent(
		"de.dfki.ccaal.gestures.GESTURE_RECOGNIZER");
	gameActivity.bindService(gestureBindIntent, mGestureConn,
		Context.BIND_AUTO_CREATE);

    }

    /**
     * unbinds to service of gestureRecognition stuff
     */
    protected void unbind() {
	try {
	    mRecService.unregisterListener(IGestureRecognitionListener.Stub
		    .asInterface(mGestureListenerStub));
	} catch (RemoteException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	mRecService = null;
	gameActivity.unbindService(mGestureConn);
    }

}