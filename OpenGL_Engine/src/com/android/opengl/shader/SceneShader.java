package com.android.opengl.shader;

import android.opengl.GLES20;

public class SceneShader extends Shader{
	
	public final int mvpMatrixHandle;
	public final int mvMatrixHandle;

	
	public final int positionHandle;
	public final int normalHandle;
	public final int textureCoordHandle;
	public final int textureHandle;

	public final int lightPositionHandle;
	public final int lightCountHandle;
	
	
	
	//Warning
	//All sub-classes should have all of the following variables in vertex or fragment shader source code
	
	public static final String UNIFORM_MVP_MATRIX = "u_MVPMatrix";
	public static final String UNIFORM_MV_MATRIX = "u_MVMatrix";
	public static final String UNIFORM_LIGHT_POSITION = "uLightPosition";
	public static final String UNIFORM_TEXTURE = "u_Texture";
	
	public static final String ATTRIBUTE_POSITION = "aPosition";

	public static final String ATTRIBUTE_NORMAL = "aNormal";
	public static final String ATTRIBUTE_TEXTURE_COORD = "aTexCoord";
	public static final String UNIFORM_LIGHT_COUNT = "uLightCount";

	public SceneShader() {
		mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_MVP_MATRIX);
		mvMatrixHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_MV_MATRIX);
		lightPositionHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_LIGHT_POSITION);
		lightCountHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_LIGHT_COUNT);
		positionHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_POSITION);
		normalHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_NORMAL);

		textureHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_TEXTURE);
		textureCoordHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_TEXTURE_COORD);
	}
	
	
	@Override
	public String getVertexShaderSrc() {
		return 
		"uniform 	mat4 " + UNIFORM_MVP_MATRIX + "; 													" +
		"uniform 	mat4 " + UNIFORM_MV_MATRIX + "; 													" +
		"uniform	vec3 " + UNIFORM_LIGHT_POSITION+"[" + MAX_LIGHT_COUNT + "];											" +
		"uniform	float " + UNIFORM_LIGHT_COUNT+";											" +
		"																							" +
		"attribute 	vec4 " + ATTRIBUTE_POSITION+";													" +
		"attribute 	vec3 " + ATTRIBUTE_NORMAL+";														" +
		"attribute 	vec2 " + ATTRIBUTE_TEXTURE_COORD+";												" +
		"																							" +
		"varying 	vec3 v_Position;																" +
		"varying 	vec3 v_LightPos[" + MAX_LIGHT_COUNT + "];																" +
		"varying 	float v_LightCount;																" +
		"varying 	vec3 v_Normal; 																	" +
		"varying 	vec2 v_TexCoord; 																" +
		"																							" +
		"void main(){																				" +
		"	vec4 pos = "+ ATTRIBUTE_POSITION +";					" +
		"	v_LightCount = " + UNIFORM_LIGHT_COUNT + ";" +
		"	int count = int(v_LightCount);" +

		"	for(int i = 0; i < " + MAX_LIGHT_COUNT + "; ++i){" +
				"count --;" +
//			"	if(count >= 0){" +
			"		v_LightPos[i] = vec3(vec4(" + UNIFORM_LIGHT_POSITION + "[i], 1.0));" +
//			"	}" +
		"	}" +
		"	v_Position = vec3("+UNIFORM_MV_MATRIX+" * pos);					" +
		"	v_Normal = vec3("+UNIFORM_MV_MATRIX+" * vec4("+ ATTRIBUTE_NORMAL +", 0.0));				" +
		"	v_TexCoord = "+ATTRIBUTE_TEXTURE_COORD+";													" +
		"	gl_Position = "+UNIFORM_MVP_MATRIX+" * pos;							" +
		"}																							";
}


	@Override
	public String getFragmentShaderSrc() {
		return 			
			"precision mediump float;																" +
			"uniform sampler2D " + UNIFORM_TEXTURE + ";															" +
			"varying 	vec3 v_Position;															" +
			"varying 	vec3 v_Normal; 																" +
			"varying 	vec2 v_TexCoord; 															" +
			"varying 	vec3 v_LightPos[" + MAX_LIGHT_COUNT + "];																" +
			"varying 	float v_LightCount;																" +
			"																					" +
			"" +generateFuncGetDiffuse() + 
			"void main(){																			" +
			"	float diffuse = " + INITIAL_DIFFUSE + ";								" +
			"	int count = int(v_LightCount);" +
			"	for(int i = 0 ; i < " + MAX_LIGHT_COUNT + "; ++i){" +
					"count--;" +
					"if(count >= 0){" +
			"			diffuse += getDiffuse(v_LightPos[i], v_Position, v_Normal, true);" +
			"		}" +
			"	}" +
//			"	gl_FragColor = resColor * diffuse * texture2D(" + UNIFORM_TEXTURE + ", v_TexCoord);	" +
			"	gl_FragColor = diffuse * texture2D(" + UNIFORM_TEXTURE + ", v_TexCoord);	" +
//			"	gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0) * diffuse * selectedColor;	" +
			"																						" +
			"}";
		}
	
	public static String generateFuncGetDiffuse(){
		return 	"" +
				"	float getDiffuse(in vec3 lightPos, in vec3 pixelPos , in vec3 normal, in bool distanceReducing){" +
				"		float distance = length(lightPos - pixelPos);					" +
				"		vec3 lightVector = normalize(lightPos - pixelPos);				" +
				"		float diffuse = max(dot(normal, lightVector), 0.0);								" +
				"		if (distanceReducing){" +
				"			diffuse = diffuse * 1.0/(1.0+(0.001*distance*distance));							" +
				"		}" +
				"		return diffuse;" +
				"	}" +
				"";

	}

}
