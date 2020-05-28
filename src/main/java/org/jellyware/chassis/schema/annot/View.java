/**
 * 
 */
package org.jellyware.chassis.schema.annot;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;

/**
 * @author Jotter
 *
 */
@Target({ ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@UI
public @interface View {
	public static final String DEFAULT = "$default";

	String extendsView() default "";

	String members() default "";

	String name() default DEFAULT;

	@Retention(RUNTIME)
	@Target({ ElementType.TYPE })
	@UI
	@interface List {
		View[] value();
	}

	@SuppressWarnings("all")
	class Literal extends AnnotationLiteral<View> implements View {
		private final String extendsView, members, name;
		{
			extendsView = "";
		}

		public Literal(String name, String members) {
			super();
			this.name = name;
			this.members = members;
		}

		public Literal(String members) {
			super();
			this.name = DEFAULT;
			this.members = members;
		}

		@Override
		public String extendsView() {
			return extendsView;
		}

		@Override
		public String members() {
			return members;
		}

		@Override
		public String name() {
			return name;
		}
	}

	@SuppressWarnings("all")
	class ListLiteral extends AnnotationLiteral<List> implements List {
		private final View[] views;

		public ListLiteral(View... views) {
			super();
			this.views = views;
		}

		@Override
		public View[] value() {
			return views;
		}
	}
}
