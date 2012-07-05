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

		if(movingTread != null){
			movingTread.interrupt();
		}
		movingTread = new MovingThread(objectToMove, destination);
		movingThreadMap.put(objectToMove, movingTread);

		movingTread.start();
	}
	
	
	public static class MovingThread extends Thread{
		
		private GameObject objectToMove;
		private Point3D destination;
		private Vector3D speedVector;
		
		
		public MovingThread(GameObject objectToMove, Point3D destination) {
			super("MovingThread");
			this.objectToMove = objectToMove;
			this.destination = destination;
			Point3D startPoint  = objectToMove.getPosition();
			speedVector = new Vector3D(startPoint, destination).normalize();
			Log.i("tag", "destination = " + destination);
			Log.i("tag", "startPoint = " + startPoint);
			Log.i("tag", "speedVector = " + speedVector);
			speedVector.setLength(10);
			setPriority(MIN_PRIORITY);
		}

		@Override
		public void run() {
			long time = System.currentTimeMillis();
			float scale = 0.1f;
			while(!isInterrupted()){
				Point3D incPoint = speedVector.getDirection().clone();//getTargetPoint(0.0000000001f);
				incPoint.x = incPoint.x * scale;
				incPoint.y = incPoint.y * scale;
				incPoint.z = incPoint.z * scale;
				objectToMove.incPosition(incPoint);
				
				float norma = Point3D.getmaxNorma(objectToMove.getPosition(), destination);
//				Log.i("tag", "curPosition" + objectToMove.getPosition());
//				Log.i("tag", "maxNorma = " + norma);
				if( norma < 1f){
					Log.i("tag", objectToMove.getClass().getSimpleName()+" destination has reached");
					// destination has reached
					break;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Log.e("tag", "moving thread: " + e.toString());
					break;
				}
			}
			
		}
	}

}
