package com.android.opengl.view.control;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLES20;

import com.android.opengl.gameobject.CommonGameObject;
import com.android.opengl.gameobject.CommonGameObject.VboDataHandler;
import com.android.opengl.gameobject.util.geometry.Point2D;
import com.android.opengl.shader.GLViewShader;

public abstract class GLView {
	
	protected float leftCoord;
	protected float topCoord;
	protected float width;
	protected float height;
	protected float bkgColorR;
	protected float bkgColorG;
	protected float bkgColorB;
	protected float bkgColorA;
	
	private GLViewShader shader;
	private VboDataHandler vboDataHandler;
	
	float[] vertexData;
	
	private boolean isVisible = true;
	
	int[] indexData = new int[]{0, 2, 3, 0, 1, 2};
	
	public GLView() {
		this.shader = new GLViewShader();
		vboDataHandler = new VboDataHandler();
		onLayout(0.6f, -1.0f);
		onMeasure(0.4f, 2.0f);
		
		vertexData = new float[]{leftCoord, topCoord, 0, 
				 leftCoord + width, topCoord, 0,
				 leftCoord + width, topCoord + height, 0,
				 leftCoord, topCoord + height, 0};

		bkgColorR = 128 / 255.0f;
		bkgColorG = 128 / 255.0f;
		bkgColorB = 128 / 255.0f;
		bkgColorA = 128 / 255.0f;

		float[] colorData = new float[]{bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA				};
		

		FloatBuffer vertexBuffer;
		IntBuffer indexBuffer;
		FloatBuffer colorBuffer;

		vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexBuffer.put(vertexData).position(0);
		colorBuffer = ByteBuffer.allocateDirect(colorData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		colorBuffer.put(colorData).position(0);
		indexBuffer = ByteBuffer.allocateDirect(indexData.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		indexBuffer.put(indexData).position(0);
		
		
		int[] vboBufs = new int[3];
		GLES20.glGenBuffers(vboBufs.length, vboBufs, 0);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboBufs[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
		vboDataHandler.vboVertexHandle = vboBufs[0];
			
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboBufs[1]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorBuffer.capacity() * 4, colorBuffer, GLES20.GL_STATIC_DRAW);
		vboDataHandler.vboColorHandle = vboBufs[1];
		
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboBufs[2]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 4, indexBuffer, GLES20.GL_STATIC_DRAW);
		vboDataHandler.vboIndexHandle = vboBufs[2];
		
		
		vertexBuffer.limit(0);
		colorBuffer.limit(0);
		indexBuffer.limit(0);
		
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		
	}
	
	public void draw(){
		if(isVisible){
			GLES20.glEnable(GLES20.GL_BLEND);
		    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

			GLES20.glUseProgram(shader.programHandle);

			
			
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboVertexHandle);
			GLES20.glEnableVertexAttribArray(shader.positionHandle);
			GLES20.glVertexAttribPointer(shader.positionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboColorHandle);
			GLES20.glEnableVertexAttribArray(shader.colorHandle);
			GLES20.glVertexAttribPointer(shader.colorHandle, 4, GLES20.GL_FLOAT, false, 0, 0);
			
			
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboDataHandler.vboIndexHandle);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexData.length, GLES20.GL_UNSIGNED_INT, 0);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	        
	        GLES20.glUseProgram(0);		
			GLES20.glDisable(GLES20.GL_BLEND);

		}				
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	

	protected void onLayout(float leftCoord, float topCoord){
		this.leftCoord = leftCoord;
		this.topCoord = topCoord;
	}

	protected void onMeasure(float width, float height) {
		this.width = width;
		this.height = height;
	} 
	
}
