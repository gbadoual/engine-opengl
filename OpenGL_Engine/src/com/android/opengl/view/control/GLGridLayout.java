package com.android.opengl.view.control;

import android.util.Log;

import com.android.opengl.Camera;

public class GLGridLayout extends GLLayout{
	
	
	private float horizantalSpacing = 2;
	private float verticalSpacing = 2;

	private float nextX;
	private float nextY = verticalSpacing;
	private float prevRowHeight;
	
 	public GLGridLayout(Camera camera) {
		super(camera);
		initLocal();
	}
 	
 	
	
	private void initLocal() {
		setColor(128, 142, 128, 192);
	}



	public GLGridLayout(Camera camera, float left, float top, float width, float height) {
		super(camera, left, top, width, height);
		initLocal();
	}



	@Override
	public void addChild(GLView child) {
		super.addChild(child);
		nextX += getHorizantalSpacing();
		float newLayoutX;
		float newLayoutY;
		if (nextX + child.mWidth >= mWidth){
			nextX = getHorizantalSpacing();

			nextY += prevRowHeight + getVerticalSpacing();
			newLayoutX = nextX;
			newLayoutY = nextY;
			prevRowHeight = child.mHeight;
			if(nextY + child.mHeight > mHeight){
				onMeasure(Math.max(mWidth, 2 * getHorizantalSpacing() + child.mWidth), nextY + getVerticalSpacing() + child.mHeight);
			}
		} else{
			newLayoutX = nextX;
			newLayoutY = nextY;
			
			prevRowHeight = Math.max(prevRowHeight, child.mHeight);
		}
		Log.i("tag", "layout left/bottom = " + newLayoutX + "/" + newLayoutY);
		Log.i("tag", "layout child.width = " + child.mWidth);
		
		nextX += child.mWidth;
		child.onLayout(newLayoutX, newLayoutY);
	}



	public float getVerticalSpacing() {
		return verticalSpacing;
	}

	public void setVerticalSpacing(float verticalSpacing) {
		this.verticalSpacing = verticalSpacing;
	}

	public float getHorizantalSpacing() {
		return horizantalSpacing;
	}

	public void setHorizantalSpacing(float horizantalSpacing) {
		this.horizantalSpacing = horizantalSpacing;
	}

}
