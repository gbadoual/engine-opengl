package com.android.opengl.view.state;

import android.app.Activity;

import com.android.opengl.view.EngineRenderer;
import com.android.opengl.view.control.GLTextView;
import com.android.opengl.view.control.GLView;
import com.android.opengl.view.control.GLView.OnTapListener;

public class MainScreenState extends EngineState{

	private GLTextView glStartSingleGameView;
	private GLTextView glStartMultiGameBTView;
	private OnTapListener mOnStartSingleGameListener = new OnTapListener() {
		
		@Override
		public void onTap(GLView glView) {
			mEngineRenderer.setEngineState(mEngineRenderer.getLoadingLevelState());
			mEngineRenderer.getEngineState().loadLevel();
		}
		
		@Override
		public void onLongTap(GLView glView) {
			// TODO Auto-generated method stub
			
		}
	};
	private OnTapListener mOnStartMultuGameBTListener = new OnTapListener() {
		
		@Override
		public void onTap(GLView glView) {
			mEngineRenderer.setEngineState(mEngineRenderer.getLoadingLevelState());
			mEngineRenderer.getEngineState().loadLevel();
		}
		
		@Override
		public void onLongTap(GLView glView) {
			// TODO Auto-generated method stub
			
		}
	};
	private Activity mActivity;
	
	
	public MainScreenState(EngineRenderer engineRenderer, Activity activity) {
		super(engineRenderer);
		mActivity = activity;
	}
	
	@Override
	public void showMainScreen() {
		super.showMainScreen();
		glStartSingleGameView = new GLTextView(mEngineRenderer.getCamera());
		glStartSingleGameView.setText("Start single game");
		glStartSingleGameView.setOnTapListener(mOnStartSingleGameListener);
		glStartSingleGameView.showBackground(true);
		glStartMultiGameBTView = new GLTextView(mEngineRenderer.getCamera());
		glStartMultiGameBTView.setPosition(0, 10);
		glStartMultiGameBTView.showBackground(true);
		glStartMultiGameBTView.setText("Start multi game via Bluetooth");
		glStartMultiGameBTView.setOnTapListener(mOnStartMultuGameBTListener);
	}

	@Override
	public void onWorldUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDrawFrame() {
		glStartSingleGameView.onDrawFrame();	
		glStartMultiGameBTView.onDrawFrame();
	}

}
