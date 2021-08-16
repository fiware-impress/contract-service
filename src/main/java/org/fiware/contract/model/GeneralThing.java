package org.fiware.contract.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class GeneralThing extends Thing {

	private String thingType;

	private Map<String, Object> additionalProperties;

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return additionalProperties;
	}

	@JsonAnySetter
	public GeneralThing setAdditionalProperties(String propertyKey, Object value) {
		if (this.additionalProperties == null) {
			this.additionalProperties = new HashMap<>();
		}
		this.additionalProperties.put(propertyKey, value);
		return this;
	}
}
