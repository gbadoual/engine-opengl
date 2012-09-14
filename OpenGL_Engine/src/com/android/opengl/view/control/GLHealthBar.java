package com.android.opengl.view.control;

import android.opengl.GLES20;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.shader.GLViewShader;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.Log;
import com.android.opengl.util.geometry.Matrix;

public class GLHealthBar extends GLView{
	
	private GameObject gameObject;
	float[] mvMatrix = new float[16];
	

	public GLHealthBar(GameObject gameObject) {
		super(gameObject.getParentScene());
		this.gameObject = gameObject;
		mShader = new GLHealtBarShader();
		//healthBar should not be touched
		camera.unregisterTouchable(this);

		setBorderWidth(0.2f);

		onMeasure(5, 1);
	}
	private int[] rValues = new int[]{20, 240, 240};
	private int[] gValues = new int[]{240, 240, 20};
	private int[] bValues = new int[]{20, 20, 20};
	
	
	@Override
	public void onDrawFrame() {
		Matrix.multiplyMM(mvMatrix, 0, gameObject.getParentScene().getMVMatrix(), 0, 
				gameObject.getModelMatrix(), 0);
		if(mvMatrix[Matrix.POS_Z_OFFSET] > 0 || mvMatrix[Matrix.POS_Z_OFFSET] < -70){
//			Log.i("ddd", "mvMatrix[Matrix.POS_Z_OFFSET] = " + mvMatrix[Matrix.POS_Z_OFFSET] + "[" + gameObject.getClan() + "] skipping");
			return;
		}
//		Log.i("ddd", "mvMatrix[Matrix.POS_Z_OFFSET] = " + mvMatrix[Matrix.POS_Z_OFFSET] + "[" + gameObject.getClan() + "]");
		float xOffset = -mvMatrix[Matrix.POS_X_OFFSET] / mvMatrix[Matrix.POS_Z_OFFSET] + 1 - mScaledWidth / 2;
		float yOffset = -mvMatrix[Matrix.POS_Y_OFFSET] / mvMatrix[Matrix.POS_Z_OFFSET] * camera.getWidthToHeightRatio() - 1;
		setPositionOffset(xOffset, yOffset);
		float h = gameObject.getHealthLevel() / gameObject.getMaxHealthLevel();
		int r = evenlyInterpolate(rValues, h);
		int g = evenlyInterpolate(gValues, h);
		int b = evenlyInterpolate(bValues, h);
		setColor(r, g, b, 192);
		GLUtil.glUseProgram(mShader.programHandle);
		float screenAlignedHealthLevel = mWidth * h * percentToScreenRatio + worldToScreenX(xOffset);
		GLES20.glUniform1f(((GLHealtBarShader)mShader).healthLevelHandle, screenAlignedHealthLevel);
		super.onDrawFrame();
	}
	


	private float worldToScreenX(float coord) {
		return coord / 2 * camera.getViewportWidth();
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
	

	
	public GameObject getGameObject() {
		return gameObject;
	}



	public static class GLHealtBarShader extends GLViewShader{

		public final int healthLevelHandle;
		
		private static final String UNIFORM_HEALTH_LEVEL = "uHealthLevel";

		public GLHealtBarShader() {
			super();
			healthLevelHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_HEALTH_LEVEL);
		}
		
		@Override
		public String getVertexShaderSrc() {
			return 
			"uniform 	vec2 " + UNIFORM_POSITION_OFFSET + ";											" +

			"attribute 	vec2 " + ATTRIBUTE_POSITION + ";												" +
			"attribute 	float " + UNIFORM_INSTANCE_ID + ";											" +
			"uniform 	float " + UNIFORM_HEALTH_LEVEL + ";											" +
			
			"																							" +
			"varying float vInstanceID;" +
			"varying float vHealthLevel;" +
			"																							" +
			"void main(){																				" +
			"	vInstanceID = " + UNIFORM_INSTANCE_ID + ";" +
			"	vHealthLevel = " + UNIFORM_HEALTH_LEVEL + ";" +
			"	gl_Position = vec4(" + ATTRIBUTE_POSITION + " + " + UNIFORM_POSITION_OFFSET + ", 0.0, 1.0);										" +
			"}																							";
	}


		@Override
		public String getFragmentShaderSrc() {
			return 			
				"precision mediump float;																" +
				"uniform 	vec4 " + UNIFORM_COLOR + ";													" +

				"varying float vHealthLevel;" +
				"varying float vInstanceID;" +

				"varying 	vec2 v_TexCoord; 															" +
				"void main(){																			" +
				"		vec4 resColor = " + UNIFORM_COLOR + ";" +
				"		if(vInstanceID != 1.0){" +
				"			resColor = vec4(0.2, 0.3, 0.2, 0.7);" +
				"		} else {" +
				"			if(gl_FragCoord.x > vHealthLevel){" +
				"				resColor = vec4(0.0);" +
				"			};" +
				"		}" +
				"		gl_FragColor = resColor;	" +
				"}																						";	
			}

	} 


}
