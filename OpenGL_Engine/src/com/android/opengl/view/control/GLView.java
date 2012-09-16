package com.android.opengl.view.control;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.opengl.GLES20;
import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.Camera.ViewportChangeListener;
import com.android.opengl.gameobject.CommonGameObject.VboDataHandler;
import com.android.opengl.gameobject.Scene;
import com.android.opengl.shader.GLViewShader;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.LoaderManager;
import com.android.opengl.util.Log;
import com.android.opengl.util.geometry.Rect2D;
import com.android.opengl.view.MotionEventDispatcher;
import com.android.opengl.view.Touchable;

public class GLView implements Touchable, ViewportChangeListener{
	
	private static final String TAG = GLView.class.getSimpleName();

	
	private float mParentShiftX = 0;
	private float mParentShiftY = 0;

	// dimensions are represented in percents from the largest side of the screen (in range 0 - 100)
	protected float mLeftCoord;
	protected float mTopCoord;
	protected float mWidth;
	protected float mHeight;
	private float mBorderWidth;

	protected float mScaledWidth;
	protected float mScaledHeight;
	protected float mScaledBorderWidth;
	protected float mScaledBorderHeight;
	protected Rect2D mBoundariesRectInPixel;
	
	protected float[] bkgColor = new float[4];

	protected GLViewShader mShader;
	protected VboDataHandler mVboHandler;
	private OnTapListener mOnTapListener;

	protected GLView mFocusedView;

	protected GLView mParent;
	protected List<GLView> mChildren = new ArrayList<GLView>();
	
	protected Camera mCamera;
	private int zOrder;
	
	private boolean isVisible = true;
	
	protected final int[] indexData = new int[]{0, 2, 3, 0, 1, 2,
											  4, 6, 7, 4, 5, 6,
											  8, 10, 11, 8, 9, 10,
											  12, 14, 15, 12, 13, 14,
											  16, 18, 19, 16, 17, 18}; 
	private final float[] instanceIdData = new float[]	{1, 1, 1, 1};
	
	private final float[] textureCoord = new float[]{0, 1,
												   1, 1, 
												   1, 0, 
												   0, 0};
	protected float[] positionOffset = new float[2];
	
	public GLView(Scene scene) {
		this.mCamera = scene.getCamera();
		init();
	}
	public GLView(Scene scene, float left, float top, float width, float height) {
		this(scene.getCamera(), left, top, width, height);
	}

	public GLView(Camera camera) {
		this.mCamera = camera;
		init();
	}
	
	public GLView(Camera camera, float left, float top, float width, float height) {
		this.mCamera = camera;
		this.mLeftCoord = left;
		this.mTopCoord = top;
		this.mWidth = width;
		this.mHeight = height;
		init();
	}
	
