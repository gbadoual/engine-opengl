package com.android.opengl.view.control;

import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.GLScene;

public abstract class GLLayout extends GLView{


	
	public GLLayout(GLScene scene, float left, float top, float width, float height) {
		super(scene, left, top, width, height);
	}

	public GLLayout(GLScene scene) {
		super(scene);
	}

	public GLLayout(Camera camera) {
		super(camera);
	}

	public GLLayout(Camera camera, float left, float top, float width, float height) {
		super(camera, left, top, width, height);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean res = super.onTouchEvent(event);
		if(mFocusedView == this){
			mFocusedView = null;
		}
		return res;
	}

}
