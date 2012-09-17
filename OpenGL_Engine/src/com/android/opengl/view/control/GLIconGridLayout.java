package com.android.opengl.view.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.GameObject;

public class GLIconGridLayout extends GLGridLayout{
	
	private HashMap<String, List<GameObject>> hashMap = new HashMap<String, List<GameObject>>();
	private boolean isExpanded;
	private int maxIconCount;

	public GLIconGridLayout(Camera camera) {
		super(camera);
		init();
	}
	

	public GLIconGridLayout(Camera camera, int left, int top, int width, int height) {
		super(camera, left, top, width, height);
		init();
	}
	
	private void init(){
		setMaxIconCount(8);
		isExpanded = true;
	}


	public void addUnitIconList(List<GameObject> gameObjectList) {
		if(gameObjectList != null){
			for(GameObject gameObject: gameObjectList){
				addUnitIcon(gameObject);
			}
		}
	}
	
	public void addUnitIcon(GameObject gameObject) {
		String key = gameObject.getClass().getName();
		List<GameObject> gameObjectList = hashMap.get(key);
		if(gameObjectList == null){
			gameObjectList = new ArrayList<GameObject>();
			hashMap.put(key, gameObjectList);
			addGLIconAsChild(gameObject);
		} else{
			if(isExpanded){
				addGLIconAsChild(gameObject);
			}			
		}
		gameObjectList.add(gameObject);
	}
	
	private void addGLIconAsChild(GameObject gameObject) {
		if(mChildren.size() < maxIconCount){
			super.addChild(gameObject.getUnitIconView());
		}
	}


	@Override
	public void removeChildren() {
		hashMap.clear();
		super.removeChildren();
	}


	public boolean isExpanded() {
		return isExpanded;
	}


	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}


	public int getMaxIconCount() {
		return maxIconCount;
	}


	public void setMaxIconCount(int maxIconCount) {
		this.maxIconCount = maxIconCount;
	}
	
	private OnTapListener onIconTapListener = new OnTapListener() {
		
		@Override
		public void onTap(GLView glView) {
									
		}
	};

}
