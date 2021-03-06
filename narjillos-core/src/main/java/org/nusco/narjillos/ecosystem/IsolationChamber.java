package org.nusco.narjillos.ecosystem;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.nusco.narjillos.creature.Narjillo;
import org.nusco.narjillos.embryogenesis.Embryo;
import org.nusco.narjillos.genomics.DNA;
import org.nusco.narjillos.genomics.GenePool;
import org.nusco.narjillos.shared.physics.Segment;
import org.nusco.narjillos.shared.physics.Vector;
import org.nusco.narjillos.shared.things.Energy;
import org.nusco.narjillos.shared.things.Thing;
import org.nusco.narjillos.shared.utilities.RanGen;

/**
 * The place that Narjillos call "home".
 */
public class IsolationChamber extends Environment {

	private static final Set<Thing> EMPTY_SET = Collections.unmodifiableSet(new LinkedHashSet<Thing>());

	private Set<Narjillo> narjillos = new LinkedHashSet<>();

	public IsolationChamber(long size, RanGen ranGen) {
		super(size);
	
		DNA dna = DNA.random(1, ranGen);
		Narjillo narjillo = new Narjillo(dna, new Embryo(dna).develop(), Vector.cartesian(size, size).by(0.5), Energy.INFINITE);
		narjillos.add(narjillo);
	}

	@Override
	public Set<Thing> getThings(String label) {
		if (label.equals("narjillo") || label.equals(""))
			return new LinkedHashSet<Thing>(narjillos);
		return EMPTY_SET;
	}

	@Override
	protected void tickThings(GenePool genePool, RanGen ranGen) {
		tickNarjillos(narjillos);
	}

	@Override
	protected Set<Thing> getCollisions(Segment movement) {
		return EMPTY_SET;
	}

	@Override
	public String getStatistics() {
		return "TODO: statistics in IsolationEnvironment";
	}
}
