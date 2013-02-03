package com.android.opengl.view.control;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.GLScene;


public class GLButton extends GLView{
	
	
	

	public GLButton(GLScene scene) {
		super(scene);

	}

	public GLButton(GLScene scene, float left, float top, float width, float height) {
		super(scene, left, top, width, height);
	}
	
	public GLButton(Camera camera) {
		super(camera);

	}

	public GLButton(Camera camera, float left, float top, float width, float height) {
		super(camera, left, top, width, height);
	}
	
	
	


	

}
