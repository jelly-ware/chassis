package org.jellyware.chassis.schema;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Request {
	private String[] svc;
	private String op, mdl;
}
