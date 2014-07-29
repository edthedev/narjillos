package org.nusco.narjillos.creature.body.embryology;

import java.util.LinkedList;
import java.util.List;

import org.nusco.narjillos.creature.body.BodyPart;

class TwinOrgansBuilder {

	static final int MIRROR_ORGAN_BIT = 0b00000001;

	private final int[] organ1Genes;
	private final int[] organ2Genes;

	public TwinOrgansBuilder(int[] organ1Genes, int[] organ2Genes) {
		this.organ1Genes = organ1Genes;
		this.organ2Genes = organ2Genes;
	}

	private boolean isMirrorSegment(int[] genes) {
		int controlGene = genes[0];
		return (controlGene & TwinOrgansBuilder.MIRROR_ORGAN_BIT) == TwinOrgansBuilder.MIRROR_ORGAN_BIT;
	}

	public List<BodyPart> buildBodyPart(BodyPart parent) {
		List<BodyPart> result = new LinkedList<>();
		
		if (organ1Genes == null)
			return result;

		if (organ2Genes == null) {
			result.add(new OrganBuilder(organ1Genes).buildBodyPart(parent, 1));
			return result;
		}
		
		if(isMirrorSegment(organ1Genes))
			return buildMirrorSegments(parent, organ2Genes);
		
		if(isMirrorSegment(organ2Genes))
			return buildMirrorSegments(parent, organ1Genes);
		
		result.add(new OrganBuilder(organ1Genes).buildBodyPart(parent, 1));
		result.add(new OrganBuilder(organ2Genes).buildBodyPart(parent, -1));
		return result;
	}

	private List<BodyPart> buildMirrorSegments(BodyPart parent, int[] genes) {
		List<BodyPart> result = new LinkedList<>();
		result.add(new OrganBuilder(genes).buildBodyPart(parent, 1));
		result.add(new OrganBuilder(genes).buildBodyPart(parent, -1));
		return result;
	}
}
