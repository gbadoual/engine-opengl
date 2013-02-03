package com.android.opengl.view.state;

import android.opengl.GLES20;
import android.util.Log;

import com.android.opengl.view.EngineRenderer;
import com.android.opengl.view.WorldView;

public class LoadingLevelState extends EngineState{

	protected static final String TAG = LoadingLevelState.class.getSimpleName();

	
	public LoadingLevelState(EngineRenderer engineRenderer) {
		super(engineRenderer);
	}

	@Override
	public void loadLevel() {
		long time = System.currentTimeMillis();
		mEngineRenderer.getCamera().initControls();

		mEngineRenderer.getCallbackHandler().removeMessages(WorldView.DIALOG_LOADING_SHOW);
		mEngineRenderer.getCallbackHandler().sendEmptyMessage(WorldView.DIALOG_LOADING_SHOW);
		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
//		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		mEngineRenderer.initGameObjects();
		mEngineRenderer.initFpsCount();
		mEngineRenderer.getCallbackHandler().sendEmptyMessage(WorldView.DIALOG_LOADING_DISMISS);

		time = System.currentTimeMillis() - time;
		Log.i("tag", "world loaded for " + time / 1000.0f + " sec.");
		
		mEngineRenderer.setEngineState(mEngineRenderer.getGameInProgressState());
	}

	@Override
	public void onDrawFrame() {
	}

	@Override
	public void onWorldUpdate() {
		
	}
	
	

}
