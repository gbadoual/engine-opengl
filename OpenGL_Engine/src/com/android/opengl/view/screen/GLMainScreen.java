package com.android.opengl.view.screen;

import com.android.opengl.Camera;
import com.android.opengl.view.EngineRenderer;
import com.android.opengl.view.control.GLTextView;
import com.android.opengl.view.control.GLView;

public class GLMainScreen extends GLScreen{

	public GLMainScreen(Camera camera) {
		super(camera);
	}

	@Override
	protected void onCreate() {
		GLTextView glStartSingleGameView = new GLTextView(mCamera);
		glStartSingleGameView.setText("Start single game");
		glStartSingleGameView.setOnTapListener(mOnStartSingleGameListener);
		glStartSingleGameView.showBackground(true);
		GLTextView glStartMultiGameBTView = new GLTextView(mCamera);
		glStartMultiGameBTView.setPosition(0, 10);
		glStartMultiGameBTView.showBackground(true);
		glStartMultiGameBTView.setText("Start multi game via Bluetooth");
		glStartMultiGameBTView.setOnTapListener(mOnStartMultuGameBTListener);
		addChild(glStartSingleGameView);
		addChild(glStartMultiGameBTView);
	}

	private OnTapListener mOnStartSingleGameListener = new OnTapListener() {
		
		@Override
		public void onTap(GLView glView) {
			EngineRenderer engineRenderer = mCamera.getEngineRenderer();
			engineRenderer.setEngineState(engineRenderer.getGameInProgressState());
			engineRenderer.getCurrentEngineState().startGame();
		}
		
		@Override
		public void onLongTap(GLView glView) {
			// TODO Auto-generated method stub
			
		}
	};
	private OnTapListener mOnStartMultuGameBTListener = new OnTapListener() {
		
		@Override
		public void onTap(GLView glView) {
			getGlScreenContext().launch(new BTMultyScreen(mCamera));			
		}
		
		@Override
		public void onLongTap(GLView glView) {
			
		}
	};

}
