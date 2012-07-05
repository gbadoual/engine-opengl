package com.android.opengl.logic;

import java.util.HashMap;

import android.util.Log;

import com.android.opengl.gameobject.base.GameObject;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.gameobject.util.geometry.Vector3D;

public class Actions {
	
	private static HashMap<GameObject, MovingThread> movingThreadMap = new HashMap<GameObject, MovingThread>();
	

	public static void moveTo(GameObject objectToMove, Point3D destination) {
		MovingThread movingTread = movingThreadMap.get(objectToMove);
		if(movingTread == null){
			movingTread = new MovingThread(objectToMove, destination);
			movingThreadMap.put(objectToMove, movingTread);
		}
		if(Thread.State.NEW != movingTread.getState()){
			movingTread.interrupt();
		}
		movingTread.start();
	}
	
	
	public static class MovingThread extends Thread{
		
		private GameObject objectToMove;
		private Point3D destination;
		private Vector3D speedVector;
		
		
		public MovingThread(GameObject objectToMove, Point3D destination) {
			this.objectToMove = objectToMove;
			this.destination = destination;
			float[] position = objectToMove.getCenterXYZ();
			speedVector = new Vector3D(destination.x - position[0], 
									destination.y - position[1],
									destination.z - position[2]).normalize();
			speedVector.setLength(10);
		}

		@Override
		public void run() {
			long time = System.currentTimeMillis();
			while(true){
				Point3D targetPoint = speedVector.getDirection();//getTargetPoint(0.0000000001f);
				Log.i("tag", "targetPoint = " + targetPoint);
				objectToMove.translateIncrement(targetPoint.asFloatArray());
				if(Point3D.getmaxNorma(targetPoint, destination) < 1f){
					// destination has reached
					break;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Log.e("tag", "moving thread: " + e.toString());
				}
			}
			
		}
	}

}
