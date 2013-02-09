package com.android.opengl.listener.listenerholder;

import java.util.ArrayList;
import java.util.List;

import com.android.opengl.listener.BaseListener;

public abstract class BaseListenerHolder<T extends BaseListener<V>, V> {
	
	protected List<T> mListenerList = new ArrayList<T>();
	
	public void registerListener(T listener){
		mListenerList.add(listener);
	}
	public void unregisterListener(T listener){
		mListenerList.remove(listener);
	}
	
	public void clear(){	
		mListenerList.clear();
	}
	
	public abstract void notifyListeners(V event);
}
