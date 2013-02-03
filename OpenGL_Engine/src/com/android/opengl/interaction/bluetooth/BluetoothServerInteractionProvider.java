package com.android.opengl.interaction.bluetooth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.android.opengl.interaction.BaseServerInteractionProvider;
import com.android.opengl.util.Log;

public class BluetoothServerInteractionProvider extends BaseBluetoothInteractionProvider implements BaseServerInteractionProvider{
	
	private static final String TAG = BluetoothServerInteractionProvider.class.getSimpleName();
	private List<BluetoothSocket> mBluetoothClientSocketList = Collections.synchronizedList(new ArrayList<BluetoothSocket>());

	private BluetoothServerThread mServerThread;

	public BluetoothServerInteractionProvider() throws IllegalAccessException{
		super(TAG);
	}
	
	
	
	private class BluetoothServerThread extends Thread{
		
		private BluetoothServerSocket mBluetoothServerSocket;
		private String mName;
		private UUID mUuid;
		
		
		public BluetoothServerThread(String name, UUID uuid) throws IOException {
			mName = name;
			mUuid = uuid;
		}
		
		@Override
		public void run() {
			while(!isInterrupted()){
				try {
					mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mName, mUuid);
					BluetoothSocket bluetoothSocket = mBluetoothServerSocket.accept();
					mBluetoothClientSocketList.add(bluetoothSocket);
					notifyDeviceConnected(bluetoothSocket);
					closeServerSocket(mBluetoothServerSocket);
					
				} catch (IOException e) {
					Log.e(TAG, "run(): closing server socket" + e);
				}finally{
					closeServerSocket(mBluetoothServerSocket);
				} 
			}
			closeServerSocket(mBluetoothServerSocket);
		}

		private void closeServerSocket(BluetoothServerSocket mBluetoothServerSocket) {
			try {
				if(mBluetoothServerSocket != null){
					mBluetoothServerSocket.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "closeServerSocket(): " + e);				
			}
		}
	}

	


	@Override
	public void startListningData(OnNewDataListner onNewDataListener) {
		if(mDataListenerThread != null){
			Log.i(TAG, "startListningData(): client is already listning for new data");
			return;
		}
		startListningData(mBluetoothClientSocketList, onNewDataListener);		
	}
	

	@Override
	public void sendData(JSONObject jsonObject) {
//		if (!mBluetoothClientSocket.isConnected()){
//			Log.w(TAG, "sendData(): BluetoothClientSocket is not connected yet. Can't send data");
//			return;
//		}
//		OutputStream outputStream = null;
//		try {
//			outputStream = mBluetoothClientSocket.getOutputStream();
//		} catch (IOException e) {
//			Log.e(TAG, "sendData(): " + e);
//			return;
//		}
//		PrintWriter printWriter = new PrintWriter(outputStream);
//		printWriter.println("Hello from client!");
//		printWriter.close();
	}

	public void startServer(String name, UUID uuid) throws IOException{
		if(mServerThread != null){
			throw new IllegalStateException("Serevr is already started. Use stopServer() before starting it again");
		}
		mServerThread = new BluetoothServerThread(name, uuid);
		mServerThread.start();
	}
	
	public void stopServer(){
		if(mServerThread != null){
			mServerThread.interrupt();
		}
		mServerThread = null;
		mOnNewDataListener = null;
		notifyConnectionChanged();
	}
	
	@Override
	public void startServer() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void notifyConnectionChanged() {
		Log.d(TAG, "notifyConnectionChanged(): " + mOnNewDataListener);
		stopListningData(null);
		if(mOnNewDataListener != null){
			startListningData(mOnNewDataListener);
		}				
	}



}
