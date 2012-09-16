package com.android.opengl.view.control;

import java.util.ArrayList;
import java.util.List;

import android.view.MotionEvent;

import com.android.opengl.Camera;
import com.android.opengl.util.Log;
import com.android.opengl.util.geometry.Rect2D;
import com.android.opengl.view.MotionEventDispatcher;

public class GLSelectionRegion extends GLView{
	
	private GLSelectionBorder mGLSelectionSurface;

	public GLSelectionRegion(Camera camera) {
		super(camera);
		onMeasure(10, 10);
		setzOrder(40);
		mGLSelectionSurface = new GLSelectionBorder(camera, 0, 0, 100, 100);
		camera.unregisterGLView(mGLSelectionSurface);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			mCamera.registerGLView(mGLSelectionSurface, getzOrder());
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mCamera.unregisterGLView(mGLSelectionSurface);
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

	
	public static class GLSelectionBorder extends GLView{
		private List<RegionSelectionListener> mRegionSelectionListenerList = new ArrayList<GLSelectionRegion.RegionSelectionListener>();
		private GLView mSelectionRectangle;

		public GLSelectionBorder(Camera camera) {
			super(camera);
			initLocal();
		}

		public GLSelectionBorder(Camera camera, float left, float top, float width, float height) {
			super(camera, left, top, width, height);
			initLocal();
		}
		@Override
		public void onDrawFrame() {
			//This view is transparent and needs only to capture the whole screen
		}
		
		@Override
		public void release() {
			mRegionSelectionListenerList.clear();
			mSelectionRectangle.release();
			super.release();
		}
		
		public boolean registerSelectionListener(RegionSelectionListener listener){
			return mRegionSelectionListenerList.add(listener);
		}
		public boolean unregisterSelectionListener(RegionSelectionListener listener){
			return mRegionSelectionListenerList.remove(listener);
		}

		public Rect2D getBorderBounds(){
			return mSelectionRectangle.getBoundariesRectInPixel();
		}

		private void initLocal() {
			mSelectionRectangle = new GLView(mCamera);
			mCamera.unregisterGLView(mSelectionRectangle);
			mSelectionRectangle.setColor(0, 40, 50, 0);
			mSelectionRectangle.setBorderWidth(0.3f);
		}
		
		public void setBorderPosition(float screenX, float screenY){
//			Log.d("tag", "setPos x/y: " + screenX + "/" + screenY);
			mSelectionRectangle.onLayout(screenX * screenToPercentRatio, screenY * screenToPercentRatio);
		}
		
		public void setBorderDimensions(float screenX, float screenY) {
			float width = screenX * screenToPercentRatio - mSelectionRectangle.mLeftCoord;
			float height = screenY * screenToPercentRatio - mSelectionRectangle.mTopCoord;
//			Log.d("tag", "setDim w/h: " + width + "/" + height);
			mSelectionRectangle.onMeasure(width, height);
		}
		public void initBorder(){
			mCamera.unregisterGLView(mSelectionRectangle);
			mCamera.registerGLView(mSelectionRectangle, 39);
			mCamera.unregisterTouchable(mSelectionRectangle);
			mSelectionRectangle.onMeasure(0, 0);
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
//			Log.v("tag", "pointerIndex = " + event.getActionIndex());
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				setBorderPosition(event.getX(), event.getY());
				initBorder();
				break;
			case MotionEvent.ACTION_MOVE:
//				Log.d("tag", "move x/y: " + border.mLeftCoord + "/" + border.mTopCoord);
				setBorderDimensions(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_UP:
				Rect2D selectedRegion = getBorderBounds();
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
		
		
	}
	
	
	public static interface RegionSelectionListener{
		public void onRegionSelected(Rect2D region);
	}
	

}
