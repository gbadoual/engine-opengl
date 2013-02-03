package com.android.opengl.interaction;

import org.json.JSONObject;

public interface BaseInteractionProvider {

	public void registerNewDataReceiveListener(NewDataReceiveListner listner);
	public void unregisterNewDataReceiveListener(NewDataReceiveListner listner);
	public void sendData(JSONObject jsonObject);
	
	public static interface NewDataReceiveListner{
		public void onNewDataReceived(JSONObject newDataJson);
	}

}
