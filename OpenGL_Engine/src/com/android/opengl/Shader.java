package com.android.opengl;

import android.opengl.GLES20;

public class Shader {

	private static final String TAG = Shader.class.getSimpleName();

	
	
	
	public final int programHandle;
	public final int mvpMatrixHandle;
	public final int mvMatrixHandle;
	public final int positionHandle;

	public final int normalHandle;
//	public final int textureHandle;
	public final int isSelectedHandle;
	
	public final int textureCoordHandle;
	public final int textureUniformHandle;
	public int textureDataHandler;
	
	
	
	public static final String UNIFORM_MVP_MATRIX = "u_MVPMatrix";
	public static final String UNIFORM_MV_MATRIX = "u_MVMatrix";
	public static final String UNIFORM_IS_SELECTED = "uIsChecked";
	public static final String UNIFORM_LIGHT_POSITION = "uLightPosition";
	public static final String UNIFORM_TEXTURE = "u_Texture";
	
	public static final String ATTRIBUTE_SELECTED = "aSelected";
	public static final String ATTRIBUTE_POSITION = "aPosition";
//	public static final String ATTRIBUTE_COLOR = "aColor";
	public static final String ATTRIBUTE_NORMAL = "aNormal";
	public static final String ATTRIBUTE_TEXTURE_COORD = "aTexCoord";

	private static final String vertexShaderSrc = 
		"uniform 	mat4 "+UNIFORM_MVP_MATRIX + "; 													" +
		"uniform 	mat4 "+UNIFORM_MV_MATRIX + "; 													" +
		"uniform 	float "+UNIFORM_IS_SELECTED+";													" +
		"																							" +
//		"attribute 	vec4 "+ATTRIBUTE_COLOR+";														" +
		"attribute 	vec4 "+ATTRIBUTE_POSITION+";													" +
		"attribute 	vec3 "+ATTRIBUTE_NORMAL+";														" +
		"attribute 	vec2 "+ATTRIBUTE_TEXTURE_COORD+";												" +
		"																							" +
//		"varying 	vec4 v_Color; 																	" +
		"varying 	vec3 v_Position;																" +
		"varying 	vec3 v_Normal; 																	" +
		"varying 	vec2 v_TexCoord; 																" +
		"varying 	float v_isSelected;																" +
		"																							" +
		"void main(){																				" +
//		"	v_Color = "+ATTRIBUTE_COLOR+";															" +
		"	v_Position = vec3("+UNIFORM_MV_MATRIX+" * "+ ATTRIBUTE_POSITION +");					" +
		"	v_Normal = vec3("+UNIFORM_MV_MATRIX+" * vec4("+ ATTRIBUTE_NORMAL +", 0.0));				" +
		"	v_isSelected = "+UNIFORM_IS_SELECTED+";													" +
		"	v_TexCoord = vec2("+ATTRIBUTE_TEXTURE_COORD+".x, "+ATTRIBUTE_TEXTURE_COORD+".y);													" +
		"	gl_Position = "+UNIFORM_MVP_MATRIX+" * "+ATTRIBUTE_POSITION+";							" +
		"}																							";

	private static final String fragmentShaderSrc = "" +
			"precision mediump float;																" +
			"uniform sampler2D " + UNIFORM_TEXTURE + ";															" +
//			"varying	vec3 "+ UNIFORM_LIGHT_POSITION+";											" +
//			"varying	vec4 v_Color;																" +
			"varying 	vec3 v_Position;															" +
			"varying 	vec3 v_Normal; 																" +
			"varying 	float v_isSelected;															" +
			"varying 	vec2 v_TexCoord; 															" +
			"void main(){																			" +
//			"	vec4 resColor;																		" +
			"	float selectedColor = 1.0;" +
			"	if (v_isSelected > 0.0){															" +
			"		selectedColor = 2.0;" +
//			"		resColor = vec4(v_Color.x / 2.0, v_Color.y + 1.0, v_Color.z + 1.0, v_Color.w);	" +
			"	} else {																			" +
//			"		resColor = v_Color;																" +
			"	}																					" +
			"	vec3 "+ UNIFORM_LIGHT_POSITION+" = vec3(0.0,0.0,0.0);								" +
			"	float distance = length("+UNIFORM_LIGHT_POSITION+" - v_Position);					" +
			"	vec3 lightVector = normalize("+ UNIFORM_LIGHT_POSITION+" - v_Position);				" +
			"	float diffuse = max(dot(v_Normal, lightVector), 0.1);								" +
			"	diffuse = diffuse * 1.0/(1.0+(0.0001*distance*distance));							" +
//			"	gl_FragColor = resColor * diffuse * texture2D(" + UNIFORM_TEXTURE + ", v_TexCoord);	" +
			"	gl_FragColor = diffuse * selectedColor * texture2D(" + UNIFORM_TEXTURE + ", v_TexCoord);	" +
//			"	gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0) * diffuse * selectedColor;	" +
			"																						" +
			"}																						";
	


	
	public Shader() {
		programHandle = createAndLinkProgram();
		mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, Shader.UNIFORM_MVP_MATRIX);
		mvMatrixHandle = GLES20.glGetUniformLocation(programHandle, Shader.UNIFORM_MV_MATRIX);
		isSelectedHandle = GLES20.glGetUniformLocation(programHandle, Shader.UNIFORM_IS_SELECTED);
		positionHandle = GLES20.glGetAttribLocation(programHandle, Shader.ATTRIBUTE_POSITION);
//		colorHandle = GLES20.glGetAttribLocation(programHandle, Shader.ATTRIBUTE_COLOR);
		normalHandle = GLES20.glGetAttribLocation(programHandle, Shader.ATTRIBUTE_NORMAL);

		textureUniformHandle = GLES20.glGetUniformLocation(programHandle, Shader.UNIFORM_TEXTURE);
		textureCoordHandle = GLES20.glGetAttribLocation(programHandle, Shader.ATTRIBUTE_TEXTURE_COORD);
	}
	
	public int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		int[] compiled = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			throw new RuntimeException(TAG
					+ "loadShader()   Could not compile shader " + type + ":"
					+ GLES20.glGetShaderInfoLog(shader));
			// GLES20.glDeleteShader(shader);
			// shader = 0;
		}
		return shader;
	}

	public int createAndLinkProgram() {
		int program = GLES20.glCreateProgram();
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc);
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
				fragmentShaderSrc);
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);
		return program;
	}

}
