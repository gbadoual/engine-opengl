package com.android.opengl.view.screen;

import com.android.opengl.Camera;
import com.android.opengl.view.control.GLTextView;

public class BTMultyScreen extends GLScreen{

	public BTMultyScreen(Camera camera) {
		super(camera);
	}

	@Override
	public void onCreate() {
		GLTextView glStartSingleGameView = new GLTextView(mCamera);
		glStartSingleGameView.setText("Hi there! It is empty yet");
		glStartSingleGameView.showBackground(true);
		addChild(glStartSingleGameView);
	}

	@Override
	public void onDestroy() {
				
	}

}
