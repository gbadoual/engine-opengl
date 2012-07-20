package com.android.opengl.gameobject.tools.moving;

import android.util.Log;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.gameobject.util.geometry.Vector3D;

public class BaseMovingThread extends Thread{
	
	private static final float SPEED_SCALE_FACTOR = 100;
	private static final float CHECK_INTERVAL = 100;
	private static final long SLEEP_INTERVAL = (long) (CHECK_INTERVAL / 20);
	private static final float EPSILON = 0.000001f;

	
	private GameObject objectToMove;
	private Point3D destination;
	private Vector3D speedVector;
	private float speed;
	
	
	
	public BaseMovingThread(GameObject objectToMove, Point3D destination) {
		super("MovingThread");
		this.objectToMove = objectToMove;
		this.destination = destination;
		Point3D startPoint  = objectToMove.getPosition();
		speedVector = new Vector3D(startPoint, destination).normalize();
		Log.i("tag", "destination = " + destination);
		Log.i("tag", "startPoint = " + startPoint);
		Log.i("tag", "speedVector = " + speedVector);
		speed = objectToMove.getMaxSpeed()/SPEED_SCALE_FACTOR;
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
//			Log.d("tag", "y = " + newY);
			Point3D endPoint = new Point3D(newX, newY, newZ);
			speedVector = new Vector3D(position, endPoint).normalize();
			speedVector.setLength(speed);
			float curStep = 0;
			time = System.currentTimeMillis();
			while(curStep < 1){
//				Log.d("tag", "speedVector = " + speedVector);
				objectToMove.setPosition(speedVector.getTargetPoint(speedVector.getLength() * curStep));
				float norma = Point3D.getmaxNorma(objectToMove.getPosition(), destination);
				if(norma < speed / 2 || Math.abs(speed) <= EPSILON){
					Log.i("tag", objectToMove.getClass().getSimpleName()+" destination has reached");
					return;
				}
				curStep = ((float)(System.currentTimeMillis() - time))/CHECK_INTERVAL;
				try {
					Thread.sleep(SLEEP_INTERVAL);
				} catch (InterruptedException e) {
					Log.w("tag", "moving thread: " + e.toString());
					interrupt();
				}
			}
			
		}
	}
}
