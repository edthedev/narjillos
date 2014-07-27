package org.nusco.narjillos.creature.genetics;

import java.util.LinkedList;
import java.util.List;

import org.nusco.narjillos.shared.utilities.RanGen;

public class DNA {

	private static final int MUTATION_RANGE = 30;
	public static final int CHROMOSOME_SIZE = 6;
	public static final double MUTATION_RATE = 0.05;

	private final Integer[] genes;

	DNA(Integer[] genes) {
		this.genes = clipToByteSize(genes);
	}

	public DNA(String dnaDocument) {
		this(new DNADocument(dnaDocument).toGenes());
	}

	public Integer[] getGenes() {
		return genes;
	}

	public DNA copy() {
		List<Integer[]> resultChromosomes = new LinkedList<>();

		DNAParser parser = new DNAParser(this);
		int[] nextChromosome;
		while((nextChromosome = parser.nextChromosome()) != null) {
			// skip a chromosome every now and then
			if (!mutationHappens())
				resultChromosomes.add(copy(nextChromosome));
			// add a chromosome every now and then
			if (mutationHappens())
				resultChromosomes.add(randomGenes(CHROMOSOME_SIZE));
		}

		Integer[] resultGenes = flatten(resultChromosomes);
		return new DNA(resultGenes);
	}

	private Integer[] flatten(List<Integer[]> chromosomes) {
		List<Integer> result = new LinkedList<>();
		for (Integer[] chromosome : chromosomes)
			for (Integer gene : chromosome)
				result.add(gene);
		return result.toArray(new Integer[result.size()]);
	}

	private Integer[] copy(int[] chromosome) {
		Integer[] result = new Integer[chromosome.length];
		for (int i = 0; i < result.length; i++)
			result[i] = copy(chromosome[i]);
		return result;
	}

	private int copy(int gene) {
		return mutationHappens() ? mutate(gene) : gene;
	}

	private int mutate(int gene) {
		int randomFactor = ((int) (RanGen.nextDouble() * MUTATION_RANGE * 2)) - MUTATION_RANGE;
		return gene + randomFactor;
	}

	private boolean mutationHappens() {
		return RanGen.nextDouble() < MUTATION_RATE;
	}

	private Integer[] clipToByteSize(Integer[] genes) {
		Integer[] result = new Integer[genes.length];
		for (int i = 0; i < result.length; i++)
			result[i] = clipToByteSize(genes[i]);
		return result;
	}

	private int clipToByteSize(int number) {
		if (number < 0)
			number = 0;
		if (number > 255)
			number = 255;
		return number;
	}

	public static DNA random() {
		int size = CHROMOSOME_SIZE * (Math.abs(RanGen.nextInt()) % 10 + 2);
		return random(size);
	}

	public static DNA random(int size) {
		Integer[] genes = randomGenes(size);
		return new DNA(genes);
	}

	private static Integer[] randomGenes(int size) {
		Integer[] genes = new Integer[size];
		for (int i = 0; i < genes.length; i++)
			genes[i] = RanGen.nextByte();
		return genes;
	}

	public int getDistanceFrom(DNA other) {
		Integer[] theseGenes = getGenes();
		Integer[] otherGenes = other.getGenes();

		if (theseGenes.length > otherGenes.length)
			return other.getDistanceFrom(this);
			
		int result = 0;
		for (int i = 0; i < theseGenes.length; i++)
			if (!theseGenes[i].equals(otherGenes[i]))
				result++;
		result += (otherGenes.length - theseGenes.length);
		return result;
	}

	@Override
	public String toString() {
		return DNADocument.toString(this);
	}
}