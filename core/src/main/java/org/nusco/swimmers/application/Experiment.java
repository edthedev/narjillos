package org.nusco.swimmers.application;

import org.nusco.swimmers.creature.Narjillo;
import org.nusco.swimmers.pond.Pond;
import org.nusco.swimmers.shared.utilities.Chronometer;
import org.nusco.swimmers.shared.utilities.RanGen;

public class Experiment {

	private static final long SEED = 2648718169735535616l;
	private static final int CYCLES = 1_000_000_000;
	private static final int PARSE_INTERVAL = 10_000;

	private static final Chronometer ticksChronometer = new Chronometer();

	public static void main(String... args) {
		RanGen.seed(SEED);

		System.out.println("Starting...");
		long startTime = System.currentTimeMillis();

		runExperiment();

		long endTime = System.currentTimeMillis();
		double timeInSeconds = ((double) (endTime - startTime)) / 1000;
		System.out.println("Done (" + timeInSeconds + "s)");
	}

	private static void runExperiment() {
		Pond pond = new Cosmos();
		for (int i = 0; i < CYCLES; i++) {
			pond.tick();
			ticksChronometer.tick();
			if (i % PARSE_INTERVAL == 0)
				System.out.println(getStatusString(pond, i));
		}
	}

	private static String getStatusString(Pond pond, int tick) {
		if (pond.getNumberOfNarjillos() == 0)
			return 	tick + ", " +
					ticksChronometer.getTicksInLastSecond() + ", " +
					pond.getNumberOfNarjillos() + ", " +
					pond.getNumberOfFoodPieces();

		Narjillo mostProlificNarjillo = pond.getMostProlificNarjillo();
		return 	tick + ", " +
				ticksChronometer.getTicksInLastSecond() + ", " +
				pond.getNumberOfNarjillos() + ", " +
				pond.getNumberOfFoodPieces() + ", " +
				mostProlificNarjillo.getNumberOfDescendants() + ", " +
				mostProlificNarjillo.getGenes();
	}
}