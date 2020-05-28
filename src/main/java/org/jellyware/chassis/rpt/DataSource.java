package org.jellyware.chassis.rpt;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;

public class DataSource implements Report.Data.DataSource<JRDataSource> {
	private Map<String, Object> params;
	private Optional<JRDataSource> ds;
	{
		params = new HashMap<>();
		ds = Optional.empty();
	}

	@Override
	public Map<String, Object> params() {
		return params;
	}

	@Override
	public Report.Data.DataSource<JRDataSource> attach(JRDataSource ds) {
		this.ds = Optional.of(ds);
		return this;
	}

	@Override
	public JRDataSource ds() {
		return ds.orElseGet(() -> new JREmptyDataSource());
	}

	@Override
	public Report.Data.DataSource<JRDataSource> attach(Map<String, Object> params) {
		this.params.putAll(params);
		return this;
	}
}
