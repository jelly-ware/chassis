/**
 * 
 */
package org.jellyware.chassis.rpt;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Qualifier;

import org.jellyware.chassis.Rs;
import org.jellyware.chassis.Stash;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * @author Jotter
 *
 */
public interface Report<T extends Report.Data> {
	public static final String DATASOURCE = "datasource";
	public static final String JDBC = "jdbc";
	public static final String PATH = "rpt";

	Report<T> attachDisplay(String name);

	String display();

	Report<T> attach(JasperReport jasperReport, Map<String, Object> params);

	JasperReport report();

	T data();

	public static interface Data {
		Data attach(Map<String, Object> params);

		Map<String, Object> params();

		public interface DataSource<T extends JRDataSource> extends Data {
			DataSource<T> attach(T ds);

			T ds();

			@Override
			DataSource<T> attach(Map<String, Object> params);
		}

		public interface JDBC extends Data {
			JDBC attach(Connection conn);

			Connection conn();

			@Override
			JDBC attach(Map<String, Object> params);
		}
	}

	@Retention(RUNTIME)
	@Target({ TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR })
	@Qualifier
	public static @interface Name {
		String value();

		@SuppressWarnings("all")
		public static class Literal extends AnnotationLiteral<Name> implements Name {
			private final String name;

			public Literal(String name) {
				super();
				this.name = name;
			}

			@Override
			public String value() {
				return name;
			}

			public static Name of(String name) {
				return new Name.Literal(name);
			}
		}
	}

	public static interface Environment extends UnaryOperator<Map<String, Object>> {
	}

	public static class Compiler {
		public void compile(InputStream template, OutputStream compiled) throws JRException {
			Objects.requireNonNull(template, "template cannot be null");
			Objects.requireNonNull(compiled, "compiled cannot be null");
			JasperCompileManager.compileReportToStream(template, compiled);
		}

		public JasperReport load(InputStream compiled) throws JRException {
			Objects.requireNonNull(compiled, "compiled cannot be null");
			return (JasperReport) JRLoader.loadObject(compiled);
		}
	}

	public static class URI {
		private final Rs.Service svc;

		@Inject
		public URI(@Rs.Service.For Rs.Service svc) {
			super();
			Objects.requireNonNull(svc, "svc cannot be null");
			this.svc = svc;
		}

		public java.net.URI of(String name, Template.Streamer.Format format, Map<String, String> params, boolean att) {
			return svc.of(Optional.of(List.of(Report.PATH, name, format.getExtension())),
					(att ? Optional.of(Stash.ATT) : Optional.empty()), Optional.of(params));
		};

		public java.net.URI of(String name, Template.Streamer.Format format, Map<String, String> params) {
			return of(name, format, params, false);
		}
	}
}
