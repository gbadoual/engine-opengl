package com.android.opengl.interaction.remote.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.android.opengl.interaction.remote.IBaseInteractionProvider;
import com.android.opengl.util.Log;

public abstract class BaseBluetoothInteractionProvider implements IBaseInteractionProvider{

	private static final long DEFAULT_DEVICE_DISCOVERY_TIMEOUT = 15 * 1000;

	private String TAG; 
	protected NewDataListenerThread mDataListenerThread;
	protected BluetoothAdapter mBluetoothAdapter;
	protected List<OnBluetoothDeviceConnectListener> mBluetoothDeviceConnectListeners = new ArrayList<OnBluetoothDeviceConnectListener>();
	protected List<BluetoothDevice> mDiscoveredDevices = new ArrayList<BluetoothDevice>();
	protected List<NewDataReceiveListner> mNewDataReceiverListenerList = Collections.synchronizedList(new ArrayList<NewDataReceiveListner>());
	


	
	public BaseBluetoothInteractionProvider(String tag) throws IllegalAccessException {
		TAG = tag;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null){
			throw new IllegalAccessException("The device doesn't suppor Bluetooth");
		}
		
	}
	
	public List<BluetoothDevice> discoverDevices(Context context){
		return discoverDevices(context, DEFAULT_DEVICE_DISCOVERY_TIMEOUT);	
	}
	
	public List<BluetoothDevice> discoverDevices(Context context, long timout){
		mDiscoveredDevices.clear();
		registerDiscoveryFoundReceiver(context);
		mBluetoothAdapter.startDiscovery();
		try {
			Thread.sleep(timout);
		} catch (InterruptedException e) {
		}
		mBluetoothAdapter.cancelDiscovery();
		unregisterDiscoveryFoundReceiver(context);
		return mDiscoveredDevices;
	}
	
	private void unregisterDiscoveryFoundReceiver(Context context) {
		context.unregisterReceiver(mDiscoveryFoundReceiver);
	}

	private void registerDiscoveryFoundReceiver(Context context) {
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(mDiscoveryFoundReceiver, filter);		
	}

	public List<BluetoothDevice> getPairedDevices(){
		List<BluetoothDevice> bluetoothDevicesList = new ArrayList<BluetoothDevice>(mBluetoothAdapter.getBondedDevices());	
		return bluetoothDevicesList;
	}
	
	public Intent getEnableBluetoothRequestIntent(){
		return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	}
	
	public Intent getEnableDiscoverablyModeIntent(){
		return new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	}
	
	public Intent getEnableDiscoverablyModeIntent(int discoverableDuration){
		Intent discoverableIntent = getEnableDiscoverablyModeIntent();
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, discoverableDuration);
		return discoverableIntent;
	}
	
	
	private BroadcastReceiver mDiscoveryFoundReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mDiscoveredDevices.add(device);
			}
		}
	};

	
	
	public void resgisterOnBTDeviceConnectListener(OnBluetoothDeviceConnectListener listener){
		mBluetoothDeviceConnectListeners.add(listener);
	}
	public void unresgisterOnBTDeviceConnectListener(OnBluetoothDeviceConnectListener listener){
		mBluetoothDeviceConnectListeners.remove(listener);
	}

	
	public static interface OnBluetoothDeviceConnectListener{
		public void onBluetoothDeviceConnected(BluetoothSocket bluetoothSocket);
	}


	public void notifyDeviceConnected(BluetoothSocket bluetoothSocket) {
		for(OnBluetoothDeviceConnectListener listener : mBluetoothDeviceConnectListeners){
			listener.onBluetoothDeviceConnected(bluetoothSocket);
		}
	}

	protected class NewDataListenerThread extends Thread{
		
		private List<BluetoothSocket> mBluetoothSocketList;

		public NewDataListenerThread(List<BluetoothSocket> bluetoothSocketList) {
			mBluetoothSocketList = bluetoothSocketList;
		}
		
		@Override
		public void run() {
//			checkAndWaitForConnection();
			List<BufferedReader> bufferedReaderList = new ArrayList<BufferedReader>();
			for(BluetoothSocket bluetoothSocket: mBluetoothSocketList){
				try {
					bufferedReaderList.add(new BufferedReader(new InputStreamReader(bluetoothSocket.getInputStream())));
				} catch (IOException e) {
					Log.e(TAG, "NewDataListenerThread:run(): " + e);
					return;
				}
			}
			
			while(!isInterrupted()){
				String newData;
				try {
					for(BufferedReader bufferedReader: bufferedReaderList){
						if(!bufferedReader.ready()){
							continue;
						}
						newData = bufferedReader.readLine();
						Log.i(TAG, "newData = " +newData);
						if(newData != null && !newData.trim().isEmpty()){
							notifyNewData(new JSONObject(newData));
						}
					}
				} catch (IOException e) {
					Log.e(TAG, "NewDataListenerThread:run(): " + e);
				} catch (JSONException e) {
					Log.e(TAG, "NewDataListenerThread:run(): " + e);
				}
			}			
		}

		private void checkAndWaitForConnection() {
			while(!isInterrupted()){
				boolean isAllSocetConnected = true;
				for(BluetoothSocket mBluetoothSocket : mBluetoothSocketList){
					if(!mBluetoothSocket.isConnected()){
						isAllSocetConnected = false;
						Log.i(TAG, "NewDataListenerThread: run() client socet is not connected yet. Waiting for connection.");
						break;
					}
				}
				if(isAllSocetConnected){
					break;
				}
			}
		}
	}
	
	protected void startListningData(List<BluetoothSocket> bluetoothSocketList) {
		stopListningData();
		mDataListenerThread = new NewDataListenerThread(bluetoothSocketList);
		mDataListenerThread.start();
	}
	
	public void stopListningData() {
		if(mDataListenerThread != null){
			mDataListenerThread.interrupt();
		}
		mDataListenerThread = null;
	}
	
	public void notifyNewData(JSONObject newDataJson) {
		for(NewDataReceiveListner onNewDataListner: mNewDataReceiverListenerList){
			onNewDataListner.onNewDataReceived(newDataJson);
		}
	}
	
	@Override
	public void registerNewDataReceiveListener(NewDataReceiveListner listner){
		mNewDataReceiverListenerList.add(listner);
	}

	@Override
	public void unregisterNewDataReceiveListener(NewDataReceiveListner listner){
		mNewDataReceiverListenerList.remove(listner);
	}


}
