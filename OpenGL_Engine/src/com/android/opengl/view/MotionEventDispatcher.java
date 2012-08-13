package com.android.opengl.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

public class MotionEventDispatcher {
	
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
		boolean res = false;
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			for(TouchableWrapper wrapper: wrapperList){
				int pointerId = Math.min(event.getPointerId(event.getActionIndex()), event.getPointerCount() - 1);
				
				float x = event.getX(pointerId);
				float y = event.getY(pointerId);
				MotionEvent currentEvent = MotionEvent.obtain(event.getDownTime(), 
															  event.getEventTime(), 
															  event.getAction(), 
															  x, y, 
															  event.getMetaState());
				if(wrapper.deliverTouchEvent(currentEvent)){
					// adding the new pointer to the motionEvent
					if (wrapper.motionEvent != null){
						PointerCoords newPointerCoords = new PointerCoords();
						event.getPointerCoords(pointerId, newPointerCoords);
						addPointer(wrapper, newPointerCoords, event.getPointerId(pointerId));
					} else {
						wrapper.motionEvent = currentEvent;
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			int pointerIndex = event.getPointerId(event.getActionIndex());
			for(TouchableWrapper touchableWrapper: wrapperList){
				removePointer(touchableWrapper, pointerIndex);
			}
			break;
		default:
			//TODO sync with current coords among all the wrappers
			for(TouchableWrapper wrapper: wrapperList){
				res |= wrapper.deliverTouchEvent();
			}
			break;
		}
		return res;
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
			touchableWrapper.getPointerCoords(i, pointerCoordsArray[i]);
			pointerIdList[i] = touchableWrapper.getPointerId(i);
		}
		pointerCoordsArray[pointerCount] = pointerCoords;
		pointerIdList[pointerCount] = newPointerId;
		touchableWrapper.addBatch(pointerCoordsArray, pointerIdList);
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
					touchableWrapper.getPointerCoords(pointerId, pointerCoordsArray[pointerCoordIndex++]);
					pointerIdArray[pointerCoordIndex] = pointerId;
				}
			}
			touchableWrapper.addBatch(pointerCoordsArray, pointerIdArray);
			return true;
		}
		return false;
	}
	
	
	private static class TouchableWrapper{
		public Touchable touchable;
		private MotionEvent motionEvent;
		private int[] pointerIdMapping = new int[0];
		
		public int zOrder;
		public TouchableWrapper(Touchable touchable, MotionEvent motionEvent,
				int zOrder) {
			super();
			this.touchable = touchable;
			this.motionEvent = motionEvent;
			this.zOrder = zOrder;
		}
		
		public void disposeMotionEvent() {
			if(motionEvent != null){
				motionEvent.recycle();
				motionEvent = null;
			}
		}

		public int findPointerIndex(int pointerIndex) {
			if(motionEvent != null){
				for(int i = 0; i < pointerIdMapping.length; ++i){
					if(pointerIdMapping[i] == pointerIndex){
						return i;
					}
				}
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

		public void addBatch(PointerCoords[] pointerCoords, int[] pointerIdList){
			if(motionEvent != null){
				motionEvent.addBatch(motionEvent.getEventTime(), pointerCoords, motionEvent.getMetaState());
				pointerIdMapping = pointerIdList;
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
