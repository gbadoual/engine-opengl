package com.android.opengl.view;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

public class GestureDetector extends android.view.GestureDetector{

	private ScaleGestureDetector scaleGestureDetector;
	private OnGestureListener gestureListener;

	
	public GestureDetector(Context context, OnGestureListener listener) {
		super(context, listener);
		gestureListener = listener;
		scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(super.onTouchEvent(ev)){
			return true;
		}
		return scaleGestureDetector.onTouchEvent(ev);
	}
	

	private OnScaleGestureListener scaleGestureListener = new OnScaleGestureListener() {
		
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			return gestureListener.onPinch(detector);
		}
	
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}
	
		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			
		}
	};

	public static interface OnGestureListener extends android.view.GestureDetector.OnGestureListener{
		
		public boolean onPinch(ScaleGestureDetector detector);
	}
	
}
