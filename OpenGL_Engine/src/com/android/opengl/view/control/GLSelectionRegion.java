package com.android.opengl.view.control;

import com.android.opengl.Camera;
import com.android.opengl.util.geometry.Point2D;

public class GLSelectionRegion extends GLView{

	public GLSelectionRegion(Camera camera) {
		super(camera);
//		mGLViewShader
		
	}
	
	
	public void setPosition(Point2D leftTopPoint){
		setPositionOffsetFromScreenCoords(leftTopPoint.x, leftTopPoint.y);
	}
	
	public void setDimensions(float width, float height) {
		onMeasure(width, height);
	}
	
	
	
	

}
