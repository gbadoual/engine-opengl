package com.android.opengl.shader;

import android.opengl.GLES20;

public class CommonShader extends Shader{
	
	public final int mvpMatrixHandle;
	public final int mvMatrixHandle;
	public final int positionHandle;


	public final int normalHandle;
//	public final int textureHandle;
	public final int isSelectedHandle;
	
	public final int textureCoordHandle;
	public final int textureHandle;
	public int textureDataHandler;
	
	
	
	public static final String UNIFORM_MVP_MATRIX = "u_MVPMatrix";
	public static final String UNIFORM_MV_MATRIX = "u_MVMatrix";
	public static final String UNIFORM_IS_SELECTED = "uIsSelected";
	public static final String UNIFORM_LIGHT_POSITION = "uLightPosition";
	public static final String UNIFORM_TEXTURE = "u_Texture";
	
	public static final String ATTRIBUTE_SELECTED = "aSelected";
	public static final String ATTRIBUTE_POSITION = "aPosition";

	public static final String ATTRIBUTE_NORMAL = "aNormal";
	public static final String ATTRIBUTE_TEXTURE_COORD = "aTexCoord";




	public CommonShader() {
		mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_MVP_MATRIX);
		mvMatrixHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_MV_MATRIX);
		isSelectedHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_IS_SELECTED);
		positionHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_POSITION);
		normalHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_NORMAL);

		textureHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_TEXTURE);
		textureCoordHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_TEXTURE_COORD);
	}


	@Override
	public String getVertexShaderSrc() {
		return 
		"uniform 	mat4 "+UNIFORM_MVP_MATRIX + "; 													" +
		"uniform 	mat4 "+UNIFORM_MV_MATRIX + "; 													" +
		"uniform 	float "+UNIFORM_IS_SELECTED+";													" +
		"																							" +
		"attribute 	vec4 "+ATTRIBUTE_POSITION+";													" +
		"attribute 	vec3 "+ATTRIBUTE_NORMAL+";														" +
		"attribute 	vec2 "+ATTRIBUTE_TEXTURE_COORD+";												" +
		"																							" +
		"varying 	vec4 v_Color; 																	" +
		"varying 	vec3 v_Position;																" +
		"varying 	vec3 v_Normal; 																	" +
		"varying 	vec2 v_TexCoord; 																" +
		"varying 	float v_isSelected;																" +
		"																							" +
		"void main(){																				" +
		"	v_Position = vec3("+UNIFORM_MV_MATRIX+" * "+ ATTRIBUTE_POSITION +");					" +
		"	v_Normal = vec3("+UNIFORM_MV_MATRIX+" * vec4("+ ATTRIBUTE_NORMAL +", 0.0));				" +
		"	v_isSelected = "+UNIFORM_IS_SELECTED+";													" +
		"	v_TexCoord = "+ATTRIBUTE_TEXTURE_COORD+";													" +
		"	gl_Position = "+UNIFORM_MVP_MATRIX+" * "+ATTRIBUTE_POSITION+";							" +
		"}																							";
}


	@Override
	public String getFragmentShaderSrc() {
		return 			
			"precision mediump float;																" +
			"uniform sampler2D " + UNIFORM_TEXTURE + ";															" +
//				"varying	vec3 "+ UNIFORM_LIGHT_POSITION+";											" +
			"varying 	vec3 v_Position;															" +
			"varying 	vec3 v_Normal; 																" +
			"varying 	float v_isSelected;															" +
			"varying 	vec2 v_TexCoord; 															" +
			"void main(){																			" +
//				"	vec4 resColor;																		" +
			"	float selectedColor = 1.0;" +
			"	if (v_isSelected > 0.0){															" +
			"		selectedColor = 2.0;" +
//				"		resColor = vec4(v_Color.x / 2.0, v_Color.y + 1.0, v_Color.z + 1.0, v_Color.w);	" +
			"	} else {																			" +
//				"		resColor = v_Color;																" +
			"	}																					" +
			"	vec3 "+ UNIFORM_LIGHT_POSITION+" = vec3(0.0,0.0,0.0);								" +
			"	float distance = length("+UNIFORM_LIGHT_POSITION+" - v_Position);					" +
			"	vec3 lightVector = normalize("+ UNIFORM_LIGHT_POSITION+" - v_Position);				" +
			"	float diffuse = max(dot(v_Normal, lightVector), 0.1);								" +
			"	diffuse = diffuse * 1.0/(1.0+(0.0001*distance*distance));							" +
//				"	gl_FragColor = resColor * diffuse * texture2D(" + UNIFORM_TEXTURE + ", v_TexCoord);	" +
//"				if(" + UNIFORM_TEXTURE + "!= null){	" +
			"		gl_FragColor = diffuse * selectedColor * texture2D(" + UNIFORM_TEXTURE + ", v_TexCoord);	" +
//				"	}" +
//				"	gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0) * diffuse * selectedColor;	" +
			"																						" +
			"}																						";	
		}
	
}
