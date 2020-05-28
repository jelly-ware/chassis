package org.jellyware.chassis.schema;

import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author Jotter
 *
 */
public class Type {
	private String title, mdl;
	private Elementary type;
	private Set<Annotation> validations, uis;
	private Map<String, Type> properties;
	private Type element, key, value;
	{
		validations = new HashSet<>();
		uis = new HashSet<>();
		properties = new HashMap<>();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMdl() {
		return mdl;
	}

	public void setMdl(String mdl) {
		this.mdl = mdl;
	}

	public Type mdl(String mdl) {
		setMdl(mdl);
		return this;
	}

	public Elementary getType() {
		return type;
	}

	public void setType(Elementary type) {
		this.type = type;
	}

	public Set<Annotation> getValidations() {
		return validations;
	}

	public void setValidations(Set<Annotation> validations) {
		this.validations = validations;
	}

	public Set<Annotation> getUis() {
		return uis;
	}

	public void setUis(Set<Annotation> uis) {
		this.uis = uis;
	}

	public Map<String, Type> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Type> properties) {
		this.properties = properties;
	}

	public Type getElement() {
		return element;
	}

	public void setElement(Type element) {
		this.element = element;
	}

	public void addProp(String key, Type value) {
		properties.put(key, value);
	}

	public void addValidation(Annotation validation) {
		validations.add(validation);
	}

	public void addUi(Annotation ui) {
		uis.add(ui);
	}

	public Type getKey() {
		return key;
	}

	public void setKey(Type key) {
		this.key = key;
	}

	public Type getValue() {
		return value;
	}

	public void setValue(Type value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Type [title=" + title + ", mdl=" + mdl + ", type=" + type + ", validations=" + validations + ", uis="
				+ uis + ", properties=" + properties + ", element=" + element + ", key=" + key + ", value=" + value
				+ "]";
	}

	public static enum Elementary {
		COLLECTION, OBJECT, STRING, NUMBER, BOOLEAN, VOID, SELF, RECURRENT, MAP, ENUM;

		public static final Set<Class<?>> STRING_CLASSES = Set.of(CharSequence.class, Character.class, UUID.class,
				Date.class, TemporalAccessor.class, ZoneId.class);

		public static final boolean isDerivative(Elementary e) {
			return e == SELF || e == RECURRENT;
		}

		public static final Optional<Elementary> of(final java.lang.reflect.Type type) {
			if (!(type instanceof Class))
				return Optional.empty();

			Class<?> cls = org.jellyware.toolkit.Type.box((Class<?>) type);

			Elementary e = OBJECT;
			if (Collection.class.isAssignableFrom(cls) || cls.getName().charAt(0) == '[')
				e = COLLECTION;
			if (Map.class.isAssignableFrom(cls))
				e = MAP;
			// if (Entity.class.isAssignableFrom(cls))
			// elementaryType = OBJECT;
			if (Number.class.isAssignableFrom(cls))
				e = NUMBER;
			if (Boolean.class.isAssignableFrom(cls))
				e = BOOLEAN;
			if (STRING_CLASSES.stream().anyMatch(c -> c.isAssignableFrom(cls)))
				e = STRING;
			if (Void.class.isAssignableFrom(cls))
				e = VOID;
			if (Enum.class.isAssignableFrom(cls))
				e = ENUM;
			return Optional.ofNullable(e);
		}
	}
}
