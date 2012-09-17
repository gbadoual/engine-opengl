package com.android.opengl.view.control;

import com.android.opengl.gameobject.GameObject;

public class GLUnitIcon extends GLView{

	public GLUnitIcon(GameObject gameObject) {
		super(gameObject.getParentScene().getCamera());
		onMeasure(6, 6);
		setBackground(gameObject.getUnitIconResId());
	}
	

}
