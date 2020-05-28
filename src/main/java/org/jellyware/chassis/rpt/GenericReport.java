package org.jellyware.chassis.rpt;

import java.sql.Connection;
import java.util.Map;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.persistence.EntityManager;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperReport;

public abstract class GenericReport<T extends Report.Data> implements Report<T> {
	private final Template.Property.Reader reader;
	private final Jsonb jsonb;
	protected String display;
	protected JasperReport jasperReport;
	protected T data;

	public GenericReport(Template.Property.Reader reader, Jsonb jsonb) {
		super();
		this.reader = reader;
		this.jsonb = jsonb;
	}

	@Override
	public Report<T> attachDisplay(String name) {
		this.display = name;
		return this;
	}

	@Override
	public String display() {
		return display;
	}

	@Override
	public Report<T> attach(JasperReport jasperReport, Map<String, Object> params) {
		// compiled report
		this.jasperReport = jasperReport;
		for (var name : this.jasperReport.getPropertiesMap().getPropertyNames()) {
			// display
			if (name.equals(Template.Property.DISPLAY))
				this.display = this.jasperReport.getProperty(name);
			// parameters
			if (name.startsWith(Template.Property.PREFIX_PARAM)) {
				reader.apply(jsonb.fromJson(jasperReport.getProperty(name), Template.Property.class), params)
						.ifPresent(param -> {
							params.put(name.substring(Template.Property.PREFIX_PARAM.length() + 1), param);
						});
			}
			// data source
			if (name.equals(Template.Property.DS))
				reader.apply(jsonb.fromJson(jasperReport.getProperty(name), Template.Property.class), params)
						.ifPresent(param -> {
							resolveDataSource(param);
						});
		}
		data.attach(params);
		return this;
	}

	protected abstract void resolveDataSource(Object object);

	@Override
	public JasperReport report() {
		return jasperReport;
	}

	@Override
	public T data() {
		return data;
	}

	@Report.Name(Report.DATASOURCE)
	public static class DataSource extends GenericReport<org.jellyware.chassis.rpt.DataSource> {
		@Inject
		public DataSource(Template.Property.Reader reader, Jsonb jsonb, org.jellyware.chassis.rpt.DataSource data) {
			super(reader, jsonb);
			this.data = data;
		}

		@Override
		protected void resolveDataSource(Object object) {
			if (JRDataSource.class.isAssignableFrom(object.getClass()))
				data.attach((JRDataSource) object);
		}
	}

	@Report.Name(Report.JDBC)
	public class JDBC extends GenericReport<org.jellyware.chassis.rpt.JDBC> {
		@Inject
		public JDBC(Template.Property.Reader reader, Jsonb jsonb, org.jellyware.chassis.rpt.JDBC data,
				EntityManager em) {
			super(reader, jsonb);
			this.data = data;
			em.getTransaction().begin();
			this.data.attach(em.unwrap(Connection.class));
			em.getTransaction().commit();
		}

		@Override
		protected void resolveDataSource(Object object) {
		}
	}
}
