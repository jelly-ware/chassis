package org.jellyware.chassis.rpt;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class JDBC implements Report.Data.JDBC {
	private Map<String, Object> params;
	private Connection conn;
	{
		params = new HashMap<>();
	}

	@Override
	public Map<String, Object> params() {
		return params;
	}

	@Override
	public Report.Data.JDBC attach(Connection conn) {
		this.conn = conn;
		return this;
	}

	@Override
	public Connection conn() {
		return conn;
	}

	@Override
	public Report.Data.JDBC attach(Map<String, Object> params) {
		this.params.putAll(params);
		return this;
	}
}
