package com.android.opengl.gameobject.base;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.android.opengl.R;
import com.android.opengl.Shader;
import com.android.opengl.Shader.VboDataHandler;
import com.android.opengl.gameobject.util.LoaderManager;

public abstract class CommonGameObject {
	
	private static final String TAG = CommonGameObject.class.getSimpleName();
	
	public static long facesCount = 0;

	final protected int programHandle;
	public int mvpMatrixHandle;
	public int positionHandle;
	
	public int textureCoordHandle;
	public int textureUniformHandle;
	private int textureDataHandler;

	public int normalHandle;
	public int textureHandle;
	protected int isSelectedHandle;
	
	protected static Map<String, VboDataHandler> vboDataHandlerMap = new HashMap<String, Shader.VboDataHandler>();


	
	protected float[] modelMatrix = new float[16];
	protected float[] mvpMatrix = new float[16];

	protected float[] centerXYZ = new float[3];
	protected float[] angleXYZ = new float[3];
	
//	protected float[] vertexData;
//	protected int indexDataLength;

	protected boolean isSelected;
	
	private int vertexElementSize = 3;
	protected int colorElementSize = 3;
	protected int textureElementSize = 3;
	protected int normalElementSize = 3;
	
	
	protected LoaderManager meshLoader;
	protected Context context;
	protected LoaderManager.MeshData meshData;

	
	public CommonGameObject(int programHandle, Context context) {
		this.programHandle = programHandle;
		this.context = context;
		meshLoader = new LoaderManager(this);
		initData();
	}
	
	private void initData() {
//		GLES20.glEnable(GLES20.GL_BLEND);
//	    GLES20.glBlendFunc (GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	    
		Matrix.setIdentityM(modelMatrix, 0);
		mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, Shader.UNIFORM_MVP_MATRIX);
		isSelectedHandle = GLES20.glGetUniformLocation(programHandle, Shader.UNIFORM_IS_SELECTED);
		positionHandle = GLES20.glGetAttribLocation(programHandle, Shader.ATTRIBUTE_POSITION);
//		colorHandle = GLES20.glGetAttribLocation(programHandle, Shader.ATTRIBUTE_COLOR);
		normalHandle = GLES20.glGetAttribLocation(programHandle, Shader.ATTRIBUTE_NORMAL);

		textureUniformHandle = GLES20.glGetUniformLocation(programHandle, Shader.UNIFORM_TEXTURE);
		textureCoordHandle = GLES20.glGetAttribLocation(programHandle, Shader.ATTRIBUTE_TEXTURE_COORD);
		GLES20.glEnable(GLES20.GL_TEXTURE_2D);
		textureDataHandler = LoaderManager.loadTexture(context, R.raw.worldmap); 
		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());
		if (vboDataHandler == null){
			vboDataHandler = new VboDataHandler();
			vboDataHandlerMap.put(getClass().getSimpleName(), vboDataHandler);
			meshData = meshLoader.loadFromRes(getMeshResource());
			if(meshData == null){
				Log.w("tag",TAG+":initDtata() meshData is null. Init aborting.");
				return;
			}
			vboDataHandler.vertexData = meshData.vertexData;
			vboDataHandler.indexData = meshData.indexData;
			vboDataHandler.facesCount = meshData.facesCount;

			//buffers
			FloatBuffer vertexBuffer;
			FloatBuffer textureBuffer;
			FloatBuffer normalBuffer;
			IntBuffer indexBuffer;
	//		FloatBuffer colorBuffer;
	
			vertexBuffer = ByteBuffer.allocateDirect(vboDataHandler.vertexData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
			vertexBuffer.put(vboDataHandler.vertexData).position(0);
	//		colorBuffer = ByteBuffer.allocateDirect(meshData.textureData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
	//		colorBuffer.put(meshData.textureData).position(0);
			textureBuffer = ByteBuffer.allocateDirect(meshData.textureData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
			textureBuffer.put(meshData.textureData).position(0);
			normalBuffer = ByteBuffer.allocateDirect(meshData.normalData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
			normalBuffer.put(meshData.normalData).position(0);
			indexBuffer = ByteBuffer.allocateDirect(vboDataHandler.indexData.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
			indexBuffer.put(vboDataHandler.indexData).position(0);
				
			int[] vboBufs = new int[4];
			GLES20.glGenBuffers(vboBufs.length, vboBufs, 0);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboBufs[0]);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
			vboDataHandler.vboVertexHandle = vboBufs[0];
				
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboBufs[1]);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, normalBuffer.capacity() * 4, normalBuffer, GLES20.GL_STATIC_DRAW);
			vboDataHandler.vboNormalHandle = vboBufs[1];
				
	//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboBufs[2]);
	//		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorBuffer.capacity() * 4, colorBuffer, GLES20.GL_STATIC_DRAW);
	//		vboColorHandle = vboBufs[2];
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboBufs[2]);
			GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureBuffer.capacity() * 4, textureBuffer, GLES20.GL_STATIC_DRAW);
			vboDataHandler.vboTextureHandle = vboBufs[2];
	
			
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboBufs[3]);
			GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * 4, indexBuffer, GLES20.GL_STATIC_DRAW);
			vboDataHandler.vboIndexHandle = vboBufs[3];
			
			
			vertexBuffer.limit(0);
			normalBuffer.limit(0);
	//		colorBuffer.limit(0);
			indexBuffer.limit(0);
			textureBuffer.limit(0);
			
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		}
		facesCount+=vboDataHandler.facesCount;
		

