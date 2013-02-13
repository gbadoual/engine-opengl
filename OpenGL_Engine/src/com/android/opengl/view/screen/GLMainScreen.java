package com.android.opengl.view.screen;

import com.android.opengl.Camera;
import com.android.opengl.view.EngineRenderer;
import com.android.opengl.view.control.GLLayout.Orientation;
import com.android.opengl.view.control.GLLinearLayout;
import com.android.opengl.view.control.GLTextView;
import com.android.opengl.view.control.GLView;

public class GLMainScreen extends GLScreen{

	
	public GLMainScreen(Camera camera) {
		super(camera);
	}

	@Override
	protected void onCreate() {
		initView();
	}

	private void initView() {
		final GLLinearLayout glLinearLayout = new GLLinearLayout(mCamera);
		glLinearLayout.setOrientation(Orientation.VERTICAL);

		GLTextView glStartSingleGameView = new GLTextView(mCamera);
		glStartSingleGameView.setText("Start single game");
		glStartSingleGameView.setOnTapListener(mOnStartSingleGameListener);
		glStartSingleGameView.showBackground(true);
		GLTextView glStartMultiGameBTView = new GLTextView(mCamera);
		glStartMultiGameBTView.showBackground(true);
		glStartMultiGameBTView.setText("Start multi game via Bluetooth");
		glStartMultiGameBTView.setOnTapListener(mOnStartMultuGameBTListener);
		glLinearLayout.addChild(glStartSingleGameView);
		glLinearLayout.addChild(glStartMultiGameBTView);
		addChild(glLinearLayout);
	}

	private OnTapListener mOnStartSingleGameListener = new OnTapListener() {
		
		@Override
		public void onTap(GLView glView) {
			EngineRenderer engineRenderer = mCamera.getEngineRenderer();
			engineRenderer.setEngineState(engineRenderer.getGameInProgressState());
			engineRenderer.getCurrentEngineState().startGame();
		}

	};
	private OnTapListener mOnStartMultuGameBTListener = new OnTapListener() {
		
		@Override
		public void onTap(GLView glView) {
			getGlScreenContext().launch(new BTMultyScreen(mCamera));			
		}

	};

}
