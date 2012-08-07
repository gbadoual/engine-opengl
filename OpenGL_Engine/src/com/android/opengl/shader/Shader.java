package com.android.opengl.shader;

import android.opengl.GLES20;

public abstract class Shader {

	private static final String TAG = Shader.class.getSimpleName();

	
	public final int programHandle;
	

	public Shader() {
		programHandle = createAndLinkProgram();
	}
	

	public int createAndLinkProgram() {
		int program = GLES20.glCreateProgram();
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexShaderSrc());
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShaderSrc());
		GLES20.glAttachShader(program, vertexShader);
		GLES20.glAttachShader(program, fragmentShader);
		GLES20.glLinkProgram(program);
		return program;
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
	
	public abstract String getVertexShaderSrc();
	public abstract String getFragmentShaderSrc();

}
