package org.nusco.narjillos.creature.body;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nusco.narjillos.creature.body.physics.RotationsPhysicsEngine;
import org.nusco.narjillos.creature.body.physics.TranslationsPhysicsEngine;
import org.nusco.narjillos.shared.physics.Angle;
import org.nusco.narjillos.shared.physics.Segment;
import org.nusco.narjillos.shared.physics.Vector;
import org.nusco.narjillos.shared.physics.ZeroVectorException;
import org.nusco.narjillos.shared.utilities.Configuration;

/**
 * The physical body of a Narjillo, with all its organs and their position in
 * space.
 * 
 * This class contains the all-important Body.tick() method. Look at its
 * comments for details.
 */
public class Body {

	private final MovingOrgan head;
	private final double metabolicConsumption;
	private final double adultMass;
	private double mass;
	private transient List<Organ> organs;

	public Body(MovingOrgan head) {
		this.head = head;
		adultMass = calculateAdultMass();
		this.metabolicConsumption = Math.pow(getHead().getMetabolicRate(), Configuration.ENERGY_METABOLIC_CONSUMPTION_POW);
	}

	public Head getHead() {
		return (Head) head;
	}

	public List<Organ> getOrgans() {
		if (organs == null) {
			organs = new ArrayList<>();
			addWithChildren(organs, head);
		}
		return organs;
	}

	public Vector getStartPoint() {
		return getHead().getStartPoint();
	}

	public double getAngle() {
		return Angle.normalize(getHead().getAbsoluteAngle() + 180);
	}

	public double getPercentEnergyToChildren() {
		return getHead().getPercentEnergyToChildren();
	}

	public double getMass() {
		double result = 0;
		for (Organ organ : getOrgans())
			result += organ.getMass();
		return result;
	}

	public double getGreenMass() {
		double result = 0;
		for (Organ organ : getOrgans())
			result += organ.getMass() * organ.getFiber().getPercentOfGreen();
		return result;
	}

	public double getRadius() {
		return calculateRadius(calculateCenterOfMass());
	}

	public double getAdultMass() {
		return adultMass;
	}

	public Vector calculateCenterOfMass() {
		// TODO: any way to avoid recalculating this here?
		// When I try to cache it, I get a weird bug with
		// the mass staying too low and narjillos zipping
		// around like crazy.
		double mass = getMass();

		if (mass <= 0)
			return getStartPoint();

		// do it in one swoop instead of creating a lot of
		// intermediate vectors

		List<Organ> organs = getOrgans();
		Vector[] weightedCentersOfMass = new Vector[organs.size()];
		Iterator<Organ> iterator = organs.iterator();
		for (int i = 0; i < weightedCentersOfMass.length; i++) {
			Organ organ = iterator.next();
			weightedCentersOfMass[i] = organ.getCenterOfMass().by(organ.getMass());
		}

		double totalX = 0;
		double totalY = 0;
		for (int i = 0; i < weightedCentersOfMass.length; i++) {
			totalX += weightedCentersOfMass[i].x;
			totalY += weightedCentersOfMass[i].y;
		}

		return Vector.cartesian(totalX / mass, totalY / mass);
	}

	public void teleportTo(Vector position) {
		final int northDirection = 90;
		getHead().forcePosition(position, northDirection);
	}

	/**
	 * Contains the core movement algorithm:
	 * 
	 * Take a target direction. Change the body's geometry based on the target
	 * direction. Move the body. Return the energy consumed on the entire
	 * operation.
	 * 
	 * Look inside for more details...
	 */
	public double tick(Vector targetDirection) {
		// Update the mass of a still-developing body.
		if (isStillGrowing())
			mass = getMass();

		// Before any movement, store away the current center of mass and the
		// angles and positions of all body parts. These will come useful later.
		// (Note that we could calculate the angles from the positions, but
		// computing angles is expensive - so it's faster to store the angles
		// away now that we already have them).
		Vector initialCenterOfMass = calculateCenterOfMass();
		Map<Organ, Double> initialAnglesOfOrgans = calculateAnglesOfOrgans();
		Map<Organ, Segment> initialPositionsOfOrgans = calculatePositionsOfOrgans();

		// This first step happens as if the body where in a vacuum.
		// The organs in the body remodel their own geometry based on the
		// target's direction. They don't "think" were to go - they just
		// changes their positions *somehow*. Natural selection will eventually
		// favor movements that result in getting closer to the target.
		tick_step1_updateAngles(targetDirection);

		// Changing the angles in the body results in a rotational force.
		// Rotate the body to match the force. In other words, keep the body's
		// moment of inertia equal to zero.
		double rotationEnergy = tick_step2_rotate(initialAnglesOfOrgans, initialPositionsOfOrgans, initialCenterOfMass, mass);

		// The previous updates moved the center of mass. Remember, we're
		// in a vacuum - so the center of mass shouldn't move. Let's put it
		// back to its original position.
		tick_step3_recenter(initialCenterOfMass);

		// Now we can finally move out of the "vacuum" reference system.
		// All the movements from the previous steps result in a different
		// body position in space, and this different position generates
		// translational forces. We can update the body position based on
		// these translations.
		double translationEnergy = tick_step4_translate(initialPositionsOfOrgans, initialCenterOfMass, mass);

		// We're done! Return the energy spent on the entire operation.
		return getEnergyConsumed(rotationEnergy, translationEnergy);
	}

