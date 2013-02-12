package com.android.opengl.view.state;

import android.app.Activity;

import com.android.opengl.view.EngineRenderer;
import com.android.opengl.view.screen.GLMainScreen;
import com.android.opengl.view.screen.GLScreen;

public class MainScreenState extends EngineState{

//	private GLTextView glStartSingleGameView;
//	private GLTextView glStartMultiGameBTView;
	private Activity mActivity;
	
	
	public MainScreenState(EngineRenderer engineRenderer, Activity activity) {
		super(engineRenderer);
		mActivity = activity;
	}
	
	@Override
	public void showMainScreen() {
		super.showMainScreen();
		GLScreen glScreen = new GLMainScreen(mEngineRenderer.getCamera());
		launch(glScreen);
		
	}


	@Override
	public void onWorldUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDrawFrame() {
		mGLScreenStack.onDrawFrame();
	}

	@Override
	public boolean onBackPressed() {
		return mGLScreenStack.onBackPressed();
	}

	public Activity getActivity() {
		return mActivity;
	}

}
