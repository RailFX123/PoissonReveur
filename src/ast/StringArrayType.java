package ast;

import visitor.Visitor;

public class StringArrayType implements Type {

    public void accept(Visitor v) {
        v.visit(this);
    }
}
