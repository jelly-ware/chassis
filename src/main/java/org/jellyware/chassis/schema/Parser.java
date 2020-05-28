package org.jellyware.chassis.schema;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonValue;
import javax.validation.Constraint;

import org.jellyware.beef.Beef;
import org.jellyware.chassis.schema.annot.Field;
import org.jellyware.chassis.schema.annot.FormControl;
import org.jellyware.chassis.schema.annot.UI;
import org.jellyware.chassis.schema.annot.View;
import org.jellyware.chassis.schema.annot.fc.Binary;
import org.jellyware.chassis.schema.annot.fc.DateTime;
import org.jellyware.chassis.schema.annot.fc.Input;
import org.jellyware.chassis.schema.annot.fc.PickOne;
import org.jellyware.chassis.schema.Type.Elementary;
import org.jellyware.trinity.Entity;
import org.jellyware.trinity.Entity.Trio;
import org.jellyware.trinity.Entity.Trio.Pouch;

public class Parser {
	private static final Set<String> INAPT_VAL_MTHDS = Set.of("message", "groups", "payload");
	public static final Set<String> RSVD_FIELDS = Set.of("id", "created", "createdBy", "updated", "updatedBy",
			"constrained", "status", "display");
	private final Pouch<Long> pouch;

	@Inject
	public Parser(Pouch<Long> pouch) {
		super();
		this.pouch = pouch;
	}

	@SuppressWarnings("all")
	private final org.jellyware.chassis.schema.Type of(Type type, Set<Class<?>> rcsvs, Elementary et,
			Map<TypeVariable<? extends Class<?>>, Type> typeArgs) {
		type = ReflectionUtils.resolveTypeVariable(type, typeArgs);
		Type rawType;
		ParameterizedType pType = null;
		Type[] actualTypeArgs = {};
		if (type instanceof ParameterizedType) {
			pType = (ParameterizedType) type;
			rawType = pType.getRawType();
			actualTypeArgs = pType.getActualTypeArguments();
		} else {
			rawType = type;
		}
		Optional<Trio<? extends Entity.Model<Long>, ? extends Entity.Persistence<Long>, ? extends Entity.Serial<Long>, Long>> trio = Optional
				.empty();
		if (Entity.class.isAssignableFrom((Class<?>) rawType))
			trio = Optional.of(pouch.of((Class<? extends Entity<Long>>) rawType));
		var mdl = rawType;
		if (trio.isPresent()) {
			mdl = trio.orElseThrow(
					() -> Beef.internal().as(b -> b.when("Parsing Schema").detail("Could not infer mdl")).build())
					.mdl();
		}

		var schema = new org.jellyware.chassis.schema.Type();
		schema.setType(Elementary.isDerivative(et) ? et
				: Elementary.of(mdl).orElseThrow(() -> Beef.internal()
						.as(b -> b.when("Parsing Schema").detail("Could not infer elementary type")).build()));
		schema.setMdl(mdl.getTypeName());
		schema.setTitle(mdl.getTypeName().substring(mdl.getTypeName().lastIndexOf(".") + 1));

		// attach properties
		if (schema.getType() == Elementary.OBJECT && !JsonValue.class.isAssignableFrom((Class<?>) mdl))
			this.bindpProps((pType != null) ? pType : mdl, schema, rcsvs);

		// attach element
		if (schema.getType() == Elementary.COLLECTION) {
			var ser = trio.isPresent() ? trio.get().ser() : mdl;
			var e = actualTypeArgs.length == 1 ? actualTypeArgs[0] : ((Class<?>) rawType).getComponentType();
			schema.setElement(of(e, rcsvs,
					ser.equals(e) ? Elementary.SELF : rcsvs.contains(e) ? Elementary.RECURRENT : null, typeArgs));
		}

		// attache key value for map
		if (schema.getType() == Elementary.MAP) {
			schema.setKey(of(actualTypeArgs[0], rcsvs, typeArgs));
			schema.setValue(of(actualTypeArgs[1], rcsvs, typeArgs));
		}

		// bind annotations
		var ser = trio.isPresent() ? trio.get().ser() : (Class<?>) mdl;
		bindConAnnots(ser, schema, ser.getAnnotations());

		return schema;
	}

	public final org.jellyware.chassis.schema.Type of(Type type, Set<Class<?>> rcsvs,
			Map<TypeVariable<? extends Class<?>>, Type> typeArgs) {
		return of(type, rcsvs, null, typeArgs);
	}

	public final org.jellyware.chassis.schema.Type of(Type type) {
		return of(type, Set.of(), Map.of());
	}

