package com.android.opengl.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLES20;
import android.util.Log;

import com.android.opengl.shader.CommonShader;
import com.android.opengl.shader.Shader;

public class GLUtil {
	
	public static final int VERTEX_SIZE = 3;
	public static final int COLOR_SIZE = 4;
	public static final int TEXTURE_SIZE = 2;
	public static final int NORMAL_SIZE = 3;

	
	
	private static final int SIZE_INT = 4;
	private static final int SIZE_FLOAT = 4;
	
	public static void attachArrayToHandler(float[] data, int handler){
//		int[] vboBufs = new int[4];
//		GLES20.glGenBuffers(vboBufs.length, vboBufs, 0);
		FloatBuffer floatBuffer;
		floatBuffer = ByteBuffer.allocateDirect(data.length * SIZE_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		floatBuffer.put(data).position(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, handler);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, floatBuffer.capacity() * SIZE_FLOAT, floatBuffer, GLES20.GL_STATIC_DRAW);
		floatBuffer.limit(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	public static  void attachIndexesToHandler(int[] indexData, int handler){
		IntBuffer indexBuffer;
		indexBuffer = ByteBuffer.allocateDirect(indexData.length * SIZE_INT).order(ByteOrder.nativeOrder()).asIntBuffer();
		indexBuffer.put(indexData).position(0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, handler);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * SIZE_INT, indexBuffer, GLES20.GL_STATIC_DRAW);
		indexBuffer.limit(0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}	
	
	
	public static void passTextureToShader(int textureDataHandler, int shaderTextureDataHandler){
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureDataHandler);
	    GLES20.glUniform1i(shaderTextureDataHandler, 0);
	}

	public static void passBufferToShader(int bufferHandler, int shaderBufferHandler, int size){
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandler);
		GLES20.glEnableVertexAttribArray(shaderBufferHandler);
		GLES20.glVertexAttribPointer(shaderBufferHandler, size, GLES20.GL_FLOAT, false, 0, 0);		
	}

	public static void drawElements(int vboIndexHandle, int indexDataLength) {
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandle);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexDataLength, GLES20.GL_UNSIGNED_INT, 0);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		
	}
	
	

}