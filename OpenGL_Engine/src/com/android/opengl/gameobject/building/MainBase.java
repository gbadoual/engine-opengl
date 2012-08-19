package com.android.opengl.gameobject.building;

import android.util.Log;

import com.android.opengl.R;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.view.control.GLButton;
import com.android.opengl.view.control.GLGridLayout;
import com.android.opengl.view.control.GLView;

public class MainBase extends AbstractBuilding{
	
	
	
	private GLGridLayout glGridLayout;

	
	public MainBase(Scene parentScene) {
		super(parentScene);
		initView();
		parentScene.registerGLView(glGridLayout, 50);
		glGridLayout.setVisible(isSelected);
	}


	private void initView() {
		glGridLayout = new GLGridLayout(parentScene.getCamera(), 5, 10, 30, 0);
		
				
		GLView child = new GLButton(parentScene.getCamera(), 50, 20, 30, 10);
		GLView child1 = new GLButton(parentScene.getCamera(), 5, 2, 10, 10);
		GLView child2 = new GLButton(parentScene.getCamera(), 5, 2, 10, 10);
		GLView child3 = new GLButton(parentScene.getCamera(), 5, 2, 10, 5);
		GLView child4 = new GLButton(parentScene.getCamera(), 5, 2, 10, 5);
		GLView child5 = new GLButton(parentScene.getCamera(), 5, 2, 10, 5);
		GLView child6 = new GLButton(parentScene.getCamera(), 5, 2, 10, 5);
		child.setBackground(R.raw.bubble_background);
		child.setOnTapListener(new GLView.OnTapListener() {
			
			@Override
			public void onTap(GLView glView) {
				Log.i("tag", "another glview tapped: " + glView);
			}
		});
		glGridLayout.addChild(child1);
		glGridLayout.addChild(child2);
		glGridLayout.addChild(child3);
		glGridLayout.addChild(child4);
		glGridLayout.addChild(child5);
		glGridLayout.addChild(child6);
		glGridLayout.addChild(child);
		glGridLayout.setOnTapListener(new GLView.OnTapListener() {
			
			@Override
			public void onTap(GLView glView) {
				Log.i("tag", "glview tapped: " + glView);
			}
		});
	}


	@Override
	public int getMeshResource() {
		return R.raw.twisted_cube;
	}
	
	@Override
	public void drawFrame() {
		super.drawFrame();
	}
	
	@Override
	public void onObjectFocusChanged(boolean hasFocus) {
		glGridLayout.setVisible(hasFocus);						
	}
	
	@Override
	public void release() {
		super.release();
		parentScene.unregisterGLView(glGridLayout);
	}


}
