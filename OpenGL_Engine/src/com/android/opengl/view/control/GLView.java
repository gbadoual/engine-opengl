package com.android.opengl.view.control;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.opengl.GLES20;
import android.util.Log;
import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.CommonGameObject.VboDataHandler;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.shader.GLViewShader;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.LoaderManager;
import com.android.opengl.util.geometry.Rect2D;
import com.android.opengl.view.MotionEventDispatcher;
import com.android.opengl.view.Touchable;

public abstract class GLView implements Touchable{
	
	private float mParentShiftX = 0;
	private float mParentShiftY = 0;

	// dimensions are represented in percents from the largest side of the screen (in range 0 - 100)
	protected float mLeftCoord;
	protected float mTopCoord;
	protected float mWidth;
	protected float mHeight;
	protected Rect2D mBoundariesRectInPixel;
	
	
	protected float[] bkgColor = new float[4];

	protected GLViewShader mGLViewShader;
	private VboDataHandler mVboDataHandler;
	private OnTapListener mOnTapListener;

	protected GLView mFocusedView;

	protected GLView mParent;
	protected List<GLView> mChildren = new ArrayList<GLView>();
	
	protected Camera camera;
	private int zOrder;
	
	private boolean isVisible = true;
	
	private final int[] indexData = new int[]{0, 2, 3, 0, 1, 2};
	private final float[] textureCoord = new float[]{0, 1,
												   1, 1, 
												   1, 0, 
												   0, 0};
	protected float[] positionOffset = new float[2];
	
	public GLView(Scene scene) {
		this.camera = scene.getCamera();
		init();
	}
	public GLView(Scene scene, float left, float top, float width, float height) {
		this(scene.getCamera(), left, top, width, height);
	}

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
		this.mGLViewShader = new GLViewShader();
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
		GLUtil.attachArrayToHandler(textureCoord, mVboDataHandler.vboTextureCoordHandle);
		GLUtil.attachIndexesToHandler(indexData, mVboDataHandler.vboIndexHandle);
		if(mParent == null){
			camera.registerGLView(this, zOrder);
		}
	}


	
	public void onDraw(){
		if(isVisible){

			GLUtil.setGLState(GLES20.GL_DEPTH_TEST, false);
			GLUtil.setGLState(GLES20.GL_CULL_FACE, false);

			GLUtil.setGLState(GLES20.GL_BLEND, true);
			GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

			
			GLES20.glUseProgram(mGLViewShader.programHandle);
			
			GLES20.glUniform2fv(mGLViewShader.positionOffsetHandle, 1, positionOffset , 0);
			int isSelected = getFocusedView() == this?1:0;
			GLES20.glUniform1f(mGLViewShader.isPressedHandle, isSelected);

			GLES20.glUniform1f(mGLViewShader.isTextureEnabledHandle, isBackgroundEnabled()? 1 : 0);
			if(isBackgroundEnabled()){
			    GLUtil.passTextureToShader(mVboDataHandler.textureDataHandler, mGLViewShader.textureHandle);
			    GLUtil.passBufferToShader(mVboDataHandler.vboTextureCoordHandle, mGLViewShader.textureCoordHandle, 
			    								  GLUtil.TEXTURE_SIZE);
			} else {
				GLES20.glUniform4fv(mGLViewShader.colorHandle, 1, bkgColor, 0);
			}
			
			GLUtil.passBufferToShader(mVboDataHandler.vboVertexHandle, mGLViewShader.positionHandle, 
					  GLUtil.VERTEX_SIZE_2D);
			
			GLUtil.drawElements(mVboDataHandler.vboIndexHandle, indexData.length);
	        
	        GLES20.glUseProgram(0);	

	        GLUtil.restorePrevGLState(GLES20.GL_DEPTH_TEST);
			GLUtil.restorePrevGLState(GLES20.GL_CULL_FACE);
			GLUtil.restorePrevGLState(GLES20.GL_BLEND);
	        
			for(GLView glView: mChildren){
				glView.onDraw();
			}

		}				
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		if(!isVisible){
			onTouchEvent(MotionEventDispatcher.obtainCancelEvent());
		}
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
		
		float[] vertexData = new float[]{scaledLeftCoord, scaledTopCoord, 
				scaledLeftCoord + scaledWidth, scaledTopCoord,
				scaledLeftCoord + scaledWidth, scaledTopCoord + scaledHeight,
				scaledLeftCoord, scaledTopCoord + scaledHeight};		
		GLUtil.attachArrayToHandler(vertexData, mVboDataHandler.vboVertexHandle);
		float percentToPixelRatio = Math.max(camera.getViewportWidth(), camera.getViewportHeight()) / 100f;

		mBoundariesRectInPixel = new Rect2D((mLeftCoord + mParentShiftX) * percentToPixelRatio, (mTopCoord + mParentShiftY)*percentToPixelRatio, mWidth * percentToPixelRatio, mHeight * percentToPixelRatio);
	}
	
	public void setColor(float r, float g, float b, float a){
		bkgColor[0] = r / 255.0f;
		bkgColor[1] = g / 255.0f;
		bkgColor[2] = b / 255.0f;
		bkgColor[3] = a / 255.0f;

//		float[] colorData = new float[]{bkgColorR, bkgColorG, bkgColorB, bkgColorA,
//				bkgColorR, bkgColorG, bkgColorB, bkgColorA,
//				bkgColorR, bkgColorG, bkgColorB, bkgColorA,
//				bkgColorR, bkgColorG, bkgColorB, bkgColorA				};
//		
//		GLUtil.attachArrayToHandler(colorData, mVboDataHandler.vboColorHandle);

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
		if(mParent == null){
			camera.registerGLView(this, zOrder);
		} else{
			camera.unregisterGLView(this);
		}
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
		if(isVisible){
			return mBoundariesRectInPixel ;
		} else{
			return Rect2D.getEmpyRect();
		}
	}

	public void release(){
		camera.unregisterGLView(this);		
		mChildren.clear();
	}
	
	public int getzOrder() {
		return zOrder;
	}
	
	public void setzOrder(int zOrder) {
		this.zOrder = zOrder;
		if(mParent == null && camera.containsGLView(this)){
			camera.notifyGLViewzOrderChanged();
		}
	}


	public static class GLViewComparator implements Comparator<GLView> {
		@Override
		public int compare(GLView lhs, GLView rhs) {
			if(lhs != null && rhs != null){
				return lhs.zOrder - rhs.zOrder; 
			}
			return 0;
		}
	};
	
	public static interface OnTapListener{
		public void onTap(GLView glView);
	}
	
}
