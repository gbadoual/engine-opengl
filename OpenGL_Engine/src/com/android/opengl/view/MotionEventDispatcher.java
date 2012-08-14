package com.android.opengl.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.util.Log;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

public class MotionEventDispatcher {
	
	private static final String TAG = MotionEventDispatcher.class.getSimpleName();

	private List<TouchableWrapper> wrapperList = new ArrayList<TouchableWrapper>();
	
	public Comparator<TouchableWrapper> touchableWrapperComparator = new Comparator<MotionEventDispatcher.TouchableWrapper>() {
		
		@Override
		public int compare(TouchableWrapper lhs, TouchableWrapper rhs) {
			if(lhs != null && rhs != null){
				return lhs.zOrder - rhs.zOrder; 
			}
			return 0;
		}
	};
	
	public boolean dispatchTouchEvent(MotionEvent event){
		if(wrapperList.isEmpty()){
			Log.i(TAG, "no any Touchable registered. Use registerToucheble(Touchable) to deliever MotionEvent");
			return false;
		}
		boolean res = false;
		int pointerIndex = event.getActionIndex();
		int pointerId = event.getPointerId(pointerIndex);
		for(TouchableWrapper touchableWrapper: wrapperList){
			touchableWrapper.syncState(event);
		}
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			int[] pointerIds = new int[]{pointerId};
			PointerCoords newPointerCoords = new PointerCoords();
			event.getPointerCoords(pointerIndex, newPointerCoords);
			PointerCoords[] pointerCoords = new PointerCoords[]{newPointerCoords};
			MotionEvent currentEvent = obrainMotionEvent(event, pointerIds, pointerCoords);

			for(TouchableWrapper wrapper: wrapperList){
				if(wrapper.deliverTouchEvent(currentEvent)){
					res = true;
					// adding the new pointer to the motionEvent
					if (wrapper.motionEvent != null){
						addPointer(wrapper, newPointerCoords, pointerId);
					} else {
						wrapper.motionEvent = currentEvent;
					}
					break;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			for(TouchableWrapper touchableWrapper: wrapperList){
				removePointer(touchableWrapper, pointerId);
			}
			break;
		default:
			for(TouchableWrapper wrapper: wrapperList){
				res |= wrapper.deliverTouchEvent();
			}
			break;
		}
		return res;
	}
	
	public static MotionEvent obrainMotionEvent(MotionEvent event, int[] pointerIds, PointerCoords[] pointerCoords) {
		return 	MotionEvent.obtain(event.getDownTime(), 
				event.getEventTime(), 
				event.getAction(), 
				event.getPointerCount(), 
				pointerIds, 
				pointerCoords, 
				event.getMetaState(), 
				event.getXPrecision(), event.getYPrecision(), 
				event.getDeviceId(), 	
				event.getEdgeFlags(), 
				event.getSource(), 
				event.getFlags());
	}

	public boolean registerToucheble(Touchable touchable, int zOrder){
		boolean res = false;
		TouchableWrapper wrapper = new TouchableWrapper(touchable, null, zOrder);
		if(!wrapperList.contains(wrapper)){
			res = wrapperList.add(wrapper);
			if(res){
				Collections.sort(wrapperList, touchableWrapperComparator);
			}
		}
		return res;
	}

	public boolean unregisterToucheble(Touchable touchable){
		return wrapperList.remove(new TouchableWrapper(touchable, null, 0));
	}
	
	public void addPointer(TouchableWrapper touchableWrapper, PointerCoords pointerCoords, int newPointerId){
		int pointerCount = touchableWrapper.getPointerCount();
		PointerCoords[] pointerCoordsArray = new PointerCoords[pointerCount + 1];
		int[] pointerIdList = new int[pointerCount + 1];
		for(int i = 0 ; i < pointerCoordsArray.length - 1; ++i){
			pointerCoordsArray[i] = new PointerCoords();
			touchableWrapper.getPointerCoords(i, pointerCoordsArray[i]);
			pointerIdList[i] = touchableWrapper.getPointerId(i);
		}
		pointerCoordsArray[pointerCount] = pointerCoords;
		pointerIdList[pointerCount] = newPointerId;
		touchableWrapper.updatePointerCoordinates(pointerCoordsArray, pointerIdList);
	}

	public boolean removePointer(TouchableWrapper touchableWrapper, int pointerIndex){
		int pointerIdToDelete = touchableWrapper.findPointerIndex(pointerIndex);
		if(pointerIdToDelete >= 0){
			int size = touchableWrapper.getPointerCount();
			if(size == 1){
				touchableWrapper.disposeMotionEvent();
				return true;
			}
			PointerCoords[] pointerCoordsArray = new PointerCoords[size - 1];
			int[] pointerIdArray = new int[size - 1];
			int pointerCoordIndex = 0;
			for(int i = 0 ; i < size; ++i){
				int pointerId = touchableWrapper.getPointerId(i);
				if(pointerId != pointerIdToDelete){
					pointerCoordsArray[pointerCoordIndex] = new PointerCoords();
					touchableWrapper.getPointerCoords(pointerId, pointerCoordsArray[pointerCoordIndex++]);
					pointerIdArray[pointerCoordIndex] = pointerId;
				}
			}
			touchableWrapper.updatePointerCoordinates(pointerCoordsArray, pointerIdArray);
			return true;
		}
		return false;
	}
	
	
	private static class TouchableWrapper{
		public Touchable touchable;
		private MotionEvent motionEvent;
		public int zOrder;
		
		
		public TouchableWrapper(Touchable touchable, MotionEvent motionEvent,
				int zOrder) {
			super();
			this.touchable = touchable;
			this.motionEvent = motionEvent;
			this.zOrder = zOrder;
		}
		
		public void syncState(MotionEvent event) {
			int pointerIndex = findPointerIndex(event.getActionIndex());
			if( pointerIndex < 0){
				return;
			}
			motionEvent.setAction(event.getAction());
			int pointerCount = motionEvent.getPointerCount();
			PointerCoords[] pointerCoordsArray = new PointerCoords[pointerCount];
			for(int i = 0; i < pointerCount; ++i){
				pointerCoordsArray[i] = new PointerCoords();
				if(i == pointerIndex){
					event.getPointerCoords(i, pointerCoordsArray[i]);
				} else{
					motionEvent.getPointerCoords(i, pointerCoordsArray[i]);
				}
			}
			motionEvent.addBatch(event.getEventTime(), pointerCoordsArray, event.getMetaState());
			
			
		}

		public void disposeMotionEvent() {
			if(motionEvent != null){
				motionEvent.recycle();
				motionEvent = null;
			}
		}

		public int findPointerIndex(int pointerId) {
			if(motionEvent != null){
				return motionEvent.findPointerIndex(pointerId);
			}
			return -1;
		}

		public int getPointerId(int i) {
			if(motionEvent != null){
				return motionEvent.getPointerId(i);
			}
			return -1;
		}

		public void getPointerCoords(int i, PointerCoords pointerCoords) {
			if(motionEvent != null){
				motionEvent.getPointerCoords(i, pointerCoords);
			}
			
		}

		public int getPointerCount() {
			if(motionEvent != null){
				return motionEvent.getPointerCount();
			}
			return 0;
		}

		public void updatePointerCoordinates(PointerCoords[] pointerCoords, int[] pointerIdList){
			if(motionEvent != null){
				motionEvent = obrainMotionEvent(motionEvent, pointerIdList, pointerCoords);
			}
		}
		
		public boolean deliverTouchEvent(MotionEvent event) {
			if(touchable != null ){
				return touchable.onTouchEvent(event);
			}
			return false;
		}
		

		public boolean deliverTouchEvent(){
			if(touchable != null && motionEvent != null){
				return touchable.onTouchEvent(motionEvent);
			}
			return false;
		}
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((motionEvent == null) ? 0 : motionEvent.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TouchableWrapper other = (TouchableWrapper) obj;
			if (motionEvent == null) {
				if (other.motionEvent != null)
					return false;
			} else if (!motionEvent.equals(other.motionEvent))
				return false;
			return true;
		}
	}

}
