package org.jellyware.chassis.schema;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jotter
 *
 */
public class Annotation {
	private String type;
	private Map<String, Object> params;
	{
		params = new HashMap<>();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public void addParam(String key, Object value) {
		params.put(key, value);
	}
}