//		vboVertexHandle = getVboBuffer(vertexBuffer, vertexData);
//		vboColorHandle = getVboBuffer(colorBuffer, meshData.textureData);
//		vboNormalHandle = getVboBuffer(normalBuffer, meshData.normalData);
//		vboIndexHandle = getVboBuffer(indexBuffer, indexData);
		
	}

	public void dealloc() {
		// TODO Auto-generated method stub
		
	}

	public void drawFrame(){
		//use program and pass buffers
		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());
		GLES20.glUseProgram(programHandle);
		GLES20.glUniform1f(isSelectedHandle, isSelected?1:0);
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

//		vertexBuffer.position(0);
//		GLES20.glVertexAttribPointer(positionHandle, vertexElementSize , GLES20.GL_FLOAT, false, 0, vertexBuffer);
//		GLES20.glEnableVertexAttribArray(positionHandle);
//		
//		colorBuffer.position(0);
//		GLES20.glVertexAttribPointer(colorHandle, colorElementSize, GLES20.GL_FLOAT, false, 0, colorBuffer);
//		GLES20.glEnableVertexAttribArray(colorHandle);
//		
//		normalBuffer.position(0);
//		GLES20.glVertexAttribPointer(normalHandle, normalElementSize, GLES20.GL_FLOAT, true, 0, normalBuffer);
//		GLES20.glEnableVertexAttribArray(normalHandle);
//		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexData.length/vertexElementSize);

//		-----------------------------------------------------
// using VBOs		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandler);
	    GLES20.glUniform1i(textureUniformHandle, 0);

	    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboVertexHandle);
		GLES20.glEnableVertexAttribArray(positionHandle);
		GLES20.glVertexAttribPointer(positionHandle, vertexElementSize, GLES20.GL_FLOAT, false, 0, 0);
		
