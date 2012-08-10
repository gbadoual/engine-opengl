package com.android.opengl.gameobject.tools.moving;

import android.util.Log;

import com.android.opengl.util.geometry.Point3D;

public class EmptyMovingTool extends MovingTool{

	@Override
	public void moveTo(Point3D destination) {
		Log.i("tag", "Can't move. No opportunity. You can set MovintTool to make it movable");
	}

	@Override
	public void stop() {
		Log.i("tag", "This object is unmovable.");
	}

	@Override
	public float getMaxSpeed() {
		return 0;
	}

}
