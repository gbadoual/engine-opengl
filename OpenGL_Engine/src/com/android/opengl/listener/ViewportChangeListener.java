package com.android.opengl.listener;

import com.android.opengl.util.geometry.Rect2D;

public interface ViewportChangeListener extends BaseListener<Rect2D>{
	public void onViewportChanged(Rect2D newViewportRect);
}
