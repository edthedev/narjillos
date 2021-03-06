package org.nusco.narjillos.genomics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.nusco.narjillos.shared.utilities.RanGen;

public class GenePoolTest {
	
	RanGen ranGen = new RanGen(1234);
	GenePool genePool = new GenePool();

	@Before
	public void setUpGenePool() {
		genePool.enableTracking();
	}
	
	@Test
	public void doesNotTrackByDefault() {
		GenePool disabledGenePool = new GenePool();
		
		disabledGenePool.createDNA("{0}");

		assertEquals(0, disabledGenePool.getCurrentPoolSize());
	}
	
	@Test
	public void tracksAncestry() {
		DNA parent1 = genePool.createDNA("{0}");
		genePool.mutateDNA(parent1, ranGen);

		DNA parent2 = genePool.createDNA("{0}");
		DNA child2_1 = genePool.mutateDNA(parent2, ranGen);
		genePool.mutateDNA(parent2, ranGen);

		DNA child2_2_1 = genePool.mutateDNA(child2_1, ranGen);

		List<DNA> ancestry = genePool.getAncestry(child2_2_1);
		
		assertEquals(3, ancestry.size());
		assertEquals(parent2, ancestry.get(0));
		assertEquals(child2_1, ancestry.get(1));
		assertEquals(child2_2_1, ancestry.get(2));
	}
	
	@Test
	public void getsMostSuccessfulDNA() {
		genePool.createDNA("111_111_111_222_111_000_000_000_000_000_000");
		genePool.createDNA("111_111_111_111_111_000_000_000_000_000_000");
		genePool.createDNA("111_111_111_222_222_000_000_000_000_000_000");
		genePool.createDNA("111_111_222_111_222_000_000_000_000_000_000");
		genePool.createDNA("111_222_222_222_222_000_000_000_000_000_000");

		DNA mostSuccessful = genePool.getMostSuccessfulDNA();
		
		assertEquals("{111_111_111_111_111_000_000_000_000_000_000}", mostSuccessful.toString());
	}
	
	@Test
	public void getsNullAsTheMostSuccesfulInAnEmptyPool() {
		assertNull(genePool.getMostSuccessfulDNA());
	}
	
	@Test
	public void canRemoveDNA() {
		genePool.createDNA("111_111_111_222_111_000_000_000_000_000_000");
		DNA thisOneWillDie = genePool.createDNA("111_111_111_111_111_000_000_000_000_000_000");
		genePool.createDNA("111_111_111_222_222_000_000_000_000_000_000");
		genePool.createDNA("111_111_222_111_222_000_000_000_000_000_000");
		genePool.createDNA("111_222_222_222_222_000_000_000_000_000_000");

		genePool.remove(thisOneWillDie);
		DNA mostSuccessful = genePool.getMostSuccessfulDNA();
		
		assertEquals("{111_111_111_222_111_000_000_000_000_000_000}", mostSuccessful.toString());
	}
	
	@Test
	public void neverRemovesDNAFromHistory() {
		for (int i = 0; i < 100; i++) {
			DNA dna = genePool.createRandomDNA(ranGen);
			if (i >= 50)
				genePool.remove(dna);
		}
		
		assertEquals(50, genePool.getCurrentPoolSize());
		assertEquals(100, genePool.getHistoricalPoolSize());
	}
}
