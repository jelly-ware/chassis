/**
 * 
 */
package org.jellyware.chassis;

import static org.reflections8.ReflectionUtils.getMethods;
import static org.reflections8.ReflectionUtils.withAnnotation;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.bind.Jsonb;
import javax.validation.Validator;
import javax.ws.rs.core.Response;

import org.jellyware.beef.Beef;
import org.jellyware.chassis.schema.Parser;
import org.jellyware.chassis.schema.Request;
import org.jellyware.toolkit.Json;
import org.jellyware.toolkit.Reflect;
import org.reflections8.Reflections;

/**
 * @author Jotter
 *
 */
public class Service {
	protected Validator valid;
	protected Parser parser;
	private Jsonb jsonb;
	private static Set<Class<?>> SERVICES;

	static {
		SERVICES = new Reflections("org").getTypesAnnotatedWith(Op.Service.class);
	}

	@Inject
	public Service(Validator valid, Parser parser, Jsonb jsonb) {
		Objects.requireNonNull(valid, "valid cannot be null");
		Objects.requireNonNull(parser, "parser cannot be null");
		Objects.requireNonNull(jsonb, "jsonb cannot be null");
		this.valid = valid;
		this.parser = parser;
		this.jsonb = jsonb;
	}

	@SuppressWarnings("all")
	public <T> T parse(String x, Class<T> type) {
		Type t = type;
		return (T) parse(x, t);
	}

	public Object parse(String x, Type type) {
		Object req = Json.parse(x, type);
		var vltns = valid.validate(req);
		if (!vltns.isEmpty())
			throw Beef.validation()
					.as(b -> b.detail(
							vltns.stream().map(Op.Rsvd.Operation.Violation::of).collect(Collectors.toSet()).toString()))
					.build();
		return req;
	}

	public Object schema(Request req) {
		if ((req.getMdl() == null) == (req.getOp() == null))
			throw Beef
					.validation().as(b -> b.when("Processing schema request")
							.detail("op and mdl have values or are null").may("Provide a value for either op or mdl"))
					.build();

		if (req.getOp() != null)
			return parser
					.of(this.op(req.getOp(), req.getSvc()).orElseThrow(() -> Beef.of(NotFoundException.class).build()));

		try {
			var mdl = getClass().getClassLoader().loadClass(req.getMdl());
			return parser.of(mdl);
		} catch (ClassNotFoundException e) {
			throw Beef.uncheck(e);
		}
	}

	@SuppressWarnings("all")
	public Optional<Method> op(String op, String... svc) {
		for (var cls : SERVICES) {
			if (Arrays.equals(svc, cls.getDeclaredAnnotation(Op.Service.class).value())) {
				var method = getMethods(cls, withAnnotation(Op.class).or(withAnnotation(Op.Rsvd.class))).stream()
						.filter(m -> {
							var rsvd = m.getDeclaredAnnotation(Op.Rsvd.class);
							if (rsvd != null)
								return Op.Rsvd.Operation.of(op).map(i -> rsvd.value() == i).orElse(false);
							var o = m.getDeclaredAnnotation(Op.class);
							if (o != null)
								return o.value().equals(op);
							return false;
						}).findAny();
				if (method.isPresent())
					return method;
			}
		}
		return Optional.empty();
	}

	public Response execute(Method m, String x) {
		try {
			var rsp = Reflect.method(m).execute(CDI.current().select(m.getDeclaringClass(), Any.Literal.INSTANCE).get(),
					m.getParameterCount() == 1 ? new Object[] { parse(x, m.getParameters()[0].getParameterizedType()) }
							: new Object[] {});
			return Response.ok(rsp != null ? jsonb.toJson(rsp) : JsonObject.NULL.toString()).build();
		} catch (Exception e) {
			return Beef.wrap(e).response().build();
		}
	}
}
