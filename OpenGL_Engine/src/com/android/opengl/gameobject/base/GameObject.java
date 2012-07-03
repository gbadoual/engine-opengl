package com.android.opengl.gameobject.base;

import android.opengl.Matrix;

import com.android.opengl.gameobject.util.ObjectOuterCube;
import com.android.opengl.gameobject.util.geometry.Vector3D;

abstract public class GameObject extends CommonGameObject{

	protected Scene parentScene;
	protected ObjectOuterCube outerCube;
	
	public GameObject(Scene parentScene) {
		super(parentScene.programHandle, parentScene.getResources());
		this.parentScene = parentScene;
		this.parentScene.addGameObject(this);
		outerCube = new ObjectOuterCube(this);

	}

	
	public void drawFrame() {
		if(parentScene.isRendingFinished()){
			throw new IllegalStateException("Scene shoud be rendered before rendering object of this scene");
		}
        Matrix.multiplyMM(mvpMatrix, 0, parentScene.getVpMatrix(), 0, modelMatrix, 0);
		super.drawFrame();
		
	}


	public Scene getParentScene() {
		return parentScene;
	}


	public ObjectOuterCube getOuterCube() {
		return outerCube;
	}
	
	public boolean setIsSelected(Vector3D vector) {
		return isSelected = outerCube.isIntersected(vector);
	}


}
