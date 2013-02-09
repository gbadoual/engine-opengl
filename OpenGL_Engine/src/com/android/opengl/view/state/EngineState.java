package com.android.opengl.view.state;

import android.util.Log;
import android.view.MotionEvent;

import com.android.opengl.view.EngineRenderer;

public abstract class EngineState implements BaseState{
	
	private static final String TAG = EngineState.class.getSimpleName();
	protected final EngineRenderer mEngineRenderer;
	
	
	public EngineState(EngineRenderer engineRenderer) {
		mEngineRenderer = engineRenderer;
	}
	
	
	public void showLogo(){
		Log.i(TAG, "showLogo() is not implemented");
	};
	
	public void showMainScreen(){
		Log.i(TAG, "showMainScreen() is not implemented");
	};
	public void showMenu(){
		Log.i(TAG, "showMenu() is not implemented");
	};
	public void startGame(){
		Log.i(TAG, "startGame() is not implemented");
	};
	public void pauseGame(){
		Log.i(TAG, "pauseGame() is not implemented");
	};

	abstract public void onWorldUpdate();
	abstract public void onDrawFrame();

	public boolean onTouchEvent(MotionEvent event){
		return false;
	};

}
