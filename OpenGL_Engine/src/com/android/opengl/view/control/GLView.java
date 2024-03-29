package com.android.opengl.view.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.opengl.GLES20;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.CommonGameObject.VboDataHandler;
import com.android.opengl.gameobject.GLScene;
import com.android.opengl.listener.ViewportChangeListener;
import com.android.opengl.shader.GLViewShader;
import com.android.opengl.util.GLUtil;
import com.android.opengl.util.LoaderManager;
import com.android.opengl.util.Log;
import com.android.opengl.util.ShaderManager;
import com.android.opengl.util.geometry.Rect2D;
import com.android.opengl.view.MotionEventDispatcher;
import com.android.opengl.view.Touchable;

public class GLView implements Touchable, ViewportChangeListener{
	
	protected static String TAG;

	private static int longTapTimeout = 600;
	
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
	private float mScaledBorderWidth;
	private float mScaledBorderHeight;
	private Rect2D mBoundariesRectInPixel;
	
	private float[] bkgColor = new float[4];

	protected GLViewShader mShader;
	private VboDataHandler mVboHandler;
	private OnTapListener mOnTapListener;
	private OnLongTapListener mOnLongTapListener;
	private int mBackgroundResId;

	protected GLView mFocusedView;

	protected GLView mParent;
	protected List<GLView> mChildren = Collections.synchronizedList(new ArrayList<GLView>());
	
	protected Camera mCamera;
	private int zOrder;
	
	protected boolean mIsVisible = true;
	
	private final int[] indexData = new int[]{0, 2, 3, 0, 1, 2,
											  4, 6, 7, 4, 5, 6,
											  8, 10, 11, 8, 9, 10,
											  12, 14, 15, 12, 13, 14,
											  16, 18, 19, 16, 17, 18}; 
	private final float[] instanceIdData = new float[]	{1, 1, 1, 1};
	
	private final float[] textureCoord = new float[]{0, 1,
												   1, 1, 
												   1, 0, 
												   0, 0};
	private float[] positionOffset = new float[2];

	private boolean isLongTapAccessible;
	private boolean isLongTapOccured;
//	private boolean isLongTapEnabled;
	
	public GLView(GLScene scene) {
		this.mCamera = scene.getCamera();
		init();
	}
	public GLView(GLScene scene, float left, float top, float width, float height) {
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
		TAG = GLView.class.getSimpleName() + ": " + getClass().getSimpleName();
		mShader = ShaderManager.getInstance().getShader(GLViewShader.class);
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
			mCamera.getViewportChangeListenerHolder().registerListener(this);
		}
	}

	
	public void setPosition(float leftCoord, float topCoord){
		onLayout(leftCoord, topCoord);
	}

	
	public void onDrawFrame(){
		if(mIsVisible){

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
		return mIsVisible;
	}

	public void setVisible(boolean isVisible) {
		if(!isVisible){
			sendActionCancel();
		}
		mIsVisible = isVisible;
		for(GLView child: mChildren){
			child.setVisible(isVisible);
		}
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
			mCamera.unregisterGLView(this);
			mCamera.registerGLView(this, zOrder);
		} else{
			mCamera.unregisterGLView(this);
			mCamera.registerGLView(this, parent.zOrder - zOrder - 1);
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
		if(!mIsVisible){
			return false;
		}
//		if (mFocusedView != null && mFocusedView != this){
//			return mFocusedView.onTouchEvent(event);
//			
//		}
//		for(int i = 0; i < mChildren.size(); ++i){
//			if(mChildren.get(i).onTouchEvent(event)){
//				return true;
//			}
//		}
		float x = event.getX();// * ratio;
		float y = event.getY();// * ratio;

		monitorForLongTap(event);
		

		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			resetLongTapMonitoringState();
//		case MotionEvent.ACTION_MOVE:
			if(getFocusedView() == this || mBoundariesRectInPixel.isWithinRect(x, y)){
//				Log.d("tag", "tap detected: " + this.getClass().getSimpleName());
				setFocusedView(this);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if(mBoundariesRectInPixel.isWithinRect(x, y) && getFocusedView() == this){
				Log.d("tag", "tap detected: " + this.getClass().getSimpleName());
				if(!isLongTapOccured && mOnTapListener != null){
					mOnTapListener.onTap(this);
				}
			}
		case MotionEvent.ACTION_CANCEL:
			handleActionCancel();
//			resetLongTapMonitoringState();
			break;

		default:
			break;
		}
		return false;
	}
	
	private void handleActionCancel() {
		setFocusedView(null);		
	}
	private void resetLongTapMonitoringState(){
		isLongTapAccessible = true;
		isLongTapOccured = false;
	}
	
	private void monitorForLongTap(MotionEvent event) {
		if(mOnLongTapListener == null || isLongTapOccured){
			return;
		}
		if(isLongTapAccessible && mBoundariesRectInPixel.isWithinRect(event.getX(), event.getY())){
			isLongTapAccessible = true;
		} else {
			isLongTapAccessible = false;
		}
		if(SystemClock.uptimeMillis() - event.getDownTime() >= longTapTimeout && isLongTapAccessible){
			mOnLongTapListener.onLongTap(this);
			isLongTapOccured = true;
			sendActionCancel();
		}
	}
	
	protected void sendActionCancel(){
		onTouchEvent(MotionEventDispatcher.obtainCancelEvent());
	}
	
	public void setBackground(int resourceId){
		mBackgroundResId = resourceId;
		mVboHandler.textureDataHandler = -1;
		if(mBackgroundResId > 0){
			mVboHandler.textureDataHandler = LoaderManager.getInstance(mCamera.getContext()).loadTexture(mBackgroundResId);
		}
	}
	
	public boolean isBackgroundEnabled(){
		return mVboHandler.textureDataHandler > 0;
	}
	
	
	private void setFocusedView(GLView glView) {
//		if(mParent != null){
//			mParent.setFocusedView(glView);
//		} else {
			mFocusedView = glView;
//		}
	}
	private GLView getFocusedView() {
//		if(mParent != null){
//			return mParent.getFocusedView();
//		}
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
		if(mIsVisible){
			return mBoundariesRectInPixel ;
		} else{
			return new Rect2D(); // returns empty rect
		}
	}

	public void release(){
		mCamera.unregisterGLView(this);
		sendActionCancel();
		removeChildren();
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
		for(GLView child: mChildren){
			child.release();
		}
		mChildren.clear();
		invalidate();
	}
	
	public List<GLView> getChildren(){
		return mChildren;
	}
	
	public int getBackgroundResId() {
		return mBackgroundResId;
	}


	public static interface OnTapListener{
		public void onTap(GLView glView);
	}
	
	public static interface OnLongTapListener{
		public void onLongTap(GLView glView);
	}
	
	@Override
	public void onViewportChanged(Rect2D newViewportRect) {
		invalidate();
	}
	
	public static int getLongTapTimeout() {
		return longTapTimeout;
	}
	public static void setLongTapTimeout(int longTapTimeout) {
		GLView.longTapTimeout = longTapTimeout;
	}
//	public boolean isLongTapEnabled() {
//		return isLongTapEnabled;
//	}
//	public void enableLongTap(boolean isLongTapEnabled) {
//		this.isLongTapEnabled = isLongTapEnabled;
//	}

	public void setOnLongTapListener(OnLongTapListener mOnLongTapListener) {
		this.mOnLongTapListener = mOnLongTapListener;
	}
	
//	private static class ViewGestureDetector{
//		
//		
//		public boolean onTouchEvent(MotionEvent event){
//			
//			return false;
//		}
//	}
	
	
}
