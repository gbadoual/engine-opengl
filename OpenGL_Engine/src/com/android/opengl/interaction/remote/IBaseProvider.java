package com.android.opengl.interaction.remote;

import org.json.JSONObject;

public interface IBaseProvider {

	public void registerNewDataReceiveListener(NewDataReceiveListner listner);
	public void unregisterNewDataReceiveListener(NewDataReceiveListner listner);
	public void sendData(JSONObject jsonObject);
	
	public static interface NewDataReceiveListner{
		public void onNewDataReceived(JSONObject newDataJson);
	}

}
