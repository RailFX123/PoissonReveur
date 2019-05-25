package ast;

import visitor.Visitor;

public class StringLiteral implements Exp {
	private String value;
	
	public StringLiteral(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public void accept(Visitor v) {
		v.visit(this);
	}
}
