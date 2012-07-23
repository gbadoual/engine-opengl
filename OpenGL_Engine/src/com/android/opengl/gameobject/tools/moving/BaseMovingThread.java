package com.android.opengl.gameobject.tools.moving;

import android.util.Log;

import com.android.opengl.gameobject.GameObject;
import com.android.opengl.gameobject.util.geometry.Plane;
import com.android.opengl.gameobject.util.geometry.Point3D;
import com.android.opengl.gameobject.util.geometry.Vector3D;

public class BaseMovingThread extends Thread{
	
	private static final int MAX_ANGLE_DEVIATION = 20;
	private static final float SPEED_SCALE_FACTOR = 100;
	private static final float MOVING_TIME_INTERVAL = 100;
	private static final long THREAD_SLEEP_INTERVAL = 10;
	private static final float EPSILON = 0.000001f;
	private static final float TURNING_360_TIME_INTERVAL = 1100 / 360;

	
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
			try {
				Point3D position = objectToMove.getPosition();
				turnObject();
				
				newX = position.x + speedVector.getLength() * objectToMove.getDirection().x;
				newZ = position.z + speedVector.getLength() * objectToMove.getDirection().z;
//				newX = position.x + speedVector.getLength() * speedVector.getDirection().x;
//				newZ = position.z + speedVector.getLength() * speedVector.getDirection().z;
				newY = objectToMove.getParentScene().getAltitude(newX, newZ);
	//			Log.d("tag", "y = " + newY);
				Point3D endPoint = new Point3D(newX, newY, newZ);
				speedVector = new Vector3D(position, endPoint).normalize();
				speedVector.setLength(speed);
				float curStep = 0;
				time = System.currentTimeMillis();
				while(curStep < 1){
					objectToMove.setPosition(speedVector.getTargetPoint(speedVector.getLength() * curStep));
					float norma = Point3D.getmaxNorma(objectToMove.getPosition(), destination);
					if(norma < speed / 2 || Math.abs(speed) <= EPSILON){
						Log.i("tag", objectToMove.getClass().getSimpleName()+" destination has reached");
						interrupt();
					}
					curStep = ((float)(System.currentTimeMillis() - time))/MOVING_TIME_INTERVAL;
					sleep(THREAD_SLEEP_INTERVAL);
				}
			} catch (InterruptedException e) {
				Log.w("tag", "moving thread was interrupted: " + e);
				interrupt();
			}
			
		}
	}

	private void turnObject() throws InterruptedException{
		float[] objDirection = objectToMove.getDirection().asFloatArray();
		float[] pointOnPlane = new float[]{0, 0, 0};
		float[] normal = objectToMove.getUpVector().asFloatArray();
		Vector3D vectorToIntersect = new Vector3D(objectToMove.getParentScene().getUpVector());
		Point3D dest = new Point3D(destination.x - objectToMove.getPosition().x, destination.y - objectToMove.getPosition().y, destination.z - objectToMove.getPosition().z);
		vectorToIntersect.setPosition(dest);
		
		Point3D intersectedPoint = Plane.getIntersectionPoint(pointOnPlane, normal, vectorToIntersect);
		if(intersectedPoint != null){
			float[] projDirection = intersectedPoint.asFloatArray();
			Vector3D.normalize(projDirection);
			float cosA = Vector3D.dotProduct(projDirection, objDirection);
			float[] infoVector = Vector3D.vectorProduct(projDirection, normal);
			float sinA = Vector3D.dotProduct(infoVector, objDirection);
			cosA = Math.max(-1, Math.min(1, cosA));
			sinA = Math.max(-1, Math.min(1, sinA));
			float turnAngle = (float) Math.toDegrees(Math.acos(cosA));
			float turningTimeInterval = TURNING_360_TIME_INTERVAL * turnAngle;					
			if(sinA < 0 ){
				turnAngle = -turnAngle;
			}
			Log.d("tag", "turnAngle = " + turnAngle);
			Log.d("tag", "cosA = " + cosA);
			Log.d("tag", "sinA = " + sinA);
			if(Math.abs(turnAngle) < MAX_ANGLE_DEVIATION){
				return;
			}
			long time = System.currentTimeMillis();
			float curStep = 0;
			float[] savedModelMatrix = objectToMove.getModelMatrix().clone();
			while(curStep < 1){
				objectToMove.setModelMatrix(savedModelMatrix.clone());
				if(isInterrupted()){
					throw new InterruptedException("the thread was interrupted while rotating an object");
				}
				objectToMove.rotate(0, -turnAngle * curStep, 0);				
				
				curStep = ((float)(System.currentTimeMillis() - time)) / turningTimeInterval;
				sleep(THREAD_SLEEP_INTERVAL);
			}
			
		}
		
		
	}
}
