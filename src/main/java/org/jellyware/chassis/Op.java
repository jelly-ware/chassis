/**
 * 
 */
package org.jellyware.chassis;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import javax.enterprise.util.AnnotationLiteral;
import javax.json.JsonValue;
import javax.validation.ConstraintViolation;

import org.jellyware.trinity.Query;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Jotter
 *
 */
@Retention(RUNTIME)
@Target({ METHOD })
public @interface Op {
	String value();

	boolean secured()

	default true;

	@Retention(RUNTIME)
	@Target({ METHOD })
	public static @interface Rsvd {
		Operation value();

		public static enum Operation {
			SIGN_IN(false), OTP(false), TK(false), RST_PWD(true), SIGN_OUT(true), SCHEMA(false), PERSIST(true),
			DELETE(true), FIND(true), SELECT(true), ENUM(true), INVOKESTATE(true), VALIDATE(true);

			private boolean secured;

			Operation(boolean secured) {
				this.secured = secured;
			}

			public boolean isSecured() {
				return secured;
			}

			public static String available() {
				var sj = new StringJoiner(", ", "[", "]");
				for (var val : values())
					sj.add(val.toString());
				return sj.toString();
			}

			public final static Optional<Operation> of(String string) {
				Optional<Operation> rtn = Optional.empty();
				try {
					rtn = Optional.of(valueOf(string.trim().replace('-', '_').toUpperCase()));
				} catch (Exception e) {
				}
				return rtn;
			}

			@Getter
			@Setter
			public static class Find {
				private String mdl;
				private Long id;
				private String codeName, status;
				private Set<Query.Parameters.Predicate> where = new HashSet<>();

				@Getter
				@Setter
				public static class Select extends Find {
					private int pageNumber;
					private Integer pageSize;
					private List<Query.Parameters.Pagination.Sorting> orderBy = new ArrayList<>();
				}
			}

			@Getter
			@Setter
			public static class Entity {
				private String mdl, status;
				private JsonValue entity;
				private Long id;
			}

			@Getter
			@Setter
			public static class Violation {
				private String message, propertyPath, rootBeanClass, constraint;

				public static Violation of(ConstraintViolation<?> cv) {
					var v = new Violation();
					v.setMessage(cv.getMessage());
					v.setPropertyPath(cv.getPropertyPath().toString());
					v.setRootBeanClass(cv.getRootBeanClass().getName());
					v.setConstraint(cv.getConstraintDescriptor().getAnnotation().annotationType().getName());
					return v;
				}
			}
		}
	}

	@Retention(RUNTIME)
	@Target({ TYPE })
	public static @interface Service {
		String[] value() default {};

		@SuppressWarnings("all")
		public final static class Literal extends AnnotationLiteral<Service> implements Service {
			public static final Literal INSTANCE = new Literal("");
			private static final long serialVersionUID = 1L;
			private final String[] value;

			public Literal(String... value) {
				super();
				this.value = value;
			}

			@Override
			public String[] value() {
				return this.value;
			}
		}
	}
}
