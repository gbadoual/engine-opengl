package com.android.opengl.view.control;

import android.opengl.GLES20;
import android.opengl.GLU;
import android.util.Log;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.PositionChangeListener;
import com.android.opengl.shader.GLViewShader;
import com.android.opengl.util.geometry.Matrix;

public class GLHealthBar extends GLView{
	
	private GameObject gameObject;
	float[] mvMatrix = new float[16];
	

	public GLHealthBar(GameObject gameObject) {
		super(gameObject.getParentScene());
		this.gameObject = gameObject;
		//healthBar should not be touched
		camera.unregisterTouchable(this);

//		gameObject.registerPositionListener(this);
		onMeasure(10, 3);
	}
	
	@Override
	public void onDraw() {
		Matrix.multiplyMM(mvMatrix, 0, gameObject.getParentScene().getMVMatrix(), 0, 
				gameObject.getModelMatrix(), 0);
		if(mvMatrix[Matrix.POS_Z_OFFSET] > 0 || mvMatrix[Matrix.POS_Z_OFFSET] < -70){
			return;
		}
		positionOffset[0] = -mvMatrix[Matrix.POS_X_OFFSET] / mvMatrix[Matrix.POS_Z_OFFSET] +1;
		positionOffset[1] = -mvMatrix[Matrix.POS_Y_OFFSET] / mvMatrix[Matrix.POS_Z_OFFSET]*  camera.getWidthToHeightRatio() - 1;
		Log.i("tag", "pos = " + positionOffset[0] + ", " + positionOffset[1]);
		float h = gameObject.getHealthLevel() / gameObject.getMaxHealthLevel();
		setColor(blend(0, 240, h), blend(200, 0, h), blend(0, 0, h), 192);


		super.onDraw();
	}
	
	private float blend(int f, int s, float blendCoeff){
		return f * blendCoeff + s * (1 - blendCoeff);
	}
	
//	public static class GLHealtgBarShader extends GLViewShader{
//		
//		
//		@Override
//		public String getFragmentShaderSrc() {
//			// TODO Auto-generated method stub
//			return super.getFragmentShaderSrc();
//		}
//		
//		@Override
//		public String getVertexShaderSrc() {
//			// TODO Auto-generated method stub
//			return super.getVertexShaderSrc();
//		}
//	} 
//	@Override
//	public void onPositionChanged(float x, float y, float z) {
//		onLayout(50, 10);
//		
//	}

}
