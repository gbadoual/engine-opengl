package com.android.opengl.gameobject.building;

import com.android.opengl.R;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.view.control.GLButton;
import com.android.opengl.view.control.GLView;

public class MainBase extends AbstractBuilding{
	
	private GLView glView;

	
	public MainBase(Scene parentScene) {
		super(parentScene);
		glView = new GLButton();
	}


	@Override
	public int getMeshResource() {
		// TODO Auto-generated method stub
		return R.raw.twisted_cube;
	}
	
	@Override
	public void drawFrame() {
		super.drawFrame();
		glView.draw();
	}
	
	@Override
	public void onObjectTap() {
		
	}


}
