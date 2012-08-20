package com.android.opengl.gameobject.building;

import java.util.Random;

import android.util.Log;

import com.android.opengl.R;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.gameobject.unit.Cube;
import com.android.opengl.gameobject.unit.Earth;
import com.android.opengl.util.geometry.Point3D;
import com.android.opengl.view.control.GLButton;
import com.android.opengl.view.control.GLGridLayout;
import com.android.opengl.view.control.GLView;

public class MainBase extends AbstractBuilding{
	
	
	
	private GLGridLayout mainPanleLayout;

	
	public MainBase(Scene parentScene) {
		super(parentScene);
		initView();
		parentScene.registerGLView(mainPanleLayout, 50);
		mainPanleLayout.setVisible(isSelected);
	}


	private Random r = new Random();
	private void initView() {
		mainPanleLayout = new GLGridLayout(parentScene, 5, 10, 30, 0);
		
				
		GLView child = new GLButton(parentScene, 50, 20, 30, 10);
		GLView child1 = new GLButton(parentScene, 5, 2, 10, 10);
		final float distance = 80;
		child1.setOnTapListener(new GLView.OnTapListener() {
			
			@Override
			public void onTap(GLView glView) {
				Log.i("tag", "another glview tapped: " + glView);
				Earth earth = new Earth(parentScene);
				float x = r.nextFloat() * distance - distance/2;
				float z = r.nextFloat() * distance - distance/2;
				float y = parentScene.getAltitude(x, z);
				earth.getMovingTool().moveTo(new Point3D(x, y, z));
			}
		});
		GLView child2 = new GLButton(parentScene, 5, 2, 10, 10);
		GLView child3 = new GLButton(parentScene, 5, 2, 10, 5);
		GLView child4 = new GLButton(parentScene, 5, 2, 10, 5);
		GLView child5 = new GLButton(parentScene, 5, 2, 10, 5);
		GLView child6 = new GLButton(parentScene, 5, 2, 10, 5);
		child.setBackground(R.raw.bubble_background);
		child.setOnTapListener(new GLView.OnTapListener() {
			
			@Override
			public void onTap(GLView glView) {
				Log.i("tag", "another glview tapped: " + glView);
				Cube cube = new Cube(parentScene);
				float x = r.nextFloat() * distance - distance/2;
				float z = r.nextFloat() * distance - distance/2;
				float y = parentScene.getAltitude(x, z);
				cube.getMovingTool().moveTo(new Point3D(x, y, z));
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
		return R.raw.twisted_cube;
	}
	
	@Override
	public void drawFrame() {
		super.drawFrame();
	}
	
	@Override
	public void onObjectFocusChanged(boolean hasFocus) {
		mainPanleLayout.setVisible(hasFocus);						
	}
	
	@Override
	public void release() {
		super.release();
		parentScene.unregisterGLView(mainPanleLayout);
	}


}
