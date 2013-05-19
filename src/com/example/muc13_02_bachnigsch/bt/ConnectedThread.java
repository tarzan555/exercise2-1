/**
 * 
 */
package com.example.muc13_02_bachnigsch.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * ConnectedThread manages a BluetoothSocket for communication via BT
 * 
 *  - run-method waits for messages coming through the socket
 *  - write can be called to send messages
 * 
 * @author Martin Bach
 *
 */
public class ConnectedThread extends Thread {
    // class-name for debug output
    private static final String TAG = "ConnectedThread";
    
    private final BluetoothSocket mSocket;
    private final InputStream mInStream;
    private final OutputStream mOutStream;
    
    /**
     * Constructor of ConnectedThread
     * @param socket already established BluetoothSocket
     */
    public ConnectedThread(BluetoothSocket socket) {
	mSocket = socket;
	InputStream tmpIn = null;
	OutputStream tmpOut = null;
	
	Log.v(TAG,"Creating ConnectedThread...");
	
	// Get input and output streams
	try {
	    tmpIn = socket.getInputStream();
	    tmpOut = socket.getOutputStream();
	} catch(IOException e) {}
	
	mInStream = tmpIn;
	mOutStream = tmpOut;
    }
    
    /**
     * Thread-method awaiting messages coming through socket
     */
    public void run() {
	byte[] buffer = new byte[1024];	// buffer store for the stream
	int bytes;	// bytes returned from read()
	
	// Keep listening to inputstream until exception occurs
	while(true) {
	    try {
		// read from input
		bytes = mInStream.read(buffer);
		// send data to ui activity
		// TODO
		Log.v(TAG,"received message: "+new String(buffer, 0, bytes));
	    } catch(IOException e) {
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
	    mOutStream.write(message.getBytes());
	} catch(IOException e) {}
    }
    
    /**
     * Call this to shutdown connection
     */
    public void cancel() {
	try {
	    mSocket.close();
	} catch(IOException e) {}
    }
}
