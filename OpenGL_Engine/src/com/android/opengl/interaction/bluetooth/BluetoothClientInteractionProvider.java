package com.android.opengl.interaction.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.android.opengl.interaction.BaseClientInteractionProvider;
import com.android.opengl.util.Log;

public class BluetoothClientInteractionProvider extends BaseBluetoothInteractionProvider implements BaseClientInteractionProvider{
	
	private static final String TAG = BluetoothClientInteractionProvider.class.getSimpleName();
	private BluetoothSocket mBluetoothClientSocket;

	public BluetoothClientInteractionProvider() throws IllegalAccessException{
		super(TAG);
	}
	

	@Override
	public void sendData(JSONObject data) {
		if (!mBluetoothClientSocket.isConnected()){
			Log.w(TAG, "sendData(): BluetoothClientSocket is not connected yet. Can't send data");
			return;
		}
		OutputStream outputStream = null;
		try {
			outputStream = mBluetoothClientSocket.getOutputStream();
		} catch (IOException e) {
			Log.e(TAG, "sendData(): " + e);
			return;
		}
		PrintWriter printWriter = new PrintWriter(outputStream);
		printWriter.println(data.toString());
		printWriter.flush();
	}

	
	public void startClient(BluetoothDevice bluetoothDevice, UUID uuid) throws IOException{
		stopClient();
		mBluetoothClientSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
		mBluetoothAdapter.cancelDiscovery();
		mBluetoothClientSocket.connect();
		notifyConnectionChanged();
	}

	@Override
	public void stopClient() {
		if(mBluetoothClientSocket != null){
			try {
				mBluetoothClientSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "stopClient(): " + e);
			}
		}		
		mBluetoothClientSocket = null;
		mOnNewDataListener = null;
		notifyConnectionChanged();
	}

	@Override
	public void startClient() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startListningData(OnNewDataListner onNewDataListner) {
		if(mDataListenerThread != null){
			Log.i(TAG, "startListningData(): client is already listning for new data");
			return;
		}
		mOnNewDataListener = onNewDataListner;
		if(mBluetoothClientSocket != null){
			List<BluetoothSocket> bluetoothSocketList = new ArrayList<BluetoothSocket>();
			bluetoothSocketList.add(mBluetoothClientSocket);
			startListningData(bluetoothSocketList, onNewDataListner);
		}
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
