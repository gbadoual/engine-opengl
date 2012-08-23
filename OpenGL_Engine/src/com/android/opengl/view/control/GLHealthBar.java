package com.android.opengl.view.control;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.PositionChangeListener;

public class GLHealthBar extends GLView implements PositionChangeListener{
	
	private GameObject gameObject;
	

	public GLHealthBar(GameObject gameObject) {
		super(gameObject.getParentScene());
		this.gameObject = gameObject;
		gameObject.registerPositionListener(this);
		onMeasure(10, 3);
	}

	
	
	@Override
	public void onPositionChanged(float x, float y, float z) {
		onLayout(50, 10);
		
	}

}
