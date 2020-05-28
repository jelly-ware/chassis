package org.jellyware.chassis.schema;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Op {
	private String[] svc;
	private String op;
	private Type parameter, returnType;
}
