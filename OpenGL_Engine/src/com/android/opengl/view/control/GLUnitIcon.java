package com.android.opengl.view.control;

import com.android.opengl.gameobject.GameObject;

public class GLUnitIcon extends GLView{

	private GameObject mGameObject;
	
	public GLUnitIcon(GameObject gameObject) {
		super(gameObject.getParentScene().getCamera());
		onMeasure(6, 6);
		setBackground(gameObject.getUnitIconResId());
		setmGameObject(gameObject);
	}

	public GameObject getGameObject() {
		return mGameObject;
	}

	public void setmGameObject(GameObject mGameObject) {
		this.mGameObject = mGameObject;
	}

	

}
