package com.android.opengl.gameobject.tools.attacking;

import android.util.Log;

import com.android.opengl.gameobject.GameObject;

public class EmptyAttackingTool implements AttackingTool{

	@Override
	public void attack(GameObject gameObjectToAttack) {
		Log.i("tag", "This object can't attack anybody. It is friendly. You can set AttackingTool to make it more powerful");
	}

}