	public double calculateRadius() {
		return calculateRadius(calculateCenterOfMass());
	}

	@Override
	public String toString() {
		return head.toString();
	}

	private void tick_step1_updateAngles(Vector targetDirection) {
		double angleToTarget = getAngleTo(targetDirection);
		getHead().tick(angleToTarget);
	}

	private double tick_step2_rotate(Map<Organ, Double> initialAnglesOfOrgans, Map<Organ, Segment> initialPositions, Vector centerOfMass, double mass) {
		RotationsPhysicsEngine forceField = new RotationsPhysicsEngine(mass, calculateRadius(centerOfMass), centerOfMass);
		for (Organ bodyPart : organs)
			forceField.registerMovement(initialAnglesOfOrgans.get(bodyPart), bodyPart.getAbsoluteAngle(), bodyPart.getPositionInSpace(),
					bodyPart.getMass(), getPush(bodyPart));
		getHead().rotateBy(forceField.getRotation());
		return forceField.getEnergy();
	}

	private void tick_step3_recenter(Vector centerOfMassBeforeReshaping) {
		Vector centerOfMassAfterUpdatingAngles = calculateCenterOfMass();
		Vector centerOfMassOffset = centerOfMassBeforeReshaping.minus(centerOfMassAfterUpdatingAngles);
		getHead().translateBy(centerOfMassOffset);
	}

	private double tick_step4_translate(Map<Organ, Segment> initialPositions, Vector centerOfMass, double mass) {
		TranslationsPhysicsEngine forceField = new TranslationsPhysicsEngine(mass);
		for (Organ bodyPart : organs)
			forceField.registerMovement(initialPositions.get(bodyPart), bodyPart.getPositionInSpace(), bodyPart.getMass(),
					getPush(bodyPart));
		getHead().translateBy(forceField.getTranslation());
		return forceField.getEnergy();
	}

	private double getPush(Organ bodyPart) {
		return 1 + bodyPart.getFiber().getPercentOfBlue() * Configuration.CREATURE_BLUE_FIBERS_EXTRA_PUSH;
	}

	private double getEnergyConsumed(double rotationEnergy, double translationEnergy) {
		return (rotationEnergy + translationEnergy) * metabolicConsumption;
	}

	private void addWithChildren(List<Organ> result, MovingOrgan organ) {
		// children first
		for (ConnectedOrgan child : organ.getChildren())
			addWithChildren(result, (MovingOrgan) child);
		result.add(organ);
	}

	private boolean isStillGrowing() {
		return mass < getAdultMass();
	}

	private double getAngleTo(Vector direction) {
		if (direction.equals(Vector.ZERO))
			return 0;
		try {
			return Angle.normalize(getAngle() - direction.getAngle());
		} catch (ZeroVectorException e) {
			throw new RuntimeException(e); // should never happen
		}
	}

	private Map<Organ, Segment> calculatePositionsOfOrgans() {
		Map<Organ, Segment> result = new LinkedHashMap<>();
		for (Organ organ : getOrgans())
			result.put(organ, organ.getPositionInSpace());
		return result;
	}

	private Map<Organ, Double> calculateAnglesOfOrgans() {
		Map<Organ, Double> result = new LinkedHashMap<>();
		for (Organ organ : getOrgans())
			result.put(organ, organ.getAbsoluteAngle());
		return result;
	}

	private double calculateAdultMass() {
		double result = 0;
		for (Organ organ : getOrgans())
			result += organ.getAdultMass();
		return result;
	}

	private double calculateRadius(Vector centerOfMass) {
		final double MIN_RADIUS = 1;
		double result = MIN_RADIUS;
		for (Organ bodyPart : getOrgans()) {
			double startPointDistance = bodyPart.getStartPoint().minus(centerOfMass).getLength();
			double endPointDistance = bodyPart.getEndPoint().minus(centerOfMass).getLength();
			double distance = Math.max(startPointDistance, endPointDistance);
			if (distance > result)
				result = distance;
		}
		return result;
	}
}
