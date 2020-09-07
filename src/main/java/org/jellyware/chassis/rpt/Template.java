package org.jellyware.chassis.rpt;

import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.jellyware.beef.Beef;
import org.jellyware.chassis.Op;
import org.jellyware.chassis.Service;
import org.jellyware.chassis.rpt.Report.Data;
import org.jellyware.chassis.schema.Request;
import org.jellyware.toolkit.Reflect;

// import com.low

import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JsonExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleJsonExporterOutput;
import net.sf.jasperreports.export.SimpleOdsReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleTextReportConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

public class Template {
	public static final String DESIGN = "jrxml";
	public static final String COMPILED = "jasper";
	private final Instance<Report<? extends Report.Data>> reports;
	private final Instance<Report.Environment> env;

	@Inject
	public Template(@Any Instance<Report<? extends Data>> reports, @Any Instance<Report.Environment> env) {
		super();
		this.reports = reports;
		this.env = env;
	}

	public Printer of(JasperReport jr) {
		var i = reports.select(Report.Name.Literal.of(jr.getProperty(Template.Property.REPORT)));
		if (!i.isResolvable())
			throw Beef.validation().as(
					b -> b.when("Finding Report").detail("Non found for " + jr.getProperty(Template.Property.REPORT)))
					.build();

		return params -> new Streamer() {
			private Report<?> rpt = i.get().attach(jr, env.isResolvable() ? env.get().apply(params) : params);

			@Override
			public String display() {
				return rpt.display();
			}

			@SuppressWarnings("all")
			@Override
			public JasperPrint print() {
				try {
					if (Report.Data.DataSource.class.isAssignableFrom(i.get().data().getClass()))
						return JasperFillManager.fillReport(jr, rpt.data().params(),
								((Report.Data.DataSource) rpt.data()).ds());
					if (Report.Data.JDBC.class.isAssignableFrom(i.get().data().getClass()))
						return JasperFillManager.fillReport(jr, rpt.data().params(),
								((Report.Data.JDBC) rpt.data()).conn());
				} catch (JRException e) {
					throw Beef.uncheck(e);
				}
				throw Beef.internal().as(b -> b.when("Filling report").title("Invalid report implementation")).build();
			}
		};
	}

	@FunctionalInterface
	public static interface Printer {
		Streamer fill(Map<String, Object> params);
	}

	public static interface Streamer {
		String display();

		JasperPrint print();

		default void pdf(OutputStream outputStream) throws JRException, Exception {
			JasperExportManager.exportReportToPdfStream(print(), outputStream);
		}

		default void xlsx(OutputStream outputStream) throws JRException, Exception {
			JRXlsxExporter exporter = new JRXlsxExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
			SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
			configuration.setOnePagePerSheet(true);
			exporter.setConfiguration(configuration);

			exporter.exportReport();
		}

		default void pptx(OutputStream outputStream) throws JRException, Exception {
			JRPptxExporter exporter = new JRPptxExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

			exporter.exportReport();
		}

		default void docx(OutputStream outputStream) throws JRException, Exception {
			JRDocxExporter exporter = new JRDocxExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

			exporter.exportReport();
		}

		default void ods(OutputStream outputStream) throws JRException, Exception {
			JROdsExporter exporter = new JROdsExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
			SimpleOdsReportConfiguration configuration = new SimpleOdsReportConfiguration();
			configuration.setOnePagePerSheet(true);
			exporter.setConfiguration(configuration);

			exporter.exportReport();
		}

		default void odt(OutputStream outputStream) throws JRException, Exception {
			JROdtExporter exporter = new JROdtExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

			exporter.exportReport();
		}

		default void csv(OutputStream outputStream) throws JRException, Exception {
			JRCsvExporter exporter = new JRCsvExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

			exporter.exportReport();
		}

		default void xls(OutputStream outputStream) throws JRException, Exception {
			JRXlsExporter exporter = new JRXlsExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
			SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
			configuration.setOnePagePerSheet(true);
			exporter.setConfiguration(configuration);

			exporter.exportReport();
		}

		default void rtf(OutputStream outputStream) throws JRException, Exception {
			JRRtfExporter exporter = new JRRtfExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

			exporter.exportReport();
		}

		default void html(OutputStream outputStream) throws JRException, Exception {
			HtmlExporter exporter = new HtmlExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));

