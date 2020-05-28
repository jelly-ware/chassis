/**
 * 
 */
package org.jellyware.chassis.schema.annot;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Jotter
 *
 */
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
@UI
public @interface ReadOnly {
	String[] forViews() default {};

	String[] notForViews() default {};
}
