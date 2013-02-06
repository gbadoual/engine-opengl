package com.android.opengl.view;

import com.android.opengl.util.geometry.Rect2D;

import android.view.MotionEvent;

public interface Touchable extends Renderable{

	public boolean onTouchEvent(MotionEvent event);
	public Rect2D getBoundariesRectInPixel();
	
}
