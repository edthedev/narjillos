package org.nusco.narjillos.shared.physics;

/**
 * A vector that has a specific origin in the plane.
 */
public class Segment {

	private final Vector startPoint;
	private final Vector vector;

	public Segment(Vector startPoint, Vector vector) {
		this.startPoint = startPoint;
		this.vector = vector;
	}

	public Vector getStartPoint() {
		return startPoint;
	}

	public Vector getVector() {
		return vector;
	}

	private double getLength() {
		return vector.minus(startPoint).getLength();
	}
	
	public double getMinimumDistanceFromPoint(Vector point) {
		double length = getLength(); // slow - only do it once
		
		if (length < 0.0001)
			return startPoint.getDistanceFrom(point);

		double lengthSquared = length * length;

		double t = ((point.x - startPoint.x) * (vector.x - startPoint.x) + (point.y - startPoint.y) * (vector.y - startPoint.y)) / lengthSquared;

		if (t < 0)
			return startPoint.getDistanceFrom(point);

		if (t > 1)
			return vector.getDistanceFrom(point);

		Vector projection = startPoint.plus(vector.minus(startPoint).by(t));
		return projection.minus(point).getLength();
	}

	public Vector getEndPoint() {
		return startPoint.plus(vector);
	}

	public Vector getMidPoint() {
		return startPoint.plus(vector.by(0.5));
	}
	
	@Override
	public String toString() {
		return "[" + startPoint + ", " + vector + "]";
	}
}
