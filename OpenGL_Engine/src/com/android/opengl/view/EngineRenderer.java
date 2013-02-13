package com.android.opengl.view;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.CommonGameObject;
import com.android.opengl.listener.TouchEventListener;
import com.android.opengl.view.state.GameState;
import com.android.opengl.view.state.SettingGameState;
import com.android.opengl.view.state.StartedGameState;

public class EngineRenderer implements Renderer, TouchEventListener{

	private GameState mCurrentEngineState;
	
	private StartedGameState mGameInProgressState;
	private SettingGameState mMainScreenState;
	
	private Camera mCamera;
	
	private long currentFrame = 0;
	private Handler callbackHandler;


	private int fps;
	private long prevTime;
	private WorldView worldView;
	
	/*
	 * Called on UI thread of main application
	 */

	public EngineRenderer(WorldView worldView, Handler handler) {
		this.worldView = worldView;
		this.callbackHandler = handler;
//		this.mViewBoundaries = new Rect2D(worldView.getLeft(), worldView.getTop(), worldView.getWidth(), worldView.getHeight());
		initStates();
	}


	private void initStates() {
		mGameInProgressState = new StartedGameState(this);
		mMainScreenState = new SettingGameState(this);
	}

	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		mCamera = new Camera(worldView);
		mCamera.getTouchEventListenerHolder().registerListener(this);
//		mCurrentEngineState = mLoadingLevelState;
//		mCurrentEngineState.loadLevel();
		mCurrentEngineState = mMainScreenState;
		mCurrentEngineState.showMainScreen();

	}


	@Override
	public void onSurfaceChanged(GL10 arg0, int width, int height) {
		mCamera.setViewport(width, height);
	}

	@Override
	public void onDrawFrame(GL10 arg0) {
		gameLoopStep();
	}
	

	private void gameLoopStep() {
		clearScreen();
		mCurrentEngineState.onWorldUpdate();
		mCurrentEngineState.onDrawFrame();
		countFPS();
	}


	public void initFpsCount() {
		prevTime = SystemClock.uptimeMillis();
	}






	private void clearScreen() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	}

	public void release() {
		if(mCamera != null){
			mCamera.release();				
		} 
	}

	private void countFPS() {
		currentFrame++;
		if(currentFrame <0){currentFrame = 0;}
		fps++;
		if(SystemClock.uptimeMillis() - prevTime>= 1000){
//			Log.i("tag", "fps = "+fps);
			worldView.updateFPS(fps, CommonGameObject.facesCount);
			fps = 0;
			prevTime = SystemClock.uptimeMillis();			
		}
	}

	





		public Context getContext() {
			return worldView.getContext();
		}


		@Override
		public boolean onTouchEvent(MotionEvent event) {
			return mCurrentEngineState.onTouchEvent(event);
		}
		


		public GameState getCurrentEngineState() {
			return mCurrentEngineState;
		}


		public void setEngineState(GameState engineState) {
			this.mCurrentEngineState = engineState;
		}

		public StartedGameState getGameInProgressState() {
			return mGameInProgressState;
		}


		public void setGameInProgressState(StartedGameState gameInProgressState) {
			this.mGameInProgressState = gameInProgressState;
		}


		public Handler getCallbackHandler() {
			return callbackHandler;
		}


		public void setCallbackHandler(Handler callbackHandler) {
			this.callbackHandler = callbackHandler;
		}



		public Camera getCamera() {
			return mCamera;
		}


		public boolean onBackPressed() {
			return mCurrentEngineState.onBackPressed();
		}


		public Activity getActivity() {
			return (Activity)worldView.getContext();
		}


	
}
