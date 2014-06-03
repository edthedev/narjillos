package org.nusco.swimmers.creature.genetics;

import java.util.LinkedList;
import java.util.List;

import org.nusco.swimmers.creature.body.Head;
import org.nusco.swimmers.creature.body.Organ;
import org.nusco.swimmers.creature.body.Side;

public class ExampleParts {

	public final static Organ HEAD = new Head(60, 6, 123);
	public final static Organ CHILD_1 = HEAD.sproutOrgan(50, 9, 30, Side.RIGHT, 123);
	public final static Organ CHILD_2 = HEAD.sproutOrgan(50, 9, -30, Side.LEFT, 123);
	public final static Organ CHILD_1_1 = CHILD_1.sproutOrgan(30, 7, 30, Side.RIGHT, 123);
	public final static Organ CHILD_1_2 = CHILD_1.sproutOrgan(30, 7, -30, Side.LEFT, 123);
	public final static Organ CHILD_2_1 = CHILD_2.sproutOrgan(30, 7, 30, Side.RIGHT, 123);
	public final static Organ CHILD_2_2 = CHILD_2.sproutOrgan(30, 7, -30, Side.LEFT, 123);
	public final static Organ CHILD_1_1_1 = CHILD_1_1.sproutOrgan(50, 6, 20, Side.RIGHT, 123);
	public final static Organ CHILD_1_1_2 = CHILD_1_1.sproutOrgan(50, 6, -20, Side.LEFT, 123);
	public final static Organ CHILD_2_2_1 = CHILD_2_2.sproutOrgan(50, 6, 20, Side.RIGHT, 123);
	public final static Organ CHILD_2_2_2 = CHILD_2_2.sproutOrgan(50, 6, -20, Side.LEFT, 123);

	public static List<Organ> asList() {
		List<Organ> expected = new LinkedList<>();
		expected.add(HEAD);
		expected.add(CHILD_1);
		expected.add(CHILD_1_1);
		expected.add(CHILD_1_1_1);
		expected.add(CHILD_1_1_2);
		expected.add(CHILD_1_2);
		expected.add(CHILD_2);
		expected.add(CHILD_2_1);
		expected.add(CHILD_2_2);
		expected.add(CHILD_2_2_1);
		expected.add(CHILD_2_2_2);
		return expected;
	}
}