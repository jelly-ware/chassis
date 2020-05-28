package org.jellyware.chassis.schema;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class ReflectionUtils {
	public static Map<TypeVariable<? extends Class<?>>, Type> typeArgsMap(Type type) {
		var map = new HashMap<TypeVariable<? extends Class<?>>, Type>();
		if (type instanceof ParameterizedType) {
			try {
				var pType = (ParameterizedType) type;
				var cls = Class.forName(pType.getRawType().getTypeName());
				var typeVars = cls.getTypeParameters();
				var actualTypeArgs = pType.getActualTypeArguments();
				for (int i = 0; i < typeVars.length; i++) {
					map.put(typeVars[i], actualTypeArgs[i]);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	public static Type resolveTypeVariable(Type type, Map<TypeVariable<? extends Class<?>>, Type> map) {
		return (type instanceof TypeVariable) ? map.get(type) : type;
	}

	public static Set<Field> getDeclaredFields(Type type, Predicate<Class<?>> exclusiveParent) {
		var declaredFields = new HashSet<Field>();
		var rawType = (Class<?>) ((type instanceof ParameterizedType) ? ((ParameterizedType) type).getRawType() : type);
		while (rawType != null && exclusiveParent.test(rawType)) {
			for (var f : rawType.getDeclaredFields()) {
				declaredFields.add(f);
			}
			rawType = rawType.getSuperclass();
		}
		return declaredFields;
	}
}
