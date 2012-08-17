package com.android.opengl.util.geometry;

public class Rect2D {
	public float mLeftCoord;
	public float mTopCoord;
	public float mWidth;
	public float mHeight;
	
	
	
	
	public Rect2D(float leftCoord, float topCoord, float width, float height) {
		super();
		this.mLeftCoord = leftCoord;
		this.mTopCoord = topCoord;
		this.mWidth = width;
		this.mHeight = height;
	}

	public Rect2D() {
	}

	public float getRightCoord(){
		return mLeftCoord + mWidth;
	}
	
	public float getBottomCoord() {
		return mTopCoord + mHeight;
	}
	
	public boolean isWithinRect(float x, float y){
		return x >= mLeftCoord && x <= mLeftCoord + mWidth &&
				y >= mTopCoord && y <= mTopCoord + mHeight;
	}


}