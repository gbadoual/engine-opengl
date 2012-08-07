package com.android.opengl.view.control;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLES20;

import com.android.opengl.gameobject.CommonGameObject;
import com.android.opengl.gameobject.CommonGameObject.VboDataHandler;
import com.android.opengl.shader.GLViewShader;

public abstract class GLView {
	
	protected float leftCoord;
	protected float topCoord;
	protected float width;
	protected float height;
	protected short bkgColorR;
	protected short bkgColorG;
	protected short bkgColorB;
	protected short bkgColorA;
	
	private GLViewShader shader;
	private VboDataHandler vboDataHandler;
	
	
	
	private boolean isVisible = true;
	
	int[] indexData = new int[]{0, 3, 1, 1, 3, 2};
	
	public GLView() {
		this.shader = new GLViewShader();
		
		vboDataHandler = new VboDataHandler();

		FloatBuffer vertexBuffer;
		FloatBuffer colorBuffer;
		IntBuffer indexBuffer;
		
		leftCoord = -0.5F;
		topCoord = -0.5f;
		width = 1f;
		height = 1;
		bkgColorR = 128;
		bkgColorG = 128;
		bkgColorB = 128;
		bkgColorA = 128;
		float[] vertexData = new float[]{leftCoord, topCoord, 0, 
										 leftCoord + width, topCoord, 0,
										 leftCoord + width, topCoord + height, 0,
										 leftCoord, topCoord + height, 0};
		float[] colorData = new float[]{bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA				};
		
		vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexBuffer.put(vertexData).position(0);
		colorBuffer = ByteBuffer.allocateDirect(colorData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		colorBuffer.put(colorData).position(0);
		indexBuffer = ByteBuffer.allocateDirect(indexData.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		indexBuffer.put(indexData).position(0);
		
		
		int[] vboBufs = new int[2];
		GLES20.glGenBuffers(vboBufs.length, vboBufs, 0);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboBufs[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
		vboDataHandler.vboVertexHandle = vboBufs[0];
			
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboBufs[1]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorBuffer.capacity() * 4, colorBuffer, GLES20.GL_STATIC_DRAW);
		vboDataHandler.vboColorHandle = vboBufs[1];
		
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboBufs[1]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 4, indexBuffer, GLES20.GL_STATIC_DRAW);
		vboDataHandler.vboIndexHandle = vboBufs[1];
		
		
		vertexBuffer.limit(0);
		colorBuffer.limit(0);
		indexBuffer.limit(0);
		
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
//		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		
	}
	
	public void draw(){
		if(isVisible){
			GLES20.glUseProgram(shader.programHandle);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboVertexHandle);
			GLES20.glEnableVertexAttribArray(shader.positionHandle);
			GLES20.glVertexAttribPointer(shader.positionHandle, CommonGameObject.VERTEX_ELEMENT_SIZE, GLES20.GL_FLOAT, false, 0, 0);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vboDataHandler.vboColorHandle);
			GLES20.glEnableVertexAttribArray(shader.colorHandle);
			GLES20.glVertexAttribPointer(shader.colorHandle, CommonGameObject.COLOR_ELEMENT_SIZE, GLES20.GL_FLOAT, false, 0, 0);

			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboDataHandler.vboIndexHandle);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexData.length, GLES20.GL_UNSIGNED_SHORT, 0);

//			GLES20.glEnableVertexAttribArray(shader.positionHandle);
//			GLES20.glVertexAttribPointer(shader.positionHandle, 3, GLES20.GL_FLOAT, true, 3 * 4, vertexBuffer);
//			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 4);
//			GLES20.glDisableVertexAttribArray(shader.positionHandle);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
			GLES20.glUseProgram(0);
		}				
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	
}
