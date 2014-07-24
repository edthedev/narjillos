package org.nusco.narjillos.creature.body;

import java.util.LinkedList;
import java.util.List;

import org.nusco.narjillos.creature.body.pns.Nerve;
import org.nusco.narjillos.shared.physics.Segment;
import org.nusco.narjillos.shared.physics.Vector;
import org.nusco.narjillos.shared.utilities.ColorByte;

public abstract class BodyPart extends Organ {

	private final Nerve nerve;
	private final BodyPart parent;
	private final List<BodyPart> children = new LinkedList<>();

	private double angleToParent = 0;

	private MovementListener movementListener = MovementListener.NULL;

	protected BodyPart(int length, int thickness, ColorByte color, BodyPart parent, Nerve nerve) {
		super(length, thickness, color);
		this.nerve = nerve;
		this.parent = parent;
	}

	protected final double getAngleToParent() {
		return angleToParent;
	}

	protected final void setAngleToParent(double angleToParent) {
		this.angleToParent = angleToParent;
		resetAllCaches();
	}

	protected Vector calculateStartPoint() {
		return getParent().getEndPoint();
	}

	protected abstract double calculateAbsoluteAngle();

	protected Vector calculateMainAxis() {
		return getParent().calculateMainAxis();
	}

	protected final BodyPart getParent() {
		return parent;
	}

	public List<BodyPart> getChildren() {
		return children;
	}

	public Vector tick(Vector inputSignal) {
		Segment beforeMovement = getSegment();

		Vector outputSignal = getNerve().tick(inputSignal);

		move(outputSignal);
		resetAllCaches();

		notifyMovementListener(beforeMovement, this);

		tickChildren(outputSignal);

		return outputSignal;
	}

	protected abstract void move(Vector signal);

	private void notifyMovementListener(Segment beforeMovement, Organ organ) {
		movementListener.moveEvent(beforeMovement, organ);
	}

	protected void tickChildren(Vector signal) {
		for (BodyPart child : getChildren())
			child.tick(signal);
	}

	public Nerve getNerve() {
		return nerve;
	}

	protected void setMovementListener(MovementListener listener) {
		movementListener = listener;
		for (BodyPart child : getChildren())
			child.setMovementListener(listener);
	}

	public BodyPart sproutOrgan(int length, int thickness, int angleToParentAtRest, ColorByte hue, int delay) {
		return addChild(new BodySegment(length, thickness, angleToParentAtRest, hue, this, delay));
	}

	BodyPart sproutOrgan(Nerve nerve) {
		return addChild(new BodySegment(nerve));
	}

	public BodyPart sproutAtrophicOrgan() {
		return addChild(new AtrophicOrgan(this));
	}

	protected BodyPart addChild(BodyPart child) {
		children.add(child);
		return child;
	}
}