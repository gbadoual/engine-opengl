package com.android.opengl.interaction.remote.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.android.opengl.interaction.remote.IBaseServerProvider;
import com.android.opengl.util.Log;

public class BluetoothServerProvider extends BaseBluetoothProvider implements IBaseServerProvider{
	
	private static final String TAG = BluetoothServerProvider.class.getSimpleName();
	
    private String mServerName;
	private UUID mUuid;
	private List<BluetoothSocket> mBluetoothClientSocketList = Collections.synchronizedList(new ArrayList<BluetoothSocket>());
	private BluetoothServerThread mServerThread;

	public BluetoothServerProvider(String name, UUID uuid) throws IllegalAccessException{
		super(TAG);
		mServerName = name;
		mUuid = uuid;
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
	public void sendData(JSONObject data) {
		for(BluetoothSocket bluetoothClientSocket: mBluetoothClientSocketList){
			try {
				OutputStream outputStream = bluetoothClientSocket.getOutputStream();
				PrintWriter printWriter = new PrintWriter(outputStream);
				printWriter.println(data.toString());
				printWriter.flush();
			} catch (IOException e) {
				Log.e(TAG, "sendData(): " + e);
			}
		}
	}

	@Override
	public void startServer() throws IOException{
		if(mServerThread != null){
			Log.d(TAG, "Serevr is already started. Use stopServer() before starting it again");
			return;
		}
		mServerThread = new BluetoothServerThread(mServerName, mUuid);
		mServerThread.start();
		resgisterOnBTDeviceConnectListener(mBluetoothDeviceConnectListener);
		startListningData(mBluetoothClientSocketList);
	}
	
	public void stopServer(){
		if(mServerThread != null){
			mServerThread.interrupt();
		}
		mServerThread = null;
		stopListningData();
		unresgisterOnBTDeviceConnectListener(mBluetoothDeviceConnectListener);
	}
	

	
	private OnBluetoothDeviceConnectListener mBluetoothDeviceConnectListener = new OnBluetoothDeviceConnectListener() {
		
		@Override
		public void onBluetoothDeviceConnected(BluetoothSocket bluetoothSocket) {
			stopListningData();
			startListningData(mBluetoothClientSocketList);
		}
	};

	public void enableDiscoverability(Context context) {
		if(!mBluetoothAdapter.isEnabled()){
			context.startActivity(getEnableBluetoothRequestIntent());
			return;
		}
		context.startActivity(getEnableDiscoverablyModeIntent(300));
	}


} 
