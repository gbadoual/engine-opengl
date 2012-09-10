package com.android.opengl.gameobject;

import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import com.android.opengl.Clan;
import com.android.opengl.R;
import com.android.opengl.shader.ObjectShader;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.LoaderManager;
import com.android.opengl.util.geometry.Matrix;
import com.android.opengl.util.geometry.Point3D;

public abstract class CommonGameObject {
	
	private static final String TAG = CommonGameObject.class.getSimpleName();

	public static long facesCount = 0;
	
//	protected CommonShader shader;
	
	protected static Map<String, VboDataHandler> vboDataHandlerMap = new HashMap<String, VboDataHandler>();
	
	protected float[] modelMatrix = new float[16];
	protected float[] mvpMatrix = new float[16];
	protected float[] mvMatrix = new float[16];

//	protected float[] centerXYZ = new float[3];
	protected float[] angleXYZ = new float[3];
	
//	protected float[] vertexData;
//	protected int indexDataLength;

	protected boolean isSelected;
	protected Clan mClan = Clan.NEUTRAL;
	
	
	protected LoaderManager meshLoader;
	protected Resources resources;
	protected LoaderManager.MeshData meshData;


	public static class VboDataHandler{
		public int vboVertexHandle;
		public int vboColorHandle;
		public int vboTextureCoordHandle;
		public int vboNormalHandle;
		public int vboIndexHandle;
		public int indexDataLength;
		public float[] vertexData;
		public int [] indexData;

		public long facesCount;
		public int textureDataHandler = -1;
	}
	
	public CommonGameObject(Resources resources) {
		Log.d(TAG, "init " + getClass().getSimpleName());
		long time = System.currentTimeMillis(); 
		this.resources = resources;
//		this.shader = shader;
		meshLoader = LoaderManager.getInstance(resources);
		initData();
		time = System.currentTimeMillis() - time;
		Log.d(TAG, getClass().getSimpleName() + " loaded for " + time / 1000.0d + " sec.");
		
	}
	
	private void initData() {
//		GLES20.glEnable(GLES20.GL_BLEND);
//	    GLES20.glBlendFunc (GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	    
		Matrix.setIdentityM(modelMatrix, 0);

		VboDataHandler vboDataHandler = vboDataHandlerMap.get(getClass().getSimpleName());
		if (vboDataHandler == null){
			vboDataHandler = new VboDataHandler();

			GLES20.glEnable(GLES20.GL_TEXTURE_2D);
			vboDataHandler.textureDataHandler = meshLoader.loadTexture(getTextureResource()); 
			vboDataHandlerMap.put(getClass().getSimpleName(), vboDataHandler);
			meshData = meshLoader.loadMeshData(getMeshResource());
			if(meshData == null){
				Log.w("tag",TAG+":initDtata() meshData is null. Init aborting.");
				return;
			}
			vboDataHandler.vertexData = meshData.vertexData;
			vboDataHandler.indexData = meshData.indexData;
			vboDataHandler.facesCount = meshData.facesCount;
			int[] vboBufs = new int[4];
			GLES20.glGenBuffers(vboBufs.length, vboBufs, 0);
			vboDataHandler.vboVertexHandle = vboBufs[0];
			vboDataHandler.vboNormalHandle = vboBufs[1];
			vboDataHandler.vboTextureCoordHandle = vboBufs[2];
			vboDataHandler.vboIndexHandle = vboBufs[3];
			
			GLUtil.attachArrayToHandler(meshData.vertexData, vboDataHandler.vboVertexHandle);
			GLUtil.attachArrayToHandler(meshData.normalData, vboDataHandler.vboNormalHandle);
			GLUtil.attachArrayToHandler(meshData.textureData, vboDataHandler.vboTextureCoordHandle);
			GLUtil.attachIndexesToHandler(meshData.indexData, vboDataHandler.vboIndexHandle);

//			vboDataHandler.vboVertexHandle = GLRenderHelper.attachArrayToHandler(meshData.vertexData);
//			vboDataHandler.vboNormalHandle = GLRenderHelper.attachArrayToHandler(meshData.normalData);
//			vboDataHandler.vboTextureCoordHandle = GLRenderHelper.attachArrayToHandler(meshData.textureData);
//			GLRenderHelper.attachIndexesToHandler(meshData.indexData, vboDataHandler.vboIndexHandle);


		}
		facesCount+=vboDataHandler.facesCount;
		

	}

	public void release() {
		
	}

	public abstract void onDrawFrame();



	public void rotate(float angleX, float angleY, float angleZ){
		rotate(new float[]{angleX, angleY, angleZ});
	}
	
	public void rotate(float [] newAngleXYZ){
		this.angleXYZ = newAngleXYZ;
		Matrix.rotateRad(modelMatrix, angleXYZ[0], angleXYZ[1], angleXYZ[2]);
	}
	
	public void rotateY(float angle) {
		Matrix.rotateRadY(modelMatrix, angle);
		
	}
	
	public void onSelected(){
		isSelected = true;
	}



	public void setModelMatrix(float[] modelMatrix) {
		this.modelMatrix = modelMatrix;
	}


	public float[] getModelMatrix() {
		return modelMatrix;
	}

	public float[] getAngleXYZ() {
		return angleXYZ;
	}
	public void setPosition(float x, float y, float z) {
		modelMatrix[12] = x;
		modelMatrix[13] = y;
		modelMatrix[14] = z;
	}

	public Point3D getPosition() {
		return new Point3D(modelMatrix[12], modelMatrix[13], modelMatrix[14]);
	}

	public void incPosition(Point3D inc) {
		Point3D position = getPosition();
		setPosition(position.x + inc.x, position.y + inc.y, position.z + inc.z);
	}

	public void setPosition(Point3D position) {
		setPosition(position.x, position.y, position.z);
	}
	
	public void setPosition(float[] position){
		setPosition(position[0], position[1], position[2]);
	}
	
	public void onObjectFocusChanged(boolean hasFocus) {
	}


	public boolean isSelected() {
		return isSelected;
	}
	
	public abstract int getMeshResource();
	
	public int getTextureResource(){
		return R.raw.world_map;
	};
	

	public final float[] getVertexData(){
		return vboDataHandlerMap.get(getClass().getSimpleName()).vertexData;
	}
	
	public Point3D getUpVector() {
		return new Point3D(
				modelMatrix[Matrix.UP_X_OFFSET],
				modelMatrix[Matrix.UP_Y_OFFSET],
				modelMatrix[Matrix.UP_Z_OFFSET]);
	}

	public Point3D getDirection(){
		return new Point3D(
				modelMatrix[Matrix.VIEX_X_OFFSET],
				modelMatrix[Matrix.VIEX_Y_OFFSET],
				modelMatrix[Matrix.VIEX_Z_OFFSET]);
	}

//	public CommonShader getShader() {
//		return shader;
//	}
	
	public float getPosX(){
		return modelMatrix[Matrix.POS_X_OFFSET];
	} 
	public float getPosY(){
		return modelMatrix[Matrix.POS_Y_OFFSET];
	} 
	public float getPosZ(){
		return modelMatrix[Matrix.POS_Z_OFFSET];
	} 


	public void notifyViewportChanged(int width, int height) {
		
	}

}
