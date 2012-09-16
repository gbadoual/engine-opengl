package com.android.opengl.view.control;

import java.util.ArrayList;
import java.util.List;

import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.R;
import com.android.opengl.Camera.ViewportChangeListener;
import com.android.opengl.util.Log;
import com.android.opengl.util.geometry.Rect2D;
import com.android.opengl.view.MotionEventDispatcher;
import com.android.opengl.view.Touchable;

public class GLSelectionRegion extends GLView{
	
	private GLSelectionSurface mGLSelectionSurface;

	public GLSelectionRegion(Camera camera) {
		super(camera);
		onMeasure(10, 10);
		onLayout(5, 40);
		setzOrder(40);
		mGLSelectionSurface = new GLSelectionSurface(camera);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			mCamera.registerTouchable(mGLSelectionSurface, getzOrder());
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mCamera.unregisterTouchable(mGLSelectionSurface);
			mGLSelectionSurface.onTouchEvent(MotionEventDispatcher.obtainCancelEvent());
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}
	
	public void setRectColor(int r, int g, int b, int a){
		mGLSelectionSurface.mSelectionRectangle.setColor(r, g, b, a);
	}
	
	public boolean registerSelectionListener(RegionSelectionListener listener){
		return mGLSelectionSurface.mRegionSelectionListenerList.add(listener);
	}

	public boolean unregisterSelectionListener(RegionSelectionListener listener){
		return mGLSelectionSurface.mRegionSelectionListenerList.remove(listener);
	}
	
	@Override
	public void release() {
		mGLSelectionSurface.release();
		super.release();
	}

	@Override
	public void onViewportChanged(Rect2D newViewportRect) {
		mGLSelectionSurface.onViewportChanged(newViewportRect);
		super.onViewportChanged(newViewportRect);
	}
	
	public static class GLSelectionSurface implements Touchable, ViewportChangeListener	{
		private List<RegionSelectionListener> mRegionSelectionListenerList = new ArrayList<GLSelectionRegion.RegionSelectionListener>();
		private GLView mSelectionRectangle;
		private Camera mCamera;
		
		private Rect2D mSurfaceBounds = new Rect2D();
		

		public GLSelectionSurface(Camera camera) {
			mCamera = camera;
			mSurfaceBounds = new Rect2D(0, 0, mCamera.getViewportWidth(), mCamera.getViewportHeight());
			mSelectionRectangle = new GLView(mCamera);
			mCamera.unregisterGLView(mSelectionRectangle);
			mSelectionRectangle.setColor(0, 40, 50, 0);
			mSelectionRectangle.setBorderWidth(0.3f);
		}

		
		public void release() {
			mRegionSelectionListenerList.clear();
			mSelectionRectangle.release();
		}
		
		public boolean registerSelectionListener(RegionSelectionListener listener){
			return mRegionSelectionListenerList.add(listener);
		}
		public boolean unregisterSelectionListener(RegionSelectionListener listener){
			return mRegionSelectionListenerList.remove(listener);
		}

		public Rect2D getSelectionRectangleBounds(){
			return mSelectionRectangle.getBoundariesRectInPixel();
		}

		
		public void setBorderPosition(float screenX, float screenY){
//			Log.d("tag", "setPos x/y: " + screenX + "/" + screenY);
			mSelectionRectangle.onLayout(screenX * Camera.screenToPercentRatio, screenY * Camera.screenToPercentRatio);
		}
		
		public void setBorderDimensions(float screenX, float screenY) {
			float width = screenX * Camera.screenToPercentRatio - mSelectionRectangle.mLeftCoord;
			float height = screenY * Camera.screenToPercentRatio - mSelectionRectangle.mTopCoord;
//			Log.d("tag", "setDim w/h: " + width + "/" + height);
			mSelectionRectangle.onMeasure(width, height);
		}
		public void initSelectionRectangle(){
			mCamera.unregisterGLView(mSelectionRectangle);
			mCamera.registerGLView(mSelectionRectangle, 100);
			mCamera.unregisterTouchable(mSelectionRectangle);
			mSelectionRectangle.onMeasure(0, 0);
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
//			Log.v("tag", "pointerIndex = " + event.getActionIndex());
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				setBorderPosition(event.getX(), event.getY());
				initSelectionRectangle();
				break;
			case MotionEvent.ACTION_MOVE:
//				Log.d("tag", "move x/y: " + border.mLeftCoord + "/" + border.mTopCoord);
				setBorderDimensions(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_UP:
				Rect2D selectedRegion = getSelectionRectangleBounds();
				selectedRegion.normalize();
				if(selectedRegion.mWidth > 0 && selectedRegion.mHeight > 0){
					Log.d("tag", "regionSelected: " + selectedRegion);
					for(RegionSelectionListener listener: mRegionSelectionListenerList){
						listener.onRegionSelected(selectedRegion);
					}
				}
			case MotionEvent.ACTION_CANCEL:
				mCamera.unregisterGLView(mSelectionRectangle);
				break;

			default:
				break;
			}
			return true;
		}

		@Override
		public Rect2D getBoundariesRectInPixel() {
			return mSurfaceBounds;
		}

		@Override
		public void onViewportChanged(Rect2D newViewportRect) {
			mSurfaceBounds.mLeftCoord = newViewportRect.mLeftCoord;			
			mSurfaceBounds.mTopCoord = newViewportRect.mTopCoord;			
			mSurfaceBounds.mWidth = newViewportRect.mWidth;			
			mSurfaceBounds.mHeight = newViewportRect.mHeight;			
		}
		
		
	}
	
	
	public static interface RegionSelectionListener{
		public void onRegionSelected(Rect2D region);
	}
	

}
