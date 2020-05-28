/**
 * 
 */
package org.jellyware.chassis.schema.annot;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;

/**
 * @author Jotter
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@UI
public @interface FormControl {
	Size size() default Size.DEFAULT;

	String icon() default "";

	String iconPack() default "";

	String placeholder() default "";

	boolean expanded() default false;

	boolean rounded() default false;

	String defaultValue() default "";

	enum Size {
		DEFAULT, SMALL, MEDIUM, LARGE
	}

	@SuppressWarnings("all")
	class Literal extends AnnotationLiteral<FormControl> implements FormControl {
		private final String placeholder, defaultValue;
		private String icon, iconPack;
		private Size size;
		private boolean expanded, rounded;
		{
			expanded = rounded = false;
			icon = iconPack = "";
			size = Size.DEFAULT;
		}

		public Literal(String placeholder, String defaultValue, String icon, String iconPack) {
			super();
			this.icon = icon;
			this.iconPack = iconPack;
			this.placeholder = placeholder;
			this.defaultValue = defaultValue;
		}

		public Literal(String placeholder, String defaultValue) {
			super();
			this.placeholder = placeholder;
			this.defaultValue = defaultValue;
		}

		@Override
		public Size size() {
			return size;
		}

		@Override
		public String icon() {
			return icon;
		}

		@Override
		public String iconPack() {
			return iconPack;
		}

		@Override
		public String placeholder() {
			return placeholder;
		}

		@Override
		public boolean expanded() {
			return expanded;
		}

		@Override
		public boolean rounded() {
			return rounded;
		}

		@Override
		public String defaultValue() {
			return defaultValue;
		}
	}
}
