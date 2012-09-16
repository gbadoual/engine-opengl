package com.android.opengl.view.control;

import android.util.Log;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.Scene;

public class GLGridLayout extends GLLayout{
	

	// dimensions are represented in percents from the largest side of the screen
	private float horizantalSpacing = 2;
	private float verticalSpacing = 2;

	private float nextX;
	private float nextY = verticalSpacing;
	private float prevRowHeight;
	
 	public GLGridLayout(Scene scene, float left, float top, float width, float height) {
		super(scene, left, top, width, height);
		init();
	}


	public GLGridLayout(Scene scene) {
		super(scene);
		init();
	}


	public GLGridLayout(Camera camera) {
		super(camera);
		init();
	}
	
	
	public GLGridLayout(Camera camera, float left, float top, float width, float height) {
		super(camera, left, top, width, height);
		init();
	}

	private void init(){
		initLayoutParams();
	}

	private void initLayoutParams(){
		nextX = 0;
		nextY = verticalSpacing;
		prevRowHeight = 0;	
	}

	@Override
	public void addChild(GLView child) {
		super.addChild(child);
		nextX += getHorizantalSpacing();
		float newLayoutX;
		float newLayoutY;
		if (nextX + child.mWidth >= mWidth){
			nextX = getHorizantalSpacing();

			if(mChildren.size() > 1){
				nextY += prevRowHeight + getVerticalSpacing();
			}
			newLayoutX = nextX;
			newLayoutY = nextY;
			prevRowHeight = child.mHeight;
		} else{
			newLayoutX = nextX;
			newLayoutY = nextY;
			
			prevRowHeight = Math.max(prevRowHeight, child.mHeight);
		}
		if(nextY + child.mHeight > mHeight){
			onMeasure(Math.max(mWidth, 2 * getHorizantalSpacing() + child.mWidth), nextY + getVerticalSpacing() + child.mHeight);
		}
		Log.i("tag", "layout left/bottom = " + newLayoutX + "/" + newLayoutY);
		Log.i("tag", "layout child.width = " + child.mWidth);
		
		nextX += child.mWidth;
		child.onLayout(newLayoutX, newLayoutY);
	}
	
	@Override
	public void removeChildren() {
		super.removeChildren();
		onMeasure(mWidth, 1);
		initLayoutParams();
	}



	public float getVerticalSpacing() {
		return verticalSpacing;
	}

	public float getHorizantalSpacing() {
		return horizantalSpacing;
	}

	public void setSpacing(float horizontalSpacing, float verticalSpacing){
		this.horizantalSpacing = horizontalSpacing;
		this.verticalSpacing = verticalSpacing;
	}



}
