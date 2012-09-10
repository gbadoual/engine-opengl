package com.android.opengl.view.control;

import android.util.Log;

import com.android.opengl.gameobject.GameObject;
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
		onMeasure(5, 1);
	}
	private int[] rValues = new int[]{20, 240, 240};
	private int[] gValues = new int[]{240, 240, 20};
	private int[] bValues = new int[]{20, 20, 20};
	
	
	@Override
	public void onDraw() {
		Matrix.multiplyMM(mvMatrix, 0, gameObject.getParentScene().getMVMatrix(), 0, 
				gameObject.getModelMatrix(), 0);
		if(mvMatrix[Matrix.POS_Z_OFFSET] > 0 || mvMatrix[Matrix.POS_Z_OFFSET] < -70){
			return;
		}
		positionOffset[0] = -mvMatrix[Matrix.POS_X_OFFSET] / mvMatrix[Matrix.POS_Z_OFFSET] +1;
		positionOffset[1] = -mvMatrix[Matrix.POS_Y_OFFSET] / mvMatrix[Matrix.POS_Z_OFFSET]*  camera.getWidthToHeightRatio() - 1;
//		Log.i("tag", "pos = " + positionOffset[0] + ", " + positionOffset[1]);
		float h = gameObject.getHealthLevel() / gameObject.getMaxHealthLevel();
		int r = evenlyInterpolate(rValues, h);
		int g = evenlyInterpolate(gValues, h);
		int b = evenlyInterpolate(bValues, h);
		setColor(r, g, b, 192);

		super.onDraw();
	}
	
	private int evenlyInterpolate(int[] valuaes, float coeff){
		coeff = 1 - coeff;
		if(coeff == 1.0){
			return valuaes[valuaes.length - 1];
		}
		int firstInterpIdx = (int)(coeff * (valuaes.length - 1));
		float localCoeff = (coeff - (float)firstInterpIdx / (valuaes.length - 1)) * (valuaes.length - 1);
		return (int)(valuaes[firstInterpIdx] * (1 - localCoeff) + valuaes[firstInterpIdx + 1] * localCoeff);
		
	}
	
//	private float blend(int f, int s, float blendCoeff){
//		return f * blendCoeff + s * (1 - blendCoeff);
//	}
//	private float blend(int f, int s, int third, float leftCoeff, float rightCoeff){
//		if(leftCoeff >= 0 && leftCoeff <= 1){
//		}
//		return f * blendCoeff + s * (1 - blendCoeff);
//	}
	
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
