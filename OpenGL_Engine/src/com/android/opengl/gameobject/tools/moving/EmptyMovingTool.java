package com.android.opengl.gameobject.tools.moving;

import android.util.Log;

import com.android.opengl.gameobject.util.geometry.Point3D;

public class EmptyMovingTool implements MovingTool{

	@Override
	public void moveTo(Point3D destination) {
		Log.i("tag", "Can't move. No opportunity. You can set MovintTool to make it movable");
	}

	@Override
	public void stop() {
		Log.i("tag", "This onject is unmovable.");
	}

}
