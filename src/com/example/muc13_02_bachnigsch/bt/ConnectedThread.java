/**
 * 
 */
package com.example.muc13_02_bachnigsch.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.muc13_02_bachnigsch.GameActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * ConnectedThread manages a BluetoothSocket for communication via BT
 * 
 * - run-method waits for messages coming through the socket - write can be
 * called to send messages
 * 
 * @author Martin Bach
 * 
 */
public class ConnectedThread extends Thread {
    // class-name for debug output
    private static final String TAG = "ConnectedThread";

    private static final int WIN = 1;
    private static final int LOSS = 0;

    private final BluetoothSocket mSocket;
    private final InputStream mInStream;
    private final OutputStream mOutStream;

    private GameActivity mGameActivity;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {

	@Override
	public void handleMessage(Message msg) {
	    // Get GameActivity
	    GameActivity gameActivity = (GameActivity) msg.obj;

	    switch (msg.what) {
	    case GameActivity.RECEIVED_GESTURE:
		gameActivity.onGestureReceived(msg.arg1,
			System.currentTimeMillis());
		break;
	    case WIN:
		gameActivity.onResultReceived(true);
		break;
	    case LOSS:
		gameActivity.onResultReceived(false);
		break;
	    default:
		super.handleMessage(msg);
	    }
	}

    };

    /**
     * Constructor of ConnectedThread
     * 
     * @param socket
     *            already established BluetoothSocket
     */
    public ConnectedThread(BluetoothSocket socket) {
	mSocket = socket;
	InputStream tmpIn = null;
	OutputStream tmpOut = null;

	Log.v(TAG, "Creating ConnectedThread...");

	// Get input and output streams
	try {
	    tmpIn = socket.getInputStream();
	    tmpOut = socket.getOutputStream();
	} catch (IOException e) {
	}

	mInStream = tmpIn;
	mOutStream = tmpOut;
    }

    public void registerActivity(GameActivity gameActivity) {
	mGameActivity = gameActivity;
    }

    public void unregisterActivity(GameActivity gameActivity) {
	if (mGameActivity == gameActivity)
	    mGameActivity = gameActivity;
    }

    /**
     * Thread-method awaiting messages coming through socket
     */
    public void run() {
	byte[] buffer = new byte[1024]; // buffer store for the stream
	int bytes; // bytes returned from read()

	// Keep listening to inputstream until exception occurs
	while (true) {
	    try {
		// read from input
		bytes = mInStream.read(buffer);

		// extract gesture
		String msg = new String(buffer, 0, bytes);
		int gesture = 0;
		if (msg.equals("gesture:up")) {
		    gesture = GameActivity.UP;
		} else if (msg.equals("gesture:down")) {
		    gesture = GameActivity.DOWN;
		} else if (msg.equals("gesture:left")) {
		    gesture = GameActivity.LEFT;
		} else if (msg.equals("gesture:right")) {
		    gesture = GameActivity.RIGHT;
		} else if (msg.equals("gesture:circle_right")) {
		    gesture = GameActivity.CIRCLERIGHT;
		} else if (msg.equals("gesture:circle_left")) {
		    gesture = GameActivity.CIRCLELEFT;
		} else if (msg.equals("gesture:square")) {
		    gesture = GameActivity.SQUARE;
		} else if (msg.equals("gesture:square_angle")) {
		    gesture = GameActivity.SQUAREANGLE;
		} else if (msg.equals("result:win") && mGameActivity != null) {
		    mHandler.obtainMessage(WIN, mGameActivity).sendToTarget();
		} else if (msg.equals("result:loss")) {
		    mHandler.obtainMessage(LOSS, mGameActivity).sendToTarget();
		}

		// send data to ui activity
		if (mGameActivity != null && gesture > 0) {
		    mHandler.obtainMessage(GameActivity.RECEIVED_GESTURE,
			    gesture, 0, mGameActivity).sendToTarget();
		    Log.v(TAG, "Handler called");
		}

		Log.v(TAG, "received message: " + msg);
	    } catch (IOException e) {
		Log.e(TAG, e.getMessage());
		break;
	    }
	}
    }

    /**
     * sends given message through stream of connected socket
     * 
     * @param message
     */
    public void write(String message) {
	try {
	    Log.v(TAG, "Sending message: " + message);
	    mOutStream.write(message.getBytes());
	} catch (IOException e) {
	    Log.e(TAG, e.getMessage());
	}
    }

    /**
     * Call this to shutdown connection
     */
    public void cancel() {
	try {
	    mSocket.close();
	} catch (IOException e) {
	}
    }
}
