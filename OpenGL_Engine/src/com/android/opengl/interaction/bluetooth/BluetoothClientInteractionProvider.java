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
		
		if (mBluetoothClientSocket == null || !mBluetoothClientSocket.isConnected()){
			Log.w(TAG, "sendData(): BluetoothClientSocket is not connected. Can't send data");
			return;
		}
		try {
			OutputStream outputStream = mBluetoothClientSocket.getOutputStream();
			PrintWriter printWriter = new PrintWriter(outputStream);
			printWriter.println(data.toString());
			printWriter.flush();
		} catch (IOException e) {
			Log.e(TAG, "sendData(): " + e);
		}
	}

	
	public void startClient(BluetoothDevice bluetoothDevice, UUID uuid) throws IOException{
		stopClient();
		mBluetoothClientSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
		mBluetoothAdapter.cancelDiscovery();
		mBluetoothClientSocket.connect();
		startListningData();
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
		stopListningData();
	}

	@Override
	public void startClient() {
		// TODO Auto-generated method stub
		
	}

	public void startListningData() {
		if(mDataListenerThread != null){
			Log.i(TAG, "startListningData(): client is already listning for new data");
			return;
		}
		if(mBluetoothClientSocket != null){
			List<BluetoothSocket> bluetoothSocketList = new ArrayList<BluetoothSocket>();
			bluetoothSocketList.add(mBluetoothClientSocket);
			startListningData(bluetoothSocketList);
		}
	}


}
