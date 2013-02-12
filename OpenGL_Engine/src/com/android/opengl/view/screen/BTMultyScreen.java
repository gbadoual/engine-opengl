package com.android.opengl.view.screen;

import android.app.Activity;

import com.android.opengl.Camera;
import com.android.opengl.view.control.GLLayout.Orientation;
import com.android.opengl.view.control.GLLinearLayout;
import com.android.opengl.view.control.GLTextView;
import com.android.opengl.view.control.GLView;

public class BTMultyScreen extends GLScreen{

	public Activity mActivity;
	
	public BTMultyScreen(Camera camera) {
		super(camera);
	}

	@Override
	protected void onCreate() {
		final GLLinearLayout glLinearLayout = new GLLinearLayout(mCamera);
		glLinearLayout.setOrientation(Orientation.VERTICAL);
		GLTextView glCreateServerGameView = new GLTextView(mCamera);
		glCreateServerGameView.setText("Create new game");
		glCreateServerGameView.showBackground(true);
		glCreateServerGameView.setOnTapListener(mOnCreateGameTapListener);
		
		GLTextView glJoinGameView = new GLTextView(mCamera);
		glJoinGameView.setText("Join the Game");
		glJoinGameView.showBackground(true);
		glJoinGameView.setOnTapListener(new OnTapListener() {
			
			@Override
			public void onTap(GLView glView) {
				glLinearLayout.setOrientation(Orientation.HORIZONTAL);
			}

		});
		
		
		glLinearLayout.addChild(glCreateServerGameView);
		glLinearLayout.addChild(glJoinGameView);
				
		addChild(glLinearLayout);
	}

	private OnTapListener mOnCreateGameTapListener = new OnTapListener() {
		
		@Override
		public void onTap(GLView glView) {
			
			
		}
	};
}
