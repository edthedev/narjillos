package org.nusco.swimmers.creature;

import java.util.LinkedList;
import java.util.List;

import org.nusco.swimmers.creature.body.BodyPart;
import org.nusco.swimmers.creature.body.Head;
import org.nusco.swimmers.creature.body.Organ;
import org.nusco.swimmers.creature.genetics.DNA;
import org.nusco.swimmers.creature.physics.ForceField;
import org.nusco.swimmers.shared.physics.Segment;
import org.nusco.swimmers.shared.physics.Vector;
import org.nusco.swimmers.shared.things.Thing;

public class Narjillo implements Thing {

	public static final double MAX_ENERGY = 500_000_000;
	static final double INITIAL_ENERGY = 250_000_000;
	private static final double ENERGY_PER_FOOD_ITEM = 250_000_000;
	private static final double NATURAL_ENERGY_DECAY = 100;

	private static final double PROPULSION_SCALE = 0.3;
	
	private final Head head;
	private final double mass;

	private Vector position;
	private Vector target = Vector.ZERO;
	private double energy = INITIAL_ENERGY;

	private final List<SwimmerEventListener> swimmerEventListeners = new LinkedList<>();
	private final DNA genes;

	public Narjillo(Head head, DNA genes) {
		this.head = head;
		this.genes = genes;
		mass = calculateTotalMass();
	}

	@Override
	public Vector getPosition() {
		return position;
	}

	@Override
	public void setPosition(Vector position) {
		this.position = position;
	}
	
	private void updatePosition(Vector position) {
		Vector start = getPosition();
		setPosition(position);
		
		for (SwimmerEventListener swimmerEventListener : swimmerEventListeners)
			swimmerEventListener.moved(new Segment(start, getPosition()));
	}

	@Override
	public void tick() {
		Vector targetDirection = getTargetDirection();

		ForceField forceField = head.createForceField();

		head.tick(targetDirection);

		// TODO: physical movement should probably move
		// outside this class. create a Body class that encapsulates
		// all body operations (except for construction and visualization).
		// the entire body should be just a machine that mechanically reacts
		// to a simple input target vector
		Vector force = forceField.getTotalForce().by(PROPULSION_SCALE);
		
		Vector movement = calculateMovement(force);
		
		Vector newPosition = getPosition().plus(movement);
		updatePosition(newPosition);

		double energySpentForMovement = forceField.getTotalEnergySpent() * getMetabolicRate();
		decreaseEnergy(energySpentForMovement + NATURAL_ENERGY_DECAY);
	}

	private double getMetabolicRate() {
		// FIXME: the metabolic rate shouldn't be stored in the head.
		// the entire narjillo/head/neck thing should be rethought
		return getHead().getMetabolicRate();
	}

	private Vector calculateMovement(Vector force) {
		return force;
		
		// FIXME: as soon as I take mass into account, there is
		// a double penalty for mass: more energy consumption,
		// and slower movement. this makes higher mass a sure-fire loss.
		// It should be either/or, and more balanced.
		
		// zero mass can actually happen
//		if (getMass() == 0)
//			return force;
//		return force.by(1.0 / Math.pow(getMass(), 0.8));
	}

	@Override
	public String getLabel() {
		return "swimmer";
	}
	
	public Head getHead() {
		return head;
	}

	public DNA getGenes() {
		return genes;
	}

	public double getEnergy() {
		return energy;
	}

	public void feed() {
		energy += ENERGY_PER_FOOD_ITEM;
		if (energy > MAX_ENERGY)
			energy = MAX_ENERGY;
	}

	void decreaseEnergy(double amount) {
		energy -= amount;
		if (energy <= 0)
			for (SwimmerEventListener swimmerEventListener : swimmerEventListeners)
				swimmerEventListener.died();
	}

	public Vector getTargetDirection() {
		return target.minus(position).normalize(1);
	}

	public void setTarget(Vector target) {
		this.target = target;
	}

	public void addSwimmerEventListener(SwimmerEventListener lifecycleEventListener) {
		swimmerEventListeners.add(lifecycleEventListener);
	}

	public double getMass() {
		return mass;
	}

	private double calculateTotalMass() {
		double result = 0;
		List<Organ> allOrgans = getAllOrgans();
		for (Organ organ : allOrgans)
			result += organ.getMass();
		return result;
	}

	private List<Organ> getAllOrgans() {
		List<Organ> result = new LinkedList<>();
		addWithChildren(getHead(), result);
		return result;
	}

	private void addWithChildren(BodyPart organ, List<Organ> result) {
		result.add(organ);
		for (BodyPart child : organ.getChildren())
			addWithChildren(child, result);
	}
}