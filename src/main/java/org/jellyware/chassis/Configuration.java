/**
 * 
 */
package org.jellyware.chassis;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Optional;

import org.jellyware.trinity.Entity;

/**
 * @author Jotterå
 *
 */
public interface Configuration<K extends Serializable> {
	void remove(String key, Entity.Model<Long> context);

	default void remove(String key) {
		remove(key, null);
	}

	default void put(String key, String value) {
		put(key, value, null);
	}

	void put(String key, String value, Entity.Model<K> context);

	default void put(String key, Object value) {
		put(key, value, null);
	}

	void put(String key, Object value, Entity.Model<K> context);

	default Optional<String> get(String key) {
		return get(key, (Entity.Model<K>) null);
	}

	Optional<String> get(String key, Entity.Model<K> context);

	default <T> Optional<T> get(String key, Type cls) {
		return get(key, cls, null);
	}

	<T> Optional<T> get(String key, Type cls, Entity.Model<K> context);

	default <T> Optional<T> get(String key, Class<T> cls) {
		return get(key, cls, null);
	}

	<T> Optional<T> get(String key, Class<T> cls, Entity.Model<K> context);
}