			exporter.exportReport();
		}

		default void xml(OutputStream outputStream) throws JRException, Exception {
			JasperExportManager.exportReportToXmlStream(print(), outputStream);
		}

		default void json(OutputStream outputStream) throws JRException, Exception {
			JsonExporter exporter = new JsonExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleJsonExporterOutput(outputStream));

			exporter.exportReport();
		}

		default void text(OutputStream outputStream) throws JRException, Exception {
			JRTextExporter exporter = new JRTextExporter();

			exporter.setExporterInput(new SimpleExporterInput(print()));
			exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
			SimpleTextReportConfiguration configuration = new SimpleTextReportConfiguration();
			configuration.setCharWidth(10f);
			configuration.setCharHeight(20f);
			exporter.setConfiguration(configuration);

			exporter.exportReport();
		}

		default void stream(Format format, OutputStream outputStream) throws JRException, Exception {
			switch (format) {
				case TXT:
					text(outputStream);
					break;
				case JSON:
					json(outputStream);
					break;
				case XML:
					xml(outputStream);
					break;
				case HTML:
					html(outputStream);
					break;
				case RTF:
					rtf(outputStream);
					break;
				case CSV:
					csv(outputStream);
					break;
				case ODT:
					odt(outputStream);
					break;
				case ODS:
					ods(outputStream);
					break;
				case DOCX:
					docx(outputStream);
					break;
				case PPTX:
					pptx(outputStream);
					break;
				case PDF:
					pdf(outputStream);
					break;
				case XLSX:
					xlsx(outputStream);
					break;
				case XLS:
					xls(outputStream);
					break;
			}
		}

		@Getter
		public static enum Format {

			TXT("text/plain", "txt"), JSON("application/json", "json"), XML("application/xml", "xml"),
			HTML("text/html", "html"), RTF("application/rtf", "rtf"), CSV("text/csv", "csv"),
			ODT("application/vnd.oasis.opendocument.text", "odt"),
			ODS("application/vnd.oasis.opendocument.spreadsheet", "ods"),
			DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
			PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"),
			PDF("application/pdf", "pdf"),
			XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
			XLS("application/vnd.ms-excel", "xls");

			String mimeType, extension;

			private Format(String mimiType, String extension) {
				this.mimeType = mimiType;
				this.extension = extension;
			}

			public static Optional<Format> of(String fmt) {
				Optional<Format> rtn = Optional.empty();
				try {
					rtn = Optional.of(valueOf(fmt.toUpperCase()));
				} catch (Exception e) {
				}
				return rtn;
			}
		}
	}

	@Getter
	@Setter
	public static class Property {
		public static final String REPORT = "org.jellyware.chassis.rpt.report";
		public static final String DISPLAY = "org.jellyware.chassis.rpt.display";
		public static final String PREFIX_PARAM = "org.jellyware.chassis.rpt.param";
		public static final String DS = "org.jellyware.chassis.rpt.ds";
		private String query, op;
		private String[] svc;

		public static interface Reader extends BiFunction<Property, Map<String, Object>, Optional<Object>> {
			public static class Impl implements Template.Property.Reader {
				private final Service svc;

				@Inject
				public Impl(Service svc) {
					super();
					this.svc = svc;
				}

				@Override
				public Optional<Object> apply(Template.Property prop, Map<String, Object> params) {
					var rsp = Optional.empty();
					var operation = prop.getOp();
					var rsvd = Op.Rsvd.Operation.of(operation);
					try {
						if (rsvd.map(i -> i == Op.Rsvd.Operation.SCHEMA).orElse(false))
							rsp = Optional.ofNullable(svc.schema(
									svc.parse((String) params.getOrDefault(prop.getQuery(), ""), Request.class)));
						else {
							var m = svc.op(operation, prop.getSvc())
									.orElseThrow(() -> Beef.internal()
											.as(b -> b.when("Servicing Request")
													.title('[' + operation + "] not implemented")
													.detail("No implementation of [" + operation + "] is available."))
											.build());
							rsp = Optional
									.ofNullable(
											Reflect.method(m)
													.execute(CDI.current()
															.select(m.getDeclaringClass(), Any.Literal.INSTANCE).get(),
															m.getParameterCount() == 1
																	? new Object[] { svc.parse(
																			(String) params
																					.getOrDefault(prop.getQuery(), ""),
																			m.getParameters()[0]
																					.getParameterizedType()) }
																	: new Object[] {}));
						}
					} catch (Exception e) {
						throw Beef.uncheck(e);
					}
					return rsp;
				}
			}
		}
	}
}
