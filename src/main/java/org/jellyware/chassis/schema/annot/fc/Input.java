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
public @interface Input {
	Type type() default Type.TEXT;

	int maxLength() default 255;

	boolean password() default false;

	double min() default Double.MIN_NORMAL;

	double max() default Double.MAX_VALUE;

	int scale() default 0;

	float step() default 0.01F;

	boolean compact() default true;

	enum Type {
		TEXT, TEXTAREA, NUMBER;
	}

	@SuppressWarnings("all")
	class Literal extends AnnotationLiteral<Input> implements Input {
		private final Type type;
		private int maxLength, scale;
		private double min, max;
		private float step;
		private boolean password, compact;
		{
			maxLength = 255;
			password = false;
			min = Double.MIN_NORMAL;
			max = Double.MAX_VALUE;
			scale = 2;
			step = 0.01F;
			compact = true;
		}

		public Literal(int maxLength) {
			super();
			this.type = Type.TEXT;
			this.maxLength = maxLength;
		}

		public Literal(int scale, double min, double max, float step, boolean compact) {
			super();
			this.type = Type.NUMBER;
			this.min = min;
			this.max = max;
			this.step = step;
			this.scale = scale;
			this.compact = compact;
		}

		public Literal(Type type) {
			super();
			this.type = type;
		}

		@Override
		public Type type() {
			return type;
		}

		@Override
		public int maxLength() {
			return maxLength;
		}

		@Override
		public double min() {
			return min;
		}

		@Override
		public double max() {
			return max;
		}

		@Override
		public float step() {
			return step;
		}

		@Override
		public int scale() {
			return scale;
		}

		@Override
		public boolean password() {
			return password;
		}

		@Override
		public boolean compact() {
			return compact;
		}
	}
}
