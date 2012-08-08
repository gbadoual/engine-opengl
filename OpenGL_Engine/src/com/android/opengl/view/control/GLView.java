package com.android.opengl.view.control;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.IvParameterSpec;

import android.content.Context;
import android.opengl.GLES20;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;

import com.android.opengl.gameobject.CommonGameObject;
import com.android.opengl.gameobject.CommonGameObject.VboDataHandler;
import com.android.opengl.gameobject.util.geometry.Point2D;
import com.android.opengl.shader.GLViewShader;

public abstract class GLView {
	
	private static final int SIZE_INT = 4;
	private static final int SIZE_FLOAT = 4;
	protected float leftCoord;
	protected float bottomCoord;
	protected float width;
	protected float height;
	protected float bkgColorR;
	protected float bkgColorG;
	protected float bkgColorB;
	protected float bkgColorA;
	
	private GLViewShader shader;
	private VboDataHandler vboDataHandler;
	private OnTapListener onTapListener;
	
	private GLView mParent;
	private List<GLView> mChildren = new ArrayList<GLView>();
	
	private static DisplayMetrics displayMetrics;
	
	float[] vertexData;
	private boolean isVisible = true;
	
	int[] indexData = new int[]{0, 2, 3, 0, 1, 2};
	
	public GLView(Context context) {
		if(displayMetrics == null){
			displayMetrics = context.getResources().getDisplayMetrics();
		}
		this.shader = new GLViewShader();
		vboDataHandler = new VboDataHandler();
		invalidate();
		


		bkgColorR = 128 / 255.0f;
		bkgColorG = 128 / 255.0f;
		bkgColorB = 128 / 255.0f;
		bkgColorA = 196 / 255.0f;

		float[] colorData = new float[]{bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA				};
		

		
		int[] vboBufs = new int[3];
		GLES20.glGenBuffers(vboBufs.length, vboBufs, 0);

		vboDataHandler.vboVertexHandle = vboBufs[0];
		vboDataHandler.vboColorHandle = vboBufs[1];
		vboDataHandler.vboIndexHandle = vboBufs[2];

		attachArrayToHandler(vertexData, vboDataHandler.vboVertexHandle);
		attachArrayToHandler(colorData, vboDataHandler.vboColorHandle);
		attachIndeciesToHandler(indexData, vboDataHandler.vboIndexHandle);
		
		
		
	}
	
	private void attachArrayToHandler(float[] data, int handler){
		FloatBuffer floatBuffer;
		floatBuffer = ByteBuffer.allocateDirect(data.length * SIZE_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		floatBuffer.put(data).position(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, handler);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, floatBuffer.capacity() * SIZE_FLOAT, floatBuffer, GLES20.GL_STATIC_DRAW);
		floatBuffer.limit(0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	private void attachIndeciesToHandler(int[] indexData, int handler){
		IntBuffer indexBuffer;
		indexBuffer = ByteBuffer.allocateDirect(indexData.length * SIZE_INT).order(ByteOrder.nativeOrder()).asIntBuffer();
		indexBuffer.put(indexData).position(0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, handler);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * SIZE_INT, indexBuffer, GLES20.GL_STATIC_DRAW);
		indexBuffer.limit(0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}	
	
	public void onDraw(){
		if(isVisible){
			boolean isDepthTestEnabled = GLES20.glIsEnabled(GLES20.GL_DEPTH_TEST);
			boolean isCulingTestEnabled = GLES20.glIsEnabled(GLES20.GL_CULL_FACE);
			if(isDepthTestEnabled){
				GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			}
			if(isCulingTestEnabled){
				GLES20.glDisable(GLES20.GL_CULL_FACE);
			}
			GLES20.glEnable(GLES20.GL_BLEND);
		    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

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
			if(isDepthTestEnabled){
				GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			}
			if(isCulingTestEnabled){
				GLES20.glEnable(GLES20.GL_CULL_FACE);
			}
			for(GLView glView: mChildren){
				glView.onDraw();
			}

		}				
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	

	protected void onLayout(float leftCoord, float bottomCoord){
		this.leftCoord = leftCoord;
		this.bottomCoord = bottomCoord;
		if(mParent != null){
			this.leftCoord += mParent.leftCoord;
			this.bottomCoord += mParent.bottomCoord;
		}
		notifyBoundsChange();
	}

	protected void onMeasure(float width, float height) {
		this.width = width;
		this.height = height;
		notifyBoundsChange();
	} 
	
	private void notifyBoundsChange(){
		float scaledLeftCoord = leftCoord / displayMetrics.widthPixels * 2 - 1;
		float scaledBottomCoord =  bottomCoord / displayMetrics.heightPixels * 2 - 1;
		float scaledWidth = width / displayMetrics.widthPixels * 2;
		float scaledHeight =  height / displayMetrics.heightPixels * 2;
		
		vertexData = new float[]{scaledLeftCoord, scaledBottomCoord, 0, 
				scaledLeftCoord + scaledWidth, scaledBottomCoord, 0,
				scaledLeftCoord + scaledWidth, scaledBottomCoord + scaledHeight, 0,
				scaledLeftCoord, scaledBottomCoord + scaledHeight, 0};		
		attachArrayToHandler(vertexData, vboDataHandler.vboVertexHandle);
	}
	
	public void invalidate(){
		onLayout(leftCoord, bottomCoord);
		for(GLView glView: mChildren){
			glView.reMeasure();
			glView.reLayout();
		}
		onMeasure(width, height);
		Log.i("tag", "GLView invalidate");
	}
	
	private void reMeasure(){
		for(GLView glView: mChildren){
			glView.reMeasure();
		}
		onMeasure(width, height);
	}
	private void reLayout(){
		onLayout(leftCoord, bottomCoord);
		for(GLView glView: mChildren){
			glView.reLayout();
		}		
	}
	
	public void addChild(GLView child){
		mChildren.add(child);
		child.setParent(this);
		invalidate();
	}

	public GLView getParent() {
		return mParent;
	}

	public void setParent(GLView parent) {
		this.mParent = parent;
	}
	
	public GLView removeChildAt(int index){
		if(index < mChildren.size()){
			GLView glView = mChildren.remove(index);
			if(glView != null){
				glView.setParent(null);
			}
			return glView;
		}
		return null;
	}
	public boolean removeChild(GLView child) {
		if(mChildren.remove(child)){
			child.setParent(null);
			return true;
		}
		return false;
	}
	
	public boolean onTouchEvent(MotionEvent event){
		for(GLView glView: mChildren){
			if(glView.onTouchEvent(event)){
				return true;
			}
		}
		
		float y = event.getY();
		if(displayMetrics != null){
			y = displayMetrics.heightPixels - y;
		}
		Log.i("tag", "x/y = " + event.getX() +"/" + event.getY());
		Log.i("tag", "left/bottom = " + leftCoord +"/" + bottomCoord);
		if(event.getX() >= leftCoord && event.getX() <= leftCoord + width &&
			y >= bottomCoord && y <= bottomCoord + height){
			if(onTapListener != null){
				return onTapListener.onTap(this);
			}
		}
		
		return false;
	}
	
	public OnTapListener getOnTapListener() {
		return onTapListener;
	}

	public void setOnTapListener(OnTapListener onTapListener) {
		this.onTapListener = onTapListener;
	}

	public static interface OnTapListener{
		public boolean onTap(GLView glView);
	}
	
}
