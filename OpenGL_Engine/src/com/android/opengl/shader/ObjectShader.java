package com.android.opengl.shader;

import android.opengl.GLES20;

public class ObjectShader extends SceneShader{
	
	public final int clanColorHandle;
	public final int isSelectedHandle;
	
	
	public static final String UNIFORM_IS_SELECTED = "uIsSelected";
	public static final String ATTRIBUTE_SELECTED = "aSelected";
	private static final String UNIFORM_CLAN_COLOR = "uClanColor";


	public ObjectShader() {
		super();
		clanColorHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_CLAN_COLOR);
		isSelectedHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_IS_SELECTED);
	}


	@Override
	public String getVertexShaderSrc() {
		return 
		"uniform 	mat4 " + UNIFORM_MVP_MATRIX + "; 													" +
		"uniform 	mat4 " + UNIFORM_MV_MATRIX + "; 													" +
		"uniform 	vec4 " + UNIFORM_CLAN_COLOR+";													" +
		"uniform	vec3 " + UNIFORM_LIGHT_POSITION+"[" + MAX_LIGHT_COUNT + "];											" +
		"uniform 	float " + UNIFORM_IS_SELECTED+";													" +
		"uniform 	float " + UNIFORM_INSTANCE_ID+";													" +
		"uniform 	float " + UNIFORM_LIGHT_COUNT+";													" +
		"																							" +
		"attribute 	vec3 "+ATTRIBUTE_POSITION+";													" +
		"attribute 	vec3 "+ATTRIBUTE_NORMAL+";														" +
		"attribute 	vec2 "+ATTRIBUTE_TEXTURE_COORD+";												" +
		"																							" +
		"varying 	vec4 v_ClanColor; 																	" +
		"varying 	vec3 v_Position;																" +
		"varying 	vec3 v_LightPos[" + MAX_LIGHT_COUNT + "];																" +
		"varying 	float v_LightCount;																" +
		"varying 	vec3 v_Normal; 																	" +
		"varying 	vec2 v_TexCoord; 																" +
		"varying 	float v_isSelected;																" +
		"																							" +
		"void main(){																				" +
		"	vec3 pos = "+ ATTRIBUTE_POSITION +";					" +
		"	pos.x += " + UNIFORM_INSTANCE_ID + " * 2.0;" +
		"	pos.y -= " + UNIFORM_INSTANCE_ID + " * 2.0;" +
		"	v_LightCount = " + UNIFORM_LIGHT_COUNT + ";" +
		"	for(int i = 0; i < " + MAX_LIGHT_COUNT + "; ++i){" +
		"		v_LightPos[i] = " + UNIFORM_LIGHT_POSITION + "[i];" +
		"	}" +
		"	v_ClanColor = " + UNIFORM_CLAN_COLOR + ";" +
		"	v_Position = vec3("+UNIFORM_MV_MATRIX+" * vec4(pos, 1.0));					" +
		"	v_Normal = vec3("+UNIFORM_MV_MATRIX+" * vec4("+ ATTRIBUTE_NORMAL +", 0.0));				" +
		"	v_isSelected = "+UNIFORM_IS_SELECTED+";													" +
		"	v_TexCoord = "+ATTRIBUTE_TEXTURE_COORD+";													" +
		"	gl_Position = "+UNIFORM_MVP_MATRIX+" * vec4(pos, 1.0);							" +
		"}																							";
}


	@Override
	public String getFragmentShaderSrc() {
		return 			
			"precision highp float;																" +
			"uniform sampler2D " + UNIFORM_TEXTURE + ";															" +
			"varying 	vec3 v_Position;															" +
			"varying 	vec3 v_Normal; 																" +
			"varying 	float v_isSelected;															" +
			"varying 	vec2 v_TexCoord; 															" +
			"varying 	vec4 v_ClanColor; 																	" +
			"varying 	vec3 v_LightPos[" + MAX_LIGHT_COUNT + "];																" +
			"varying 	float v_LightCount;																" +
			"																					" +
			"" + generateFuncGetDiffuse() + 
			"void main(){																			" +
//				"	vec4 resColor;																		" +
			"	vec4 selectedColor = vec4(.0, .0, .0, .0);" +
			"	if (v_isSelected > 0.0){															" +
			"		selectedColor = vec4(0.5, 0.5, 0.5, 0.0);" +
			"	}" +
			"	float diffuse = " + INITIAL_DIFFUSE + ";								" +
			"	int count = 2;" +
			"	for(int i = 0; i < " + MAX_LIGHT_COUNT + "; ++i){" +
					"count--;" +
					"if(count >= 0){" +
			"			diffuse += getDiffuse(v_LightPos[i], v_Position, v_Normal, true);" +
			"		}" +
			"	}" +
			"	gl_FragColor = v_ClanColor + selectedColor + diffuse  * texture2D(" + UNIFORM_TEXTURE + ", v_TexCoord);	" +
//			"	gl_FragColor = resColor * diffuse * texture2D(" + UNIFORM_TEXTURE + ", v_TexCoord);	" +
//			"	gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0) * diffuse * selectedColor;	" +
			"																						" +
			"}																						";
		}
	
}
