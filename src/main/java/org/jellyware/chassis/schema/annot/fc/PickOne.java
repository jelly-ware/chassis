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
public @interface PickOne {
	boolean addNew() default false;

	@SuppressWarnings("all")
	class Literal extends AnnotationLiteral<PickOne> implements PickOne {
		private boolean addNew;
		{
			addNew = false;
		}

		public Literal(boolean addNew) {
			super();
			this.addNew = addNew;
		}

		public Literal() {
			super();
		}

		@Override
		public boolean addNew() {
			return addNew;
		}
	}
}
