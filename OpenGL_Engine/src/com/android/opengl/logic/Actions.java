package com.android.opengl.logic;

import java.util.HashMap;

import android.util.Log;

import com.android.opengl.gameobject.base.GameObject;
import com.android.opengl.gameobject.base.Scene;
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
		private float speed = 1f;
		
		private static final float CHECK_INTERVAL = 100; // ms
		private static final long SLEEP_INTERVAL = (long) (CHECK_INTERVAL / 20); // ms
		
		
		public MovingThread(GameObject objectToMove, Point3D destination) {
			super("MovingThread");
			this.objectToMove = objectToMove;
			this.destination = destination;
			Point3D startPoint  = objectToMove.getPosition();
			speedVector = new Vector3D(startPoint, destination).normalize();
			Log.i("tag", "destination = " + destination);
			Log.i("tag", "startPoint = " + startPoint);
			Log.i("tag", "speedVector = " + speedVector);
			speedVector.setLength(speed);
			setPriority(MIN_PRIORITY);
		}

		@Override
		public void run() {
			long time;
			float newX, newY, newZ;
			while(!isInterrupted()){
				Point3D position = objectToMove.getPosition();
				
				newX = position.x + speedVector.getLength() * speedVector.getDirection().x;
				newZ = position.z + speedVector.getLength() * speedVector.getDirection().z;
				newY = objectToMove.getParentScene().getAltitude(newX, newZ);
				Log.d("tag", "y = " + newY);
				Point3D endPoint = new Point3D(newX, newY, newZ);
				speedVector = new Vector3D(position, endPoint).normalize();
				speedVector.setLength(speed);
				float curStep = 0;
				time = System.currentTimeMillis();
				while(curStep < 1){
					Log.d("tag", "speedVector = " + speedVector);
					objectToMove.setPosition(speedVector.getTargetPoint(speedVector.getLength() * curStep));
					float norma = Point3D.getmaxNorma(objectToMove.getPosition(), destination);
					if( norma < speed / 2){
						Log.i("tag", objectToMove.getClass().getSimpleName()+" destination has reached");
						return;
					}
					curStep = ((float)(System.currentTimeMillis() - time))/CHECK_INTERVAL;
					try {
						Thread.sleep(SLEEP_INTERVAL);
					} catch (InterruptedException e) {
						Log.e("tag", "moving thread: " + e.toString());
						return;
					}
				}
			}
		}
	}

}