//		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboColorHandle);
//		GLES20.glEnableVertexAttribArray(colorHandle);
//		GLES20.glVertexAttribPointer(colorHandle, colorElementSize, GLES20.GL_FLOAT, false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboNormalHandle);
		GLES20.glEnableVertexAttribArray(normalHandle);
		GLES20.glVertexAttribPointer(normalHandle, normalElementSize, GLES20.GL_FLOAT, false, 0, 0);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboDataHandler.vboTextureHandle);
		GLES20.glEnableVertexAttribArray(textureCoordHandle);
		GLES20.glVertexAttribPointer(textureCoordHandle, textureElementSize, GLES20.GL_FLOAT, false, 0, 0);
		
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vboDataHandler.vboIndexHandle);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, vboDataHandler.indexData.length, GLES20.GL_UNSIGNED_INT, 0);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        
        GLES20.glUseProgram(0);
	};

	protected int getVboBuffer(FloatBuffer buffer, float[] bufferData){
		buffer.put(bufferData).position(0);
		return getVboBuffer(buffer);
	}
	protected int getVboBuffer(IntBuffer buffer, int[] bufferData) {
		buffer.put(bufferData).position(0);
		return getVboBuffer(buffer);
	}

	private int getVboBuffer(Buffer buffer) {
		IntBuffer intBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
		intBuffer.position(0);
		GLES20.glGenBuffers(1, intBuffer);
		int vboHandle = intBuffer.get(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboHandle);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		return vboHandle;
	}


	public void rotate(float angleX, float angleY, float angleZ){
		rotate(new float[]{angleX, angleY, angleZ});
	}
	
	public void translate(float centerX, float centerY, float centerZ){
		translate(new float[]{centerX, centerY, centerZ});
	}
	
	public void rotate(float [] newAngleXYZ){
		this.angleXYZ = newAngleXYZ;
//		Matrix.setIdentityM(modelMatrix, 0);
//		Matrix.rotateM(modelMatrix, 0, 359, angleXYZ[0]/359, angleXYZ[1]/359, angleXYZ[2]/359);
		Matrix.rotateM(modelMatrix, 0, angleXYZ[0], 1, 0, 0);
		Matrix.rotateM(modelMatrix, 0, angleXYZ[1], 0, 1, 0);
		Matrix.rotateM(modelMatrix, 0, angleXYZ[2], 0, 0, 1);
//		Matrix.translateM(modelMatrix, 0, centerXYZ[0], centerXYZ[1], centerXYZ[2]);
	}
	
	public void translate(float[] newCenterXYZ){
		Matrix.translateM(modelMatrix, 0, -centerXYZ[0], -centerXYZ[1], -centerXYZ[2]);
		this.centerXYZ = newCenterXYZ;
		Matrix.translateM(modelMatrix, 0, centerXYZ[0], centerXYZ[1], centerXYZ[2]);
	}
	

	public void setModelMatrix(float[] modelMatrix) {
		this.modelMatrix = modelMatrix;
	}


	public float[] getModelMatrix() {
		return modelMatrix;
	}


	public void setAngle(float angleX, float angleY, float angleZ){
		setAngleXYZ(new float[]{angleX, angleY, angleZ});		
	}
	public void setAngleXYZ(float[] angleXYZ) {
		this.angleXYZ = angleXYZ;
		this.angleXYZ[0] = this.angleXYZ[0]%360;
		this.angleXYZ[1] = this.angleXYZ[1]%360;
		this.angleXYZ[2] = this.angleXYZ[2]%360;
//		rotate(this.angleXYZ);
	}

	public float[] getAngleXYZ() {
		return angleXYZ;
	}

	public void setCenterXYZ(float centerX, float centerY, float centerZ){
		setCenterXYZ(new float[]{centerX, centerY, centerZ});
	}
	public void setCenterXYZ(float[] centerXYZ) {
		this.centerXYZ = centerXYZ;
	}

	public float[] getCenterXYZ() {
		return centerXYZ;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isSelected() {
		return isSelected;
	}
	
//	public abstract float[] getVertexData();
//	public abstract float[] getColorData();
//	public abstract float[] getNormalData();
//	public abstract int[] 	getIndexData();
	public abstract int 	getMeshResource();
	

	public final int getVertexElementSize() {
		return vertexElementSize;
	}
	
	public final float[] getVertexData(){
		return vboDataHandlerMap.get(getClass().getSimpleName()).vertexData;
	}

	public Context getContext() {
		return context;
	}



}
