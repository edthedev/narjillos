package org.nusco.narjillos.serializer;

import org.nusco.narjillos.shared.things.Energy;
import org.nusco.narjillos.shared.things.LifeFormEnergy;

import com.google.gson.JsonParseException;

class EnergyAdapter extends HierarchyAdapter<Energy> {

	@Override
	protected String getTypeTag(Energy obj) {
		if (obj instanceof LifeFormEnergy)
			return "life_form_energy";
		return "<unsupported_energy_type>";
	}

	@Override
	protected Class<?> getClass(String typeTag) throws JsonParseException {
		if (typeTag.equals("life_form_energy"))
			return LifeFormEnergy.class;
		throw new RuntimeException("Unsupported subtype of Energy: " + typeTag);
	}
}