	@SuppressWarnings("all")
	private void bindpProps(final Type type, final org.jellyware.chassis.schema.Type schema, Set<Class<?>> roots) {
		Type mdl;
		Map<TypeVariable<? extends Class<?>>, Type> typeArgsMap = new HashMap<>();
		if (type instanceof ParameterizedType) {
			var pType = (ParameterizedType) type;
			mdl = pType.getRawType();
			typeArgsMap = ReflectionUtils.typeArgsMap(pType);
		} else {
			mdl = type;
		}
		Optional<Trio<? extends Entity.Model<Long>, ? extends Entity.Persistence<Long>, ? extends Entity.Serial<Long>, Long>> trio = Optional
				.empty();
		if (Entity.class.isAssignableFrom((Class<?>) mdl))
			trio = Optional.of(pouch.of((Class<? extends Entity<Long>>) mdl));
		mdl = trio.isPresent() ? trio.get().ser() : mdl;
		var p = trio.isPresent();
		for (var f : ReflectionUtils.getDeclaredFields(mdl, t -> p ? Entity.class.isAssignableFrom(t)
				: (Object.class.isAssignableFrom(t) && !Object.class.equals(t)))) {
			var ser = trio.isPresent() ? trio.get().ser() : mdl;
			var rcsvs = new HashSet<>(roots);
			rcsvs.add((Class<?>) ser);
			var propSchema = of(f.getGenericType(), rcsvs, ser.equals(f.getType()) ? Elementary.SELF
					: rcsvs.contains(f.getType()) ? Elementary.RECURRENT : null, typeArgsMap);
			propSchema.setTitle(f.getName());

			bindConAnnots(
					(Class<?>) ReflectionUtils.resolveTypeVariable((f.getGenericType() instanceof ParameterizedType)
							? ((ParameterizedType) f.getGenericType()).getRawType()
							: f.getGenericType(), typeArgsMap),
					propSchema, f.getAnnotations());
			schema.addProp(f.getName(), propSchema);
		}
	}

	private void bindAnnots(final org.jellyware.chassis.schema.Type type, Annotation... annots) {
		type.getUis().clear();
		for (var annot : annots) {
			// validation
			if (annot.annotationType().isAnnotationPresent(Constraint.class))
				type.addValidation(parseAnnot(annot, INAPT_VAL_MTHDS));

			// ui
			if (annot.annotationType().isAnnotationPresent(UI.class))
				type.addUi(parseAnnot(annot, Set.of()));
		}
	}

	private void bindConAnnots(final Class<?> mdl, final org.jellyware.chassis.schema.Type type,
			Annotation... annotations) {
		// annotations
		var annots = new HashSet<Annotation>();
		// control flags for ui contingency annotatinos
		boolean hasFc, hasFormControl, hasField;
		hasFc = hasFormControl = hasField = false;
		for (var annot : annotations) {
			annots.add(annot);
			if (annot.getClass().getPackageName().equals("org.jellyware.chassis.schema.annot.fc"))
				hasFc = true;
			if (annot.getClass().getName().equals("org.jellyware.chassis.schema.annot.FormControl"))
				hasFormControl = true;
			if (annot.getClass().getName().equals("org.jellyware.chassis.schema.annot.Field"))
				hasField = true;
		}

		// parameters for form control
		String placeholder = type.getTitle(), defaultValue = "", icon = "", iconPack = "fas";

		// prepare contingencies
		switch (type.getType()) {
			case COLLECTION:
				icon = "layer-group";
				break;
			case BOOLEAN:
				defaultValue = "false";
				if (!hasFc)
					annots.add(new Binary.Literal());
				break;
			case NUMBER:
				icon = "draft2digital";
				defaultValue = "0";
				if (!hasFc) {
					var boxedCls = org.jellyware.toolkit.Type.box(mdl);
					if (BigDecimal.class.isAssignableFrom(boxedCls)) {
						annots.add(new Input.Literal(4, Double.MIN_VALUE, Double.MAX_VALUE, 0.0001F, true));
					} else if (Double.class.isAssignableFrom(boxedCls)) {
						annots.add(new Input.Literal(2, Double.MIN_VALUE, Double.MAX_VALUE, 0.01F, true));
					} else {
						annots.add(new Input.Literal(0, Double.MIN_VALUE, Double.MAX_VALUE, 1, true));
					}
				}
				break;
			case ENUM:
				icon = "dice-one";
				if (!hasFc)
					annots.add(new PickOne.Literal());
				break;
			case STRING:
				if (Temporal.class.isAssignableFrom(mdl)) {
					if (LocalDate.class.isAssignableFrom(mdl) || LocalDateTime.class.isAssignableFrom(mdl)) {
						icon = "calendar-day";
						if (!hasFc)
							annots.add(new DateTime.Literal(DateTime.Type.DATE));
					} else {
						icon = "digital-tachograph";
						if (!hasFc)
							annots.add(new DateTime.Literal(DateTime.Type.TIME));
					}
				} else {
					icon = "font";
					if (!hasFc)
						annots.add(new Input.Literal(Input.Type.TEXT));
				}
				break;
			case VOID:
				break;
			// defaults to object or derivative
			default:
				icon = "object-group";
				if (!hasFc)
					annots.add(new PickOne.Literal(true));
				break;
		}

		// add form control
		if (!hasFormControl)
			annots.add(new FormControl.Literal(placeholder, defaultValue, icon, iconPack));

		// add field
		if (!hasField)
			annots.add(new Field.Literal(placeholder));

		// view
		if (type.getType() == Elementary.OBJECT)
			bindDefaultView(mdl, annots);

		// clear sets and bind annots
		bindAnnots(type, annots.toArray(Annotation[]::new));
	}

