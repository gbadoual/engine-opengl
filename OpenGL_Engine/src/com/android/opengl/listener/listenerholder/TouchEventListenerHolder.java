package com.android.opengl.listener.listenerholder;

import android.view.MotionEvent;

import com.android.opengl.listener.TouchEventListener;

public class TouchEventListenerHolder extends BaseListenerHolder<TouchEventListener, MotionEvent>{

	@Override
	public void notifyListeners(MotionEvent event) {
		for(TouchEventListener listener: mListenerList ){
			listener.onTouchEvent(event);
		}		
	}

}
