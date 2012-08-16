package com.android.opengl.view.control;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.CommonGameObject;
import com.android.opengl.gameobject.CommonGameObject.VboDataHandler;
import com.android.opengl.shader.GLViewShader;
import com.android.opengl.util.LoaderManager;
import com.android.opengl.util.geometry.Rect2D;
import com.android.opengl.view.Touchable;

public abstract class GLView implements Touchable{
	
	private static final int SIZE_INT = 4;
	private static final int SIZE_FLOAT = 4;
	private static final String TAG = GLView.class.getSimpleName();
	
	private float mParentShiftX = 0;
	private float mParentShiftY = 0;
	protected float mLeftCoord;
	protected float mTopCoord;
	protected float mWidth;
	protected float mHeight;
	protected Rect2D mBoundariesRectInPixel;
	
	
	protected float bkgColorR;
	protected float bkgColorG;
	protected float bkgColorB;
	protected float bkgColorA;

	private GLViewShader mShader;
	private VboDataHandler mVboDataHandler;
	private OnTapListener mOnTapListener;

	protected GLView mFocusedView;

	protected GLView mParent;
	protected List<GLView> mChildren = new ArrayList<GLView>();
	
	private Camera camera;
	
	private boolean isVisible = true;
	
	int[] indexData = new int[]{0, 2, 3, 0, 1, 2};
	float[] textureCoord = new float[]{0, 1,
									   1, 1, 
									   1, 0, 
									   0, 0};
	
	public GLView(Camera camera) {
		this.camera = camera;
		init();
	}
	public GLView(Camera camera, float left, float top, float width, float height) {
		this.camera = camera;
		this.mLeftCoord = left;
		this.mTopCoord = top;
		this.mWidth = width;
		this.mHeight = height;
		init();
	}
	
