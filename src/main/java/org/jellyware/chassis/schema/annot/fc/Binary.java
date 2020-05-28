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
public @interface Binary {
	Type type() default Type.CHECKBOX;

	String on() default "Yes";

	String off() default "No";

	enum Type {
		SWITCH, CHECKBOX, CHECKBOX_BUTTON;
	}

	@SuppressWarnings("all")
	class Literal extends AnnotationLiteral<Binary> implements Binary {
		private String on, off;
		private final Type type;
		{
			on = "Yes";
			off = "No";
		}

		public Literal(Type type, String on, String off) {
			super();
			this.on = on;
			this.off = off;
			this.type = type;
		}

		public Literal(Type type) {
			super();
			this.type = type;
		}

		public Literal() {
			super();
			this.type = Type.CHECKBOX;
		}

		@Override
		public String on() {
			return on;
		}

		@Override
		public String off() {
			return off;
		}

		@Override
		public Type type() {
			return type;
		}

	}
}
