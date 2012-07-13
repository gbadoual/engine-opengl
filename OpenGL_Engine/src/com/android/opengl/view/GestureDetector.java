package com.android.opengl.view;

import android.content.Context;
import android.view.MotionEvent;

public class GestureDetector extends android.view.GestureDetector{

	private enum GESTURE_STATE{
		ROTATE,
		SCALE,
		DOUBLE_SLIDE,
		NONE
	}

	private static final int INVALID_ID = -1;

	private static final float MIN_ANGLE_TO_BEGIN_ROTATION = 1.2f;
	private static final float MIN_DISTANCE_TO_BEGIN_PINCH = 1.01f;
	private static final float MIN_DISTANCE_TO_BEGIN_DOUBLE_SLIDE = 1f;

	private static final double MAX_DOUBLE_SLIDE_DISTANCE_DIFFERENCE = 20;
	private static final double MAX_DOUBLE_SLIDE_ANGLE_DIFFERENCE = 1;
	
	private static final double EPSILON = 0.000001f;

	private OnGestureListener gestureListener;
	
	
	private GESTURE_STATE currentState;

	private float prevX1, prevY1, prevX2, prevY2;
	private int pointerId1 = INVALID_ID; 
	private int pointerId2 = INVALID_ID;
	private float centerX, centerY;

	
	public GestureDetector(Context context, OnGestureListener listener) {
		super(context, listener);
		gestureListener = listener;
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		
		
		switch (ev.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			currentState = GESTURE_STATE.NONE;
			prevX1 = ev.getX();
			prevY1 = ev.getY();
			pointerId1 = ev.getPointerId(0);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			pointerId2 = ev.getPointerId(ev.getActionIndex());
			prevX2 = ev.getX(pointerId2);
			prevY2 = ev.getY(pointerId2);
			centerX = (prevX1 + prevX2) / 2;
			centerY = (prevY1 + prevY2) / 2;
			break;
		case MotionEvent.ACTION_MOVE:
			if(pointerId1 != INVALID_ID && pointerId2 != INVALID_ID && pointerId1 != pointerId2){
				float curX1 = ev.getX(pointerId1);
				float curY1 = ev.getY(pointerId1);
				float curX2 = ev.getX(pointerId2);
				float curY2 = ev.getY(pointerId2);
				float dAngle = angleBtwLines(prevX1, prevY1, prevX2, prevY2, curX1, curY1, curX2, curY2);
				float dScaleFactor = scaleFactor(prevX1, prevY1, prevX2, prevY2, curX1, curY1, curX2, curY2);
				float dDoubleSlideDistance = doubleSlideDistance(prevX1, prevY1, prevX2, prevY2, curX1, curY1, curX2, curY2);

//				if(currentState == GESTURE_STATE.NONE && 
//						Math.abs(dDoubleSlideDistance) > MIN_DISTANCE_TO_BEGIN_DOUBLE_SLIDE){
//					currentState = GESTURE_STATE.DOUBLE_SLIDE;
//				}
				if(currentState == GESTURE_STATE.NONE && 
						Math.abs(dAngle) > MIN_ANGLE_TO_BEGIN_ROTATION){
					currentState = GESTURE_STATE.ROTATE;
				}
				if(currentState == GESTURE_STATE.NONE && 
						(dScaleFactor > MIN_DISTANCE_TO_BEGIN_PINCH ||
						1/dScaleFactor > MIN_DISTANCE_TO_BEGIN_PINCH)){
					currentState = GESTURE_STATE.SCALE;
				}
				
				prevX1 = curX1;
				prevY1 = curY1;
				prevX2 = curX2;
				prevY2 = curY2;
				switch (currentState) {
				case ROTATE:
					return gestureListener.onRotate(centerX, centerY, dAngle);
				case SCALE:
					return gestureListener.onPinch(centerX, centerY, dScaleFactor);
				case DOUBLE_SLIDE:
					return gestureListener.onDoubleSlide(dDoubleSlideDistance, dDoubleSlideDistance);						
				default:
					break;
				}
			}else{
				prevX1 = ev.getX();
				prevY1 = ev.getY();				
			}
			
			break;
		case MotionEvent.ACTION_UP:
			pointerId1 = INVALID_ID;
			currentState = GESTURE_STATE.NONE;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			pointerId2 = INVALID_ID;
			break;
		case MotionEvent.ACTION_CANCEL:
			pointerId1 = INVALID_ID;
			pointerId2 = INVALID_ID;
			currentState = GESTURE_STATE.NONE;
			
			break;				

		default:
			break;
		}

		
		return super.onTouchEvent(ev);
	}
	

	private float doubleSlideDistance(float fx1, float fy1, float fx2, float fy2, float sx1, float sy1, float sx2, float sy2) {
		float res = 0;
		double translateDistance1 = Math.sqrt((fx1 - sx1) * (fx1 - sx1) + (fy1 - sy1) * (fy1 - sy1));
		double translateDistance2 = Math.sqrt((fx2 - sx2) * (fx2 - sx2) + (fy2 - sy2) * (fy2 - sy2));
		if(Math.abs(translateDistance1 - translateDistance2) < MAX_DOUBLE_SLIDE_DISTANCE_DIFFERENCE){
		    double angle1 = Math.toDegrees(Math.atan2(fy1 - fy2, fx1 - fx2)) % 360;
		    double angle2 = Math.toDegrees(Math.atan2(sy1 - sy2, sx1 - sx2)) % 360;
		    if (Math.abs(angle1 - angle2) < MAX_DOUBLE_SLIDE_ANGLE_DIFFERENCE){
		    	res = (float)(sx1 - fx1);
		    }
		}
		return res;
	}


	private float scaleFactor(float prevX1, float prevY1,
			float prevX2, float prevY2, float curX1, float curY1,
			float curX2, float curY2) {
		double oldDistance = Math.sqrt((prevX1 - prevX2) * (prevX1 - prevX2) + (prevY1 - prevY2) * (prevY1 - prevY2));
		double newDistance = Math.sqrt((curX1 - curX2) * (curX1 - curX2) + (curY1 - curY2) * (curY1 - curY2));
		if(Math.abs(oldDistance) < EPSILON){
			return 1;
		}
		return (float)(newDistance/oldDistance);
	}


	private float angleBtwLines (float fx1, float fy1, float fx2, float fy2, float sx1, float sy1, float sx2, float sy2){
	    double angle1 = Math.toDegrees(Math.atan2(fy1 - fy2, fx1 - fx2)) % 360;
	    double angle2 = Math.toDegrees(Math.atan2(sy1 - sy2, sx1 - sx2)) % 360;
	    double res = angle1 - angle2;
	    if(res < -180) {res += 360;}
	    if(res >  180) {res -= 360;}
	    
	    
	    return (float) res;
	}


	
	
	public static interface OnGestureListener extends android.view.GestureDetector.OnGestureListener{
		
		public boolean onPinch(float centerX, float centerY, float angle);
		public boolean onDoubleSlide(float distanceX,	float distanceY);
		public boolean onRotate(float centerX, float centerY, float angle);
	}
	
}
