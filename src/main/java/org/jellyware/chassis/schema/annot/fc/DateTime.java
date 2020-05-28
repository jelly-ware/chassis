/**
 * 
 */
package org.jellyware.chassis.schema.annot.fc;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;

import org.jellyware.chassis.schema.annot.UI;

/**
 * @author Jotter
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@UI
public @interface DateTime {
	Type type() default Type.DATE;

	enum Type {
		DATE, TIME, CLOCK
	}

	@SuppressWarnings("all")
	class Literal extends AnnotationLiteral<DateTime> implements DateTime {
		public final Type type;

		public Literal(Type type) {
			super();
			this.type = type;
		}

		@Override
		public Type type() {
			return type;
		}
	}
}
