package com.android.opengl.shader;

import android.opengl.GLES20;

public class GLViewShader extends Shader{

	public final int colorHandle;
	public final int positionHandle;
	public final int isPressedHandle;
	public final int isTextureEnabledHandle;
	public final int positionOffsetHandle;
	
	public final int textureHandle;
	public final int textureCoordHandle;
	
	public static final String UNIFORM_PRESSED = "uPressed";
	public static final String UNIFORM_TEXTURE_ENABLED = "uTextureEnabled";
	public static final String UNIFORM_POSITION_OFFSET = "u_PositionOffset";		

	public static final String UNIFORM_TEXTURE = "uTexture";
	public static final String ATTRIBUTE_TEXTURE_COORD = "aTexCoord";
	public static final String UNIFORM_COLOR = "aColor";
	public static final String ATTRIBUTE_POSITION = "aPosition";

	
	public GLViewShader() {
		colorHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_COLOR);
		positionHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_POSITION);
		isPressedHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_PRESSED);
		positionOffsetHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_POSITION_OFFSET);

		isTextureEnabledHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_TEXTURE_ENABLED);
		textureHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_TEXTURE);
		textureCoordHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_TEXTURE_COORD);
		

	}
	
	
	@Override
	public String getVertexShaderSrc() {
		return 
		"attribute 	vec2 " + ATTRIBUTE_POSITION + ";												" +
		"uniform 	vec2 " + UNIFORM_POSITION_OFFSET + ";											" +
		"attribute 	vec2 " + ATTRIBUTE_TEXTURE_COORD + ";											" +
		
		"																							" +
		"varying 	vec2 v_TexCoord; 																" +
		"																							" +
		"void main(){																				" +
		"	v_TexCoord = " + ATTRIBUTE_TEXTURE_COORD + ";												" +
		"	gl_Position = vec4(" + ATTRIBUTE_POSITION + " + " + UNIFORM_POSITION_OFFSET + ", 0.0, 1.0);										" +
		"}																							";
}


	@Override
	public String getFragmentShaderSrc() {
		return 			
			"precision mediump float;																" +
			"uniform 	vec4 " + UNIFORM_COLOR + ";													" +
			"uniform 	float "+UNIFORM_PRESSED+";													" +
			"uniform 	float "+UNIFORM_TEXTURE_ENABLED+";													" +
			"uniform	sampler2D "+UNIFORM_TEXTURE+ ";												" +

			"varying 	vec2 v_TexCoord; 															" +
			"void main(){																			" +
			"		vec4 resColor = " + UNIFORM_COLOR + ";" +
			"		if("+UNIFORM_TEXTURE_ENABLED+" > 0.0) {" +
			"			resColor = texture2D("+UNIFORM_TEXTURE+", v_TexCoord);						" +
					"}" +
			"		if("+ UNIFORM_PRESSED+" > 0.0){													" +
			"			resColor *=  1.6;				" +
			"		}																				" +
			"		gl_FragColor = resColor;	" +
			"}																						";	
		}


}