	private void init() {
		this.mShader = new GLViewShader();
		mVboDataHandler = new VboDataHandler();
		mBoundariesRectInPixel = new Rect2D();

		int[] vboBufs = new int[4];
		GLES20.glGenBuffers(vboBufs.length, vboBufs, 0);

		mVboDataHandler.vboVertexHandle = vboBufs[0];
		mVboDataHandler.vboColorHandle = vboBufs[1];
		mVboDataHandler.vboIndexHandle = vboBufs[2];
		mVboDataHandler.vboTextureCoordHandle = vboBufs[3];

		setColor(128, 128, 128, 192);
		invalidate();
		attachArrayToHandler(textureCoord, mVboDataHandler.vboTextureCoordHandle);
		attachIndeciesToHandler(indexData, mVboDataHandler.vboIndexHandle);
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

			GLES20.glUseProgram(mShader.programHandle);
			GLES20.glUniform1f(mShader.isPressedHandle, getFocusedView() == this?1:0);

			GLES20.glUniform1f(mShader.isTextureEnabledHandle, isBackgroundEnabled()? 1 : 0);
			if(isBackgroundEnabled()){
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mVboDataHandler.textureDataHandler);
			    GLES20.glUniform1i(mShader.textureHandle, 0);

				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboDataHandler.vboTextureCoordHandle);
				GLES20.glEnableVertexAttribArray(mShader.textureCoordHandle);
				GLES20.glVertexAttribPointer(mShader.textureCoordHandle, CommonGameObject.TEXTURE_ELEMENT_SIZE, GLES20.GL_FLOAT, false, 0, 0);
			}
			
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboDataHandler.vboVertexHandle);
			GLES20.glEnableVertexAttribArray(mShader.positionHandle);
			GLES20.glVertexAttribPointer(mShader.positionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
			
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboDataHandler.vboColorHandle);
			GLES20.glEnableVertexAttribArray(mShader.colorHandle);
			GLES20.glVertexAttribPointer(mShader.colorHandle, 4, GLES20.GL_FLOAT, false, 0, 0);
			
			
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVboDataHandler.vboIndexHandle);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexData.length, GLES20.GL_UNSIGNED_INT, 0);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
			GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	        
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
	

	protected void onLayout(float leftCoord, float topCoord){
		if(mParent != null){
			mParentShiftX = mParent.mLeftCoord;
			mParentShiftY = mParent.mTopCoord;
		}

		this.mLeftCoord = leftCoord;
		this.mTopCoord = topCoord;
		notifyBoundsChange();
	}

	protected void onMeasure(float width, float height) {
		this.mWidth = width;
		this.mHeight = height;
		notifyBoundsChange();
	} 
	
	private void notifyBoundsChange(){
		float aspectRatio = ((float)camera.getViewportWidth())/camera.getViewportHeight();
		float aspectRatioX;
		float aspectRatioY;

		if(aspectRatio > 1){
			aspectRatioX = 2.0f / 100;
			aspectRatioY = 2.0f / (100 / aspectRatio);
		} else {
			aspectRatioX = 2.0f / (100 * aspectRatio);
			aspectRatioY = 2.0f / 100;
		}
		float scaledLeftCoord = (mLeftCoord + mParentShiftX) * aspectRatioX - 1;
		float scaledTopCoord = 1 - (mTopCoord + mParentShiftY)* aspectRatioY;
		float scaledWidth = mWidth * aspectRatioX;
		float scaledHeight =  - mHeight * aspectRatioY;
		
		float[] vertexData = new float[]{scaledLeftCoord, scaledTopCoord, 0, 
				scaledLeftCoord + scaledWidth, scaledTopCoord, 0,
				scaledLeftCoord + scaledWidth, scaledTopCoord + scaledHeight, 0,
				scaledLeftCoord, scaledTopCoord + scaledHeight, 0};		
		attachArrayToHandler(vertexData, mVboDataHandler.vboVertexHandle);
		float percentToPixelRatio = Math.max(camera.getViewportWidth(), camera.getViewportHeight()) / 100f;

		mBoundariesRectInPixel = new Rect2D((mLeftCoord + mParentShiftX) * percentToPixelRatio, (mTopCoord + mParentShiftY)*percentToPixelRatio, mWidth * percentToPixelRatio, mHeight * percentToPixelRatio);
	}
	
	public void setColor(float r, float g, float b, float a){
		bkgColorR = r / 255.0f;
		bkgColorG = g / 255.0f;
		bkgColorB = b / 255.0f;
		bkgColorA = a / 255.0f;

		float[] colorData = new float[]{bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA,
				bkgColorR, bkgColorG, bkgColorB, bkgColorA				};
		
		attachArrayToHandler(colorData, mVboDataHandler.vboColorHandle);

	}
	
	public void invalidate(){
		onLayout(mLeftCoord, mTopCoord);
		for(GLView glView: mChildren){
			glView.reMeasure();
			glView.reLayout();
		}
		onMeasure(mWidth, mHeight);
		Log.i("tag", "GLView invalidate");
	}
	
	private void reMeasure(){
		for(GLView glView: mChildren){
			glView.reMeasure();
		}
		onMeasure(mWidth, mHeight);
	}
	private void reLayout(){
		onLayout(mLeftCoord, mTopCoord);
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
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(!isVisible){
			return false;
		}
		if (mFocusedView != null && mFocusedView != this){
			return mFocusedView.onTouchEvent(event);
			
		}
		for(GLView glView: mChildren){
			if(glView.onTouchEvent(event)){
				return true;
			}
		}
		float x = event.getX();// * ratio;
		float y = event.getY();// * ratio;
//		Log.i("tag", "x/y = " + x +"/" + y);
//		Log.i("tag", "left/bottom = " + (leftCoord + parentShiftX) +"/" + (bottomCoord + parentShiftY));

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			if(getFocusedView() == this || mBoundariesRectInPixel.isWithinRect(x, y)){
//				Log.d("tag", "tap detected: " + this.getClass().getSimpleName());
				setFocusedView(this);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if(mBoundariesRectInPixel.isWithinRect(x, y) && getFocusedView() == this){
				Log.d("tag", "tap detected: " + this.getClass().getSimpleName());
				if(mOnTapListener != null){
					mOnTapListener.onTap(this);
				}
			}
		case MotionEvent.ACTION_CANCEL:
			setFocusedView(null);
			break;
			

		default:
			break;
		}
		return false;
	}
	
	
	public void setBackground(int resourceId){
		mVboDataHandler.textureDataHandler = -1;
		if(resourceId > 0){
			mVboDataHandler.textureDataHandler = LoaderManager.getInstance(camera.getContext()).loadTexture(resourceId);
		}
	}
	
	public boolean isBackgroundEnabled(){
		return mVboDataHandler.textureDataHandler > 0;
	}
	
	
	private void setFocusedView(GLView glView) {
		if(mParent != null){
			mParent.setFocusedView(glView);
		} else {
			mFocusedView = glView;
		}
	}
	private GLView getFocusedView() {
		if(mParent != null){
			return mParent.getFocusedView();
		}
		return	mFocusedView;
	}
	
	public OnTapListener getOnTapListener() {
		return mOnTapListener;
	}

	public void setOnTapListener(OnTapListener onTapListener) {
		this.mOnTapListener = onTapListener;
	}
	
	@Override
	public Rect2D getBoundariesRectInPixel() {
		return mBoundariesRectInPixel ;
	}

	
	
	public static interface OnTapListener{
		public void onTap(GLView glView);
	}
	
}
