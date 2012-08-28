package com.android.opengl.view.control;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.PositionChangeListener;
import com.android.opengl.util.geometry.Matrix;

public class GLHealthBar extends GLView{
	
	private GameObject gameObject;
	float[] mvpMatrix = new float[16];
	

	public GLHealthBar(GameObject gameObject) {
		super(gameObject.getParentScene());
		this.gameObject = gameObject;
//		gameObject.registerPositionListener(this);
		onMeasure(10, 3);
	}
	
	@Override
	public void onDraw() {
		Matrix.multiplyMM(mvpMatrix, 0, gameObject.getParentScene().getMVPMatrix(), 0, 
				gameObject.getModelMatrix(), 0);
		
		super.onDraw();
	}
	
	
//	@Override
//	public void onPositionChanged(float x, float y, float z) {
//		onLayout(50, 10);
//		
//	}

}
