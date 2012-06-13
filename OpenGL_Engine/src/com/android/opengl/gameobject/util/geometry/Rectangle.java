package com.android.opengl.gameobject.util.geometry;

public class Rectangle {
	
	private Point2D leftBottom;
	private Point2D rightTop;
	
	public Rectangle() {
		leftBottom = new Point2D();
		rightTop = new Point2D();
	}
	
	public Rectangle(float x1, float y1, float x2, float y2){
		leftBottom = new Point2D(x1, y1);
		rightTop = new Point2D(x2, y2);
	}
	
	public Rectangle(Point2D leftBottom, Point2D rightTop){
		this.leftBottom = leftBottom;
		this.rightTop = rightTop;
	}

	
	
	public Point2D getLeftBottom() {
		return leftBottom;
	}

	public void setLeftBottom(Point2D leftBottom) {
		this.leftBottom = leftBottom;
	}

	public Point2D getRightTop() {
		return rightTop;
	}

	public void setRightTop(Point2D rightTop) {
		this.rightTop = rightTop;
	}
	
	
}
