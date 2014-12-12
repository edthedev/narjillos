package org.nusco.narjillos.creature.body;

import org.nusco.narjillos.creature.body.pns.Nerve;
import org.nusco.narjillos.shared.physics.Angle;
import org.nusco.narjillos.shared.physics.Vector;
import org.nusco.narjillos.shared.utilities.ColorByte;

/**
 * Adds the notion of the passing of time to the BodyPart hierarchy, embodied by
 * the tick() method. This means that it moves (see calculateAngleToParent()).
 */
public abstract class MovingOrgan extends ConnectedOrgan {

	private double angleToParent = 0;

	protected MovingOrgan(int adultLength, int adultThickness, ColorByte color, ConnectedOrgan parent, Nerve nerve, int angleToParentAtRest) {
		super(adultLength, adultThickness, color, parent, nerve);
		setAngleToParent(angleToParentAtRest);
	}

	public void tick(double percentOfAmplitude, double angleToTarget) {
		recursivelyGrow();

		double processedPercentOfAmplitude = getNerve().tick(percentOfAmplitude);

		setAngleToParent(calculateNewAngleToParent(processedPercentOfAmplitude, angleToTarget));

		updateGeometry();

		for (ConnectedOrgan child : getChildren())
			((MovingOrgan) child).tick(processedPercentOfAmplitude, angleToTarget);
	}

	protected final double getAngleToParent() {
		return angleToParent;
	}

	protected final void setAngleToParent(double newAngleToParent) {
		angleToParent = newAngleToParent;
	}

	protected abstract double calculateNewAngleToParent(double targetAngle, double angleToTarget);

	private void recursivelyGrow() {
		// FIXME: this needs to be rethought. In particular, children's grow()
		// gets called many times (look at the tick() recursion)
		grow();

		for (ConnectedOrgan child : getChildren())
			((MovingOrgan) child).recursivelyGrow();
	}

	protected void updateTree() {
		updateGeometry();

		for (ConnectedOrgan child : getChildren())
			((MovingOrgan) child).updateTree();
	}

	public void rotateBy(double rotation) {
		setAngleToParent(Angle.normalize(getAngleToParent() + rotation));
		updateTree();
	}

	protected void translateBy(Vector translation) {
		updatePosition();

		for (ConnectedOrgan child : getChildren())
			((MovingOrgan) child).translateBy(translation);
	}
}