	private void bindDefaultView(final Class<?> mdl, Set<Annotation> annots) {
		// collect views
		Supplier<Stream<View>> views = () -> annots.stream()
				.filter(annot -> annot.getClass().getName().contains("org.jellyware.chassis.schema.annot.View"))
				.flatMap(a -> a.getClass().getName().contains("List") ? Stream.of(((View.List) a).value())
						: Stream.of((View) a));

		// return if already contains default
		Stream<View> dflt = Stream.of();
		if (views.get().noneMatch(view -> view.name().equals(View.DEFAULT))) {
			// prep sequence
			var fieldSequence = new HashMap<java.lang.reflect.Field, Integer>();

			var isEntity = Entity.class.isAssignableFrom(mdl);
			var rawType = mdl;
			var seq = 100;
			while (rawType != null && (isEntity ? Entity.class.isAssignableFrom(mdl)
					: (Object.class.isAssignableFrom(mdl) && !Object.class.equals(mdl)))) {
				for (var f : rawType.getDeclaredFields()) {
					if (!RSVD_FIELDS.contains(f.getName()))
						fieldSequence.put(f, seq);
				}
				rawType = rawType.getSuperclass();
				seq -= 10;
			}

			var sj = new StringJoiner(";");
			fieldSequence.entrySet().stream().sorted(Map.Entry.comparingByValue())
					.forEachOrdered(e -> sj.add(e.getKey().getName()));
			dflt = Stream.of(new View.Literal(sj.toString()));
		}

		// remove view annots
		annots.removeIf(
				a -> View.class.isAssignableFrom(a.getClass()) || View.List.class.isAssignableFrom(a.getClass()));

		// add views
		var vl = new View.ListLiteral(Stream.concat(views.get(), dflt).toArray(View[]::new));
		annots.add(vl);
	}

	private org.jellyware.chassis.schema.Annotation parseAnnot(Annotation annot, Set<String> exclusiveMethods) {
		var annotation = new org.jellyware.chassis.schema.Annotation();
		var cls = annot.annotationType();
		annotation.setType(cls.getName());
		for (var method : cls.getDeclaredMethods())
			if (!exclusiveMethods.contains(method.getName()))
				try {
					if (Annotation.class.isAssignableFrom(method.getReturnType())) {
						annotation.addParam(method.getName(),
								parseAnnot((Annotation) method.invoke(annot), exclusiveMethods));
					} else if (method.getReturnType().isArray()) {
						if (Annotation.class.isAssignableFrom(method.getReturnType().getComponentType())) {
							annotation.addParam(method.getName(), Stream.of((Annotation[]) method.invoke(annot))
									.map(a -> parseAnnot(a, exclusiveMethods)).toArray());
						} else {
							annotation.addParam(method.getName(), method.invoke(annot));
						}
					} else {
						annotation.addParam(method.getName(), method.invoke(annot));
					}
				} catch (InvocationTargetException e) {
					throw Beef.uncheck((Exception) e.getCause());
				} catch (IllegalAccessException | IllegalArgumentException e) {
					throw Beef.of(e).as(b -> b.when("Parsing schema annotation").detail("Couldn't parse annotation")
							.may("Contact application adminstrator")).build();
				}
		return annotation;
	}

	public final Op of(Method method) {
		var schema = new Op();
		schema.setSvc(method.getDeclaringClass().getDeclaredAnnotation(org.jellyware.chassis.Op.Service.class).value());
		var rsvd = method.getDeclaredAnnotation(org.jellyware.chassis.Op.Rsvd.class);
		if (rsvd != null)
			schema.setOp(rsvd.value().toString());
		var op = method.getDeclaredAnnotation(org.jellyware.chassis.Op.class);
		if (op != null)
			schema.setOp(op.value());
		var args = method.getParameters();
		if (args.length == 1)
			schema.setParameter(of(args[0].getParameterizedType()));
		schema.setReturnType(of(method.getGenericReturnType()));
		return schema;
	}
}
