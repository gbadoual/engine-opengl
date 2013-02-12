package com.android.opengl.view.control;

import java.util.ArrayList;
import java.util.List;

import com.android.opengl.Camera;
import com.android.opengl.gameobject.GLScene;

public class GLLinearLayout extends GLLayout{

	
	private Orientation mOrientation = Orientation.HORIZONTAL;
	private float mSpacing = 1;

	public GLLinearLayout(Camera camera) {
		super(camera);
	}

	public GLLinearLayout(Camera camera, float left, float top, float width,
			float height) {
		super(camera, left, top, width, height);
	}

	public GLLinearLayout(GLScene scene) {
		super(scene);
	}

	public GLLinearLayout(GLScene scene, float left, float top, float width,
			float height) {
		super(scene, left, top, width, height);
	}
	
	@Override
	public void addChild(GLView child) {
		super.addChild(child);
		layoutChild(child);
	}
	
	private void layoutChild(GLView child) {
		switch (mOrientation) {
		case HORIZONTAL:
			layoutHorizontally(child);
			break;
		case VERTICAL:
			layoutVertically(child);
			break;
		}
	}

	private void layoutVertically(GLView child) {
		float lastX = mSpacing;
		float lastY = mSpacing;
		if(mChildren.size() > 1){
			GLView lastChild = mChildren.get(mChildren.size() - 2);
			lastY += lastChild.mTopCoord + lastChild.mHeight;
		}
		child.onLayout(lastX, lastY);
	}

	private void layoutHorizontally(GLView child){
		float lastX = mSpacing;
		float lastY = mSpacing;
		if(mChildren.size() > 1){
			GLView lastChild = mChildren.get(mChildren.size() - 2);
			lastX += lastChild.mLeftCoord + lastChild.mWidth;
		}
		child.onLayout(lastX, lastY);
	}
	
	private void relayoutChildren(){
		List<GLView> childCopy = new ArrayList<GLView>(mChildren);
		mChildren.clear();
		for(GLView child: childCopy){
			addChild(child);
		}
	}
	
	public void setOrientation(Orientation orientation){
		mOrientation = orientation;
		relayoutChildren();
	}


}
