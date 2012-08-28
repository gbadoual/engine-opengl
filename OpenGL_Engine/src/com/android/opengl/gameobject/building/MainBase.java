package com.android.opengl.gameobject.building;

import android.util.Log;

import com.android.opengl.R;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.gameobject.unit.Cube;
import com.android.opengl.gameobject.unit.Earth;
import com.android.opengl.view.control.GLButton;
import com.android.opengl.view.control.GLGridLayout;
import com.android.opengl.view.control.GLView;

public class MainBase extends AbstractBuilding{
	
	
	
	private GLGridLayout mainPanleLayout;

	
	public MainBase(Scene parentScene) {
		super(parentScene);
		initView();
//		parentScene.registerGLView(mainPanleLayout, 50);
		mainPanleLayout.setzOrder(50);
		mainPanleLayout.setVisible(isSelected);
		
	}


	private void initView() {
		mainPanleLayout = new GLGridLayout(parentScene, 5, 10, 30, 0);
		mainPanleLayout.setColor(128, 150, 128, 192);
				
		GLView child = new GLButton(parentScene, 50, 20, 30, 10);
		GLView child1 = new GLButton(parentScene, 5, 2, 10, 10);
		child1.setBackground(R.raw.icon_cube);
		child1.setOnTapListener(new GLView.OnTapListener() {
			
			@Override
			public void onTap(GLView glView) {
				Log.i("tag", "another glview tapped: " + glView);
				Cube cube = new Cube(parentScene);
				initBuildedObject(cube);
			}
		});
		GLView child2 = new GLButton(parentScene, 5, 2, 10, 10);
		GLView child3 = new GLButton(parentScene, 5, 2, 10, 5);
		GLView child4 = new GLButton(parentScene, 5, 2, 10, 5);
		GLView child5 = new GLButton(parentScene, 5, 2, 10, 5);
		GLView child6 = new GLButton(parentScene, 5, 2, 10, 5);
		child2.setBackground(R.raw.icon_earth);
		child2.setOnTapListener(new GLView.OnTapListener() {
			
			@Override
			public void onTap(GLView glView) {
				Log.i("tag", "another glview tapped: " + glView);
				Earth earth = new Earth(parentScene);
				initBuildedObject(earth);
			}
		});
		mainPanleLayout.addChild(child1);
		mainPanleLayout.addChild(child2);
		mainPanleLayout.addChild(child3);
		mainPanleLayout.addChild(child4);
		mainPanleLayout.addChild(child5);
		mainPanleLayout.addChild(child6);
		mainPanleLayout.addChild(child);
		mainPanleLayout.setOnTapListener(new GLView.OnTapListener() {
			
			@Override
			public void onTap(GLView glView) {
				Log.i("tag", "glview tapped: " + glView);
			}
		});
	}


	@Override
	public int getMeshResource() {
		return R.raw.main_base;
	}
	@Override
	public int getTextureResource() {
		return R.raw.skydome_texture_small;
	}
	
	@Override
	public void onDrawFrame() {
		super.onDrawFrame();
	}
	
	@Override
	public void onObjectFocusChanged(boolean hasFocus) {
		mainPanleLayout.setVisible(hasFocus);						
	}
	
	@Override
	public void release() {
		mainPanleLayout.release();
		super.release();
//		TODO maybe this line related to CuncurrentModificationException in Scene.onDraw()
//		parentScene.unregisterGLView(mainPanleLayout);
		
	}


}
