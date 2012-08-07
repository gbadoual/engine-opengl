package com.android.opengl.shader;

import android.opengl.GLES20;

public class GLViewShader extends Shader{

	public final int colorHandle;
	public final int positionHandle;
	
	public static final String ATTRIBUTE_COLOR = "aColor";
	public static final String ATTRIBUTE_POSITION = "aPosition";

	
	public GLViewShader() {
		colorHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_COLOR);
		positionHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_POSITION);

	}
	
	
	@Override
	public String getVertexShaderSrc() {
		return 
		"attribute 	vec4 "+ATTRIBUTE_COLOR+";														" +
		"attribute 	vec3 "+ATTRIBUTE_POSITION+";													" +
		"																							" +
		"varying 	vec4 v_Color; 																	" +
		"																							" +
		"void main(){																				" +
		"	v_Color = "+ATTRIBUTE_COLOR+";															" +
		"	gl_Position = vec4("+ATTRIBUTE_POSITION+", 1.0);							" +
		"}																							";
}


	@Override
	public String getFragmentShaderSrc() {
		return 			
			"precision mediump float;																" +
			"varying	vec4 v_Color;																" +
			"void main(){																			" +
			"		gl_FragColor = v_Color;	" +
			"}																						";	
		}


}
