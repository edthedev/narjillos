package org.nusco.narjillos.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.nusco.narjillos.creature.Narjillo;
import org.nusco.narjillos.ecosystem.Ecosystem;
import org.nusco.narjillos.embryogenesis.Embryo;
import org.nusco.narjillos.genomics.DNA;
import org.nusco.narjillos.shared.physics.Vector;
import org.nusco.narjillos.shared.things.Energy;
import org.nusco.narjillos.shared.utilities.RanGen;

public class LocatorTest {
	
	Ecosystem ecosystem;
	Locator locator;
	RanGen ranGen = new RanGen(1234);
	
	@Before
	public void initialize() {
		ecosystem = new Ecosystem(1000, false);
		locator = new Locator(ecosystem);
	}

	private Narjillo insertNarjillo(Vector position) {
		DNA dna = new DNA(1, "{145_227_116_072_163_201_077_221_217}{060_227_157_252_209_149_056_114_167}{250_253_092_189_010_247_016_214_009}{027_039_203_179_042_042_175_110_008}");
		Narjillo narjillo = new Narjillo(dna, new Embryo(dna).develop(), position, Energy.INFINITE);
		ecosystem.insertNarjillo(narjillo);
		return narjillo;
	}
	
	@Test
	public void findsThingsAtAGivenPosition() {
		Narjillo narjillo1 = insertNarjillo(Vector.cartesian(1000, 1000));
		Narjillo narjillo2 = insertNarjillo(Vector.cartesian(100, 100));
		assertTrue(narjillo1.getRadius() > 4 && narjillo1.getRadius() < 300);
		assertTrue(narjillo2.getRadius() > 4 && narjillo2.getRadius() < 300);

		assertNull(locator.findNarjilloAt(Vector.cartesian(500, 500)));
		assertEquals(narjillo2, locator.findNarjilloAt(Vector.cartesian(103, 103)));
		assertEquals(narjillo1, locator.findNarjilloAt(Vector.cartesian(999, 1001)));
	}
	
	@Test
	public void returnsNullIfNoThingIsCloseEnough() {
		insertNarjillo(Vector.cartesian(100, 10));
		
		assertNull(locator.findNarjilloAt(Vector.cartesian(500, 500)));
	}

	@Test
	public void returnsNullIfTheEcosystemContainsNoNarjillos() {
		Locator emptyLocator = new Locator(new Ecosystem(1000, false));
		
		assertNull(emptyLocator.findNarjilloAt(Vector.cartesian(150, 150)));
	}
}