	private void init() {
		mShader = new GLViewShader();
		mVboHandler = new VboDataHandler();
		mBoundariesRectInPixel = new Rect2D();

		int[] vboBufs = new int[5];
		GLES20.glGenBuffers(vboBufs.length, vboBufs, 0);

		mVboHandler.vboVertexHandle = vboBufs[0];
		mVboHandler.vboColorHandle = vboBufs[1];
		mVboHandler.vboIndexHandle = vboBufs[2];
		mVboHandler.vboTextureCoordHandle = vboBufs[3];
		mVboHandler.vboInstanceIdHandle = vboBufs[4];

		setColor(128, 128, 128, 192);
		invalidate();
		setBorderWidth(0.2f);
		GLUtil.attachArrayToHandler(textureCoord, mVboHandler.vboTextureCoordHandle);
		GLUtil.attachArrayToHandler(instanceIdData, mVboHandler.vboInstanceIdHandle);
		GLUtil.attachIndexesToHandler(indexData, mVboHandler.vboIndexHandle);
		if(mParent == null){
			mCamera.registerGLView(this, zOrder);
			mCamera.registerViewportChangeListener(this);
		}
	}


	
	public void onDrawFrame(){
		if(isVisible){

			GLUtil.setGLState(GLES20.GL_DEPTH_TEST, false);
			GLUtil.setGLState(GLES20.GL_CULL_FACE, false);

			GLUtil.setGLState(GLES20.GL_BLEND, true);
			GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

			
			GLUtil.glUseProgram(mShader.programHandle);
			
			GLES20.glUniform2fv(mShader.positionOffsetHandle, 1, positionOffset , 0);
			int isSelected = getFocusedView() == this?1:0;
			GLES20.glUniform1f(mShader.isPressedHandle, isSelected);

			GLES20.glUniform1f(mShader.isTextureEnabledHandle, isBackgroundEnabled()? 1 : 0);
			if(isBackgroundEnabled()){
			    GLUtil.passTextureToShader(mVboHandler.textureDataHandler, mShader.textureHandle);
			    GLUtil.passBufferToShader(mVboHandler.vboTextureCoordHandle, mShader.textureCoordHandle, GLUtil.TEXTURE_SIZE);
			} else {
				GLES20.glUniform4fv(mShader.colorHandle, 1, bkgColor, 0);
			}
			
			GLUtil.passBufferToShader(mVboHandler.vboVertexHandle, mShader.positionHandle, GLUtil.VERTEX_SIZE_2D);
			GLUtil.passBufferToShader(mVboHandler.vboInstanceIdHandle, mShader.instanceIdHandle, 1);
			
			GLUtil.drawElements(mVboHandler.vboIndexHandle, indexData.length);
	        
	        GLUtil.glUseProgram(0);	

	        GLUtil.restorePrevGLState(GLES20.GL_DEPTH_TEST);
			GLUtil.restorePrevGLState(GLES20.GL_CULL_FACE);
			GLUtil.restorePrevGLState(GLES20.GL_BLEND);

			for(GLView glView: mChildren){
				glView.onDrawFrame();
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
		if(mWidth != width || mHeight != height){
			this.mWidth = width;
			this.mHeight = height;
			notifyBoundsChange();
		}
	} 
	
	
	private void notifyBoundsChange(){
		
		float scaledLeftCoord = scalePercentToWorldX(mLeftCoord + mParentShiftX);
		float scaledTopCoord = scalePercentToWorldY(mTopCoord + mParentShiftY);
		float xFlip = mWidth > 0 ? 1:-1;
		float yFlip = mHeight > 0 ? 1:-1;
 
		mScaledWidth = ensurePercentToPixelAligment(mWidth) * Camera.percentToWorldRatioX;
		mScaledHeight = - ensurePercentToPixelAligment(mHeight) * Camera.percentToWorldRatioY;
		mScaledBorderWidth *= xFlip;
		mScaledBorderHeight *= yFlip;
		float scaledRightCoord = scaledLeftCoord + mScaledWidth;
		float scaledBottomCoord = scaledTopCoord + mScaledHeight;

		float[] vertexData = new float[]{
				//inner field
				scaledLeftCoord + mScaledBorderWidth, scaledTopCoord - mScaledBorderHeight, 
				scaledRightCoord - mScaledBorderWidth, scaledTopCoord - mScaledBorderHeight,
				scaledRightCoord - mScaledBorderWidth, scaledBottomCoord + mScaledBorderHeight,
				scaledLeftCoord + mScaledBorderWidth, scaledBottomCoord + mScaledBorderHeight,

				//border
				scaledLeftCoord, scaledTopCoord, 
				scaledLeftCoord + mScaledBorderWidth, scaledTopCoord,
				scaledLeftCoord + mScaledBorderWidth, scaledBottomCoord,
				scaledLeftCoord, scaledBottomCoord,

				scaledRightCoord, scaledTopCoord, 
				scaledRightCoord - mScaledBorderWidth, scaledTopCoord,
				scaledRightCoord - mScaledBorderWidth, scaledBottomCoord,
				scaledRightCoord, scaledBottomCoord,
				
				scaledLeftCoord + mScaledBorderWidth, scaledTopCoord, 
				scaledRightCoord - mScaledBorderWidth, scaledTopCoord,
				scaledRightCoord - mScaledBorderWidth, scaledTopCoord - mScaledBorderHeight,
				scaledLeftCoord + mScaledBorderWidth, scaledTopCoord - mScaledBorderHeight,
				
				scaledLeftCoord + mScaledBorderWidth, scaledBottomCoord, 
				scaledRightCoord - mScaledBorderWidth, scaledBottomCoord,
				scaledRightCoord - mScaledBorderWidth, scaledBottomCoord + mScaledBorderHeight,
				scaledLeftCoord + mScaledBorderWidth, scaledBottomCoord + mScaledBorderHeight,
				
				};		
		GLUtil.attachArrayToHandler(vertexData, mVboHandler.vboVertexHandle);


		mBoundariesRectInPixel = new Rect2D((mLeftCoord + mParentShiftX) * Camera.percentToScreenRatio, 
											(mTopCoord + mParentShiftY) * Camera.percentToScreenRatio, 
											mWidth * Camera.percentToScreenRatio, 
											mHeight * Camera.percentToScreenRatio);
		mScaledBorderWidth *= xFlip;
		mScaledBorderHeight *= yFlip;
	}
	
	private float scalePercentToWorldY(float screenCoordY) {
		return 1 - screenCoordY * Camera.percentToWorldRatioY;
	}
	private float scalePercentToWorldX(float screenCoordX) {
		return screenCoordX  * Camera.percentToWorldRatioX - 1;
	}

	
	private void setScaledBorderWidth() {
		float pixelEnsuredBorderWidth = ensurePercentToPixelAligment(mBorderWidth);

		mScaledBorderWidth = pixelEnsuredBorderWidth * Camera.percentToWorldRatioX;
		mScaledBorderHeight = pixelEnsuredBorderWidth * Camera.percentToWorldRatioY;
	}
	protected float ensurePercentToPixelAligment(float value){
		return Math.round(value * Camera.percentToScreenRatio) * Camera.screenToPercentRatio;
	}
	
	public void setColor(float r, float g, float b, float a){
		bkgColor[0] = r / 255.0f;
		bkgColor[1] = g / 255.0f;
		bkgColor[2] = b / 255.0f;
		bkgColor[3] = a / 255.0f;
	}
	
	public void invalidate(){
		setScaledBorderWidth();
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
			mCamera.registerGLView(this, zOrder);
		} else{
			mCamera.unregisterGLView(this);
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
		mVboHandler.textureDataHandler = -1;
		if(resourceId > 0){
			mVboHandler.textureDataHandler = LoaderManager.getInstance(mCamera.getContext()).loadTexture(resourceId);
		}
	}
	
	public boolean isBackgroundEnabled(){
		return mVboHandler.textureDataHandler > 0;
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
	
	protected void setPositionOffset(float xOffset, float yOffset) {
		positionOffset[0] = xOffset;		
		positionOffset[1] = yOffset;		
	}
	protected void setPositionOffsetFromScreenCoords(float xOffset, float yOffset) {
		
		positionOffset[0] = scalePercentToWorldX(xOffset);		
		positionOffset[1] = scalePercentToWorldY(yOffset);		
	}
	
	public float getPositionOffsetX(){
		return positionOffset[0];
	}

	public float getPositionOffsetY(){
		return positionOffset[1];
	}
	
	@Override
	public Rect2D getBoundariesRectInPixel() {
		if(isVisible){
			return mBoundariesRectInPixel ;
		} else{
			return new Rect2D(); // returns empty rect
		}
	}

	public void release(){
		mCamera.unregisterGLView(this);		
		mChildren.clear();
	}
	
	public int getzOrder() {
		return zOrder;
	}
	
	public void setzOrder(int zOrder) {
		this.zOrder = zOrder;
		if(mParent == null && mCamera.containsGLView(this)){
			mCamera.notifyGLViewzOrderChanged();
		}
	}


	public float getBorderWidth() {
		return mBorderWidth;
	}
	public void setBorderWidth(float borderWidth) {
		mBorderWidth = borderWidth;
		setScaledBorderWidth();
	}


	// to ensure view be on the top it should be drawn the last 
	// unlike touchable which should be touched first
	public static class GLViewComparator implements Comparator<GLView> {
		@Override
		public int compare(GLView lhs, GLView rhs) {
			if(lhs != null && rhs != null){
				return rhs.zOrder - lhs.zOrder; 
			}
			return 0;
		}
	};
	public void removeChildren() {
		mChildren.clear();
		invalidate();
	}
	
	public List<GLView> getChildren(){
		return mChildren;
	}

	public static interface OnTapListener{
		public void onTap(GLView glView);
	}

	@Override
	public void onViewportChanged(Rect2D newViewportRect) {
		invalidate();
	}
	
}
