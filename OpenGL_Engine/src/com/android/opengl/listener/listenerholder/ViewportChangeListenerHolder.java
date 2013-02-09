package com.android.opengl.listener.listenerholder;

import com.android.opengl.listener.ViewportChangeListener;
import com.android.opengl.util.geometry.Rect2D;

public class ViewportChangeListenerHolder extends BaseListenerHolder<ViewportChangeListener, Rect2D>{

	@Override
	public void notifyListeners(Rect2D event) {
		for(ViewportChangeListener listener: mListenerList){
			listener.onViewportChanged(event);	
		}
		
	}

}
