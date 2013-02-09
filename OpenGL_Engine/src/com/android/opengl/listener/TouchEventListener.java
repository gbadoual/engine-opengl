package com.android.opengl.listener;

import android.view.MotionEvent;

public interface TouchEventListener extends BaseListener<MotionEvent>{
	public boolean onTouchEvent(MotionEvent event);

}
