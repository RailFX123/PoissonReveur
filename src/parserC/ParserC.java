package parserC;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ast.*;
import lexerC.LexerC;
import lexerC.TokenC;
import lexerC.TokenTypeC;

public class ParserC {

    private LexerC lexer;
    private TokenC token;
    private TokenC errorToken;

    // hash table for operator precedence levels
    private final static Map<TokenTypeC, Integer> binopLevels;

    private ArrayList<VarDecl> decelarations; //declarations symbol table
    private ArrayList<Identifier> identifiers; //identifiers symbol table
    private ArrayList<Assign> assigns; //assigns symbol table
    private ArrayList<Exp> conditions; //conditions symbol table

    private int errors;

    static {
        binopLevels = new HashMap<TokenTypeC, Integer>();
        binopLevels.put(TokenTypeC.AND, 10);
        binopLevels.put(TokenTypeC.OR, 10);
        binopLevels.put(TokenTypeC.LT, 20);
        binopLevels.put(TokenTypeC.RT, 20);
        binopLevels.put(TokenTypeC.LT_EQ, 20);
        binopLevels.put(TokenTypeC.RT_EQ, 20);
        binopLevels.put(TokenTypeC.EQ, 20);
        binopLevels.put(TokenTypeC.NEQ, 20);
        binopLevels.put(TokenTypeC.PLUS, 30);
        binopLevels.put(TokenTypeC.MINUS, 30);
        binopLevels.put(TokenTypeC.TIMES, 40);
        binopLevels.put(TokenTypeC.DIV, 40);
        binopLevels.put(TokenTypeC.MOD, 40);
        binopLevels.put(TokenTypeC.LBRACKET, 50);
    }

    public ParserC(FileReader file) throws IOException {
        this.lexer = new LexerC(file);
        this.token = lexer.getToken();
        this.decelarations = new ArrayList<VarDecl>();
        this.identifiers = new ArrayList<Identifier>();
        this.assigns = new ArrayList<Assign>();
        this.conditions = new ArrayList<Exp>();
    }

    // verifies current token type and grabs next token or reports error
    private boolean eat(TokenTypeC type) throws IOException {
        if (token.getType() == type) {
            token = lexer.getToken();
            return true;
        } else {
            error(type);
            return false;
        }
    }

    // reports an error to the console
    private void error(TokenTypeC type) {
        // only report error once per erroneous token
        if (token == errorToken) {
            return;
        }

        // print error report
        System.err.print("ERROR: " + token.getType());
        System.err.print(" at line " + token.getLineNumber() + ", column " + token.getColumnNumber());
        System.err.println("; Expected " + type);

        errorToken = token; // set error token to prevent cascading
        errors++; // increment error counter
    }

    // skip tokens until match in follow set for error recovery
    private void skipTo(TokenTypeC... follow) throws IOException {
        while (token.getType() != TokenTypeC.EOF) {
            for (TokenTypeC skip : follow) {
                if (token.getType() == skip) {
                    return;
                }
            }
            token = lexer.getToken();
        }
    }

    // number of reported syntax errors
    public int getErrors() {
        return errors;
    }

    public ArrayList<VarDecl> getDecelarations() {
        return decelarations;
    }

    public ArrayList<Identifier> getIdentifiers() {
        return identifiers;
    }

    public ArrayList<Assign> getAssigns() {
        return assigns;
    }

    public ArrayList<Exp> getConditions() {
        return conditions;
    }

    // Program ::= int main '('')' { Declarations StatementList }
    public Program parseProgram() throws IOException {
        while (token.getType() == TokenTypeC.ASTERISC) {
            eat(TokenTypeC.ASTERISC);
            eat(TokenTypeC.INCLUDE);
            eat(TokenTypeC.LT);
            eat(TokenTypeC.ID);
            eat(TokenTypeC.PUNTO);
            eat(TokenTypeC.ID);
            eat(TokenTypeC.RT);
        }
        eat(TokenTypeC.INT);
        eat(TokenTypeC.MAIN);
        eat(TokenTypeC.LPAREN);
        eat(TokenTypeC.RPAREN);
        eat(TokenTypeC.LBRACE);

        Declarations declarations = parseDeclarations();
        StatementList statementList = parseStatementList();
        while (token.getType() == TokenTypeC.PRINTF) {
            eat(TokenTypeC.PRINTF);
            eat(TokenTypeC.LPAREN);
            eat(TokenTypeC.STRING_CONTS);
            eat(TokenTypeC.RPAREN);
            eat(TokenTypeC.SEMI);
        }
        eat(TokenTypeC.RETURN);
        eat(TokenTypeC.INT_CONST);
        eat(TokenTypeC.SEMI);
        eat(TokenTypeC.RBRACE);
        eat(TokenTypeC.EOF);

        return new Program(statementList, declarations);
    }

    // Declarations ::= { VarDeclList }
    private Declarations parseDeclarations() throws IOException {
        Declarations declarations = new Declarations();

        while (token.getType() == TokenTypeC.INT || token.getType() == TokenTypeC.FLOAT
                || token.getType() == TokenTypeC.BOOLEAN || token.getType() == TokenTypeC.CHAR) {
            declarations.addElement(parseVarDecList());
        }

        return declarations;
    }

    // VarDeclList ::= VarDecl { , Identifier };
    private VarDeclList parseVarDecList() throws IOException {
        VarDeclList varDeclList = new VarDeclList();
        VarDecl varDecl = parseVarDecl();
        varDeclList.addElement(varDecl);
        getDecelarations().add(varDecl);
        
        if(token.getType()==TokenTypeC.ASSIGN){
            parseStatement();
        }
        // check for additional varDecl
        while (token.getType() == TokenTypeC.COMMA) {
            eat(TokenTypeC.COMMA);
            VarDecl newVarDecl = new VarDecl(varDecl.getType(), parseIdentifier());
            varDeclList.addElement(newVarDecl);
            getDecelarations().add(newVarDecl);
        }
        eat(TokenTypeC.SEMI);

        return varDeclList;
    }

    // VarDecl ::= Type Identifier
    private VarDecl parseVarDecl() throws IOException {
        Type type = parseType();
        Identifier id = parseIdentifier();
        return new VarDecl(type, id);
    }

    /*
	 * Type ::= int | int '['integer']' | float | float'['integer']' | boolean | boolean'['integer']' | char | char'['integer']'
	 * int (IntegerType)
	 * int [integer] (IntArrayType)
	 * float (FloatType)
	 * float[integer] (FloatArrayType)
	 * boolean (BooleanType)
	 * boolean[integer] (BooleanArrayType)
     */
    private Type parseType() throws IOException {
        switch (token.getType()) {

            case INT:
                eat(TokenTypeC.INT);

                // check for integer array type
                if (token.getType() == TokenTypeC.LBRACKET) {
                    eat(TokenTypeC.LBRACKET);

                    // check array size integer
                    if (eat(TokenTypeC.INT_CONST)) {
                        if (token.getType() == TokenTypeC.RBRACKET) {
                            eat(TokenTypeC.RBRACKET);
                            return new IntegerArrayType();
                        }
                    }

                    // invalid integer type declaration
                    eat(TokenTypeC.TYPE);
                    return null;
                }
                return new IntegerType();

            case FLOAT:
                eat(TokenTypeC.FLOAT);

                // check for integer array type
                if (token.getType() == TokenTypeC.LBRACKET) {
                    eat(TokenTypeC.LBRACKET);

                    // check array size integer
                    if (eat(TokenTypeC.INT_CONST)) {
                        if (token.getType() == TokenTypeC.RBRACKET) {
                            eat(TokenTypeC.RBRACKET);
                            return new FloatArrayType();
                        }
                    }

                    // invalid integer type declaration
                    eat(TokenTypeC.TYPE);
                    return null;
                }
                return new FloatType();

            case BOOLEAN:
                eat(TokenTypeC.BOOLEAN);

                // check for integer array type
                if (token.getType() == TokenTypeC.LBRACKET) {
                    eat(TokenTypeC.LBRACKET);

                    // check array size integer
                    if (eat(TokenTypeC.INT_CONST)) {
                        if (token.getType() == TokenTypeC.RBRACKET) {
                            eat(TokenTypeC.RBRACKET);
                            return new BooleanArrayType();
                        }
                    }

                    // invalid integer type declaration
                    eat(TokenTypeC.TYPE);
                    return null;
                }
                return new BooleanType();

            case CHAR:
                eat(TokenTypeC.CHAR);

                // check for integer array type
                if (token.getType() == TokenTypeC.LBRACKET) {
                    eat(TokenTypeC.LBRACKET);

                    // check array size integer
                    if (eat(TokenTypeC.INT_CONST)) {
                        if (token.getType() == TokenTypeC.RBRACKET) {
                            eat(TokenTypeC.RBRACKET);
                            return new CharArrayType();
                        }
                    }

                    // invalid integer type declaration
                    eat(TokenTypeC.TYPE);
                    return null;
                }
                return new CharType();

            default:
                // unknown type
                eat(TokenTypeC.TYPE);
                return null;

        }
    }

    // Identifier ::= Letter { Letter | Digit }
    private Identifier parseIdentifier() throws IOException {
        Identifier identifier = null;

        // grab ID value if token type is ID
        if (token.getType() == TokenTypeC.ID) {
            identifier = new Identifier(token.getAttribute().getIdVal());
        }

        eat(TokenTypeC.ID);

        return identifier;
    }

    // StatementList ::= { Statement }
    private StatementList parseStatementList() throws IOException {
        StatementList statementList = new StatementList();
        while (isStatement()) {
            statementList.addElement(parseStatement());
        }
        return statementList;
    }

    // checks the beginning of a new statement 
    private boolean isStatement() {
        switch (token.getType()) {
            case SEMI:
            case IF:
            case WHILE:
            case LPAREN:
            case LBRACE:
            case ID:
                return true;
            default:
                return false;
        }
    }

    // Statement ::= Block | IfStatement | WhileStatement | identifier = Exp | identifier '['Exp']' = Exp
    private Statement parseStatement() throws IOException {

        // IfStatement ::=  if '('Exp')' Statement [else Statement]
        if (token.getType() == TokenTypeC.IF) {
            eat(TokenTypeC.IF);

            // parse conditional expression
            if (!eat(TokenTypeC.LPAREN)) {
                skipTo(TokenTypeC.RPAREN, TokenTypeC.LBRACE, TokenTypeC.RBRACE);
            }

            Exp condExp = parseExp();
            conditions.add(condExp);

            /*if(condExp instanceof IdentifierExp){
				IdentifierExp idExp = (IdentifierExp) condExp;
				Identifier identifier = new Identifier(idExp.getName());
				identifiers.add(identifier);
			}*/
            if (!eat(TokenTypeC.RPAREN)) {
                skipTo(TokenTypeC.LBRACE, TokenTypeC.SEMI, TokenTypeC.RBRACE);
            }

            // parse true and false statements
            Statement trueStm;

            // BLock ::= '{' StatementList '}' 
            if (token.getType() == TokenTypeC.LBRACE) {
                trueStm = parseBlock();
            } else // parse true statement
            {
                trueStm = parseStatement();
            }

            if (token.getType() == TokenTypeC.ELSE) {
                if (!eat(TokenTypeC.ELSE)) {
                    skipTo(TokenTypeC.LBRACE, TokenTypeC.SEMI, TokenTypeC.RBRACE);
                }

                Statement falseStm;

                // BLock ::= '{' StatementList '}' 
                if (token.getType() == TokenTypeC.LBRACE) {
                    falseStm = parseBlock();
                } else // parse false statement
                {
                    falseStm = parseStatement();
                }

                return new If(condExp, trueStm, falseStm);
            }
            return new If(condExp, trueStm, null);
        }


        // WhileStatement ::= while '('Exp')' Statement
        if (token.getType() == TokenTypeC.WHILE) {
            eat(TokenTypeC.WHILE);

            // parse looping condition
            if (!eat(TokenTypeC.LPAREN)) {
                skipTo(TokenTypeC.RPAREN, TokenTypeC.LBRACE, TokenTypeC.RBRACE);
            }

            Exp condExp = parseExp();
            conditions.add(condExp);

            /*if(condExp instanceof IdentifierExp){
				IdentifierExp idExp = (IdentifierExp) condExp;
				Identifier identifier = new Identifier(idExp.getName());
				identifiers.add(identifier);
			}*/
            if (!eat(TokenTypeC.RPAREN)) {
                skipTo(TokenTypeC.LBRACE, TokenTypeC.SEMI, TokenTypeC.RBRACE);
            }

            Statement loopStm;

            // BLock ::= '{' StatementList '}' 
            if (token.getType() == TokenTypeC.LBRACE) {
                loopStm = parseBlock();
            } else // parse looping statement
            {
                loopStm = parseStatement();
            }

            return new While(condExp, loopStm);
        }

        // Identifier statement
        if (token.getType() == TokenTypeC.ID) {

            Identifier id = new Identifier(token.getAttribute().getIdVal());
            identifiers.add(id);
            eat(TokenTypeC.ID);

            // Assignment statement: id = Exp ;
            if (token.getType() == TokenTypeC.ASSIGN) {
                eat(TokenTypeC.ASSIGN);
                Exp value = parseExp();

                /*if(value instanceof IdentifierExp){
					IdentifierExp idExp = (IdentifierExp) value;
					Identifier identifier = new Identifier(idExp.getName());
					identifiers.add(identifier);
				}*/
                eat(TokenTypeC.SEMI);

                Assign assign = new Assign(id, value);
                assigns.add(assign);
                return assign;
            }

            // Array value assignment statement: id [ Exp ] = Exp ;
            if (token.getType() == TokenTypeC.LBRACKET) {
                eat(TokenTypeC.LBRACKET);
                Exp index = parseExp();

                if (!(index instanceof IntegerLiteral)) {
                    // statement type unknown
                    eat(TokenTypeC.TYPE);
                    token = lexer.getToken();
                    return null;
                }

                if (!eat(TokenTypeC.RBRACKET)) {
                    skipTo(TokenTypeC.ASSIGN, TokenTypeC.SEMI);
                }

                if (!eat(TokenTypeC.ASSIGN)) {
                    skipTo(TokenTypeC.SEMI);
                }

                Exp value = parseExp();

                /*if(value instanceof IdentifierExp){
					IdentifierExp idExp = (IdentifierExp) value;
					Identifier identifier = new Identifier(idExp.getName());
					identifiers.add(identifier);
				}*/
                eat(TokenTypeC.SEMI);

                Assign assign = new Assign(id, value);
                assigns.add(assign);
                return new ArrayAssign(id, index, value);
            }
        }

        // statement type unknown
        eat(TokenTypeC.STATEMENT);
        token = lexer.getToken();
        return null;
    }

    // BLock ::= '{' StatementList '}'
    // Block ::= '{' StatementList '}'
    private Block parseBlock() throws IOException {
        eat(TokenTypeC.LBRACE);

        // recursively call parseStatement() until closing brace
        StatementList stms = new StatementList();
        while (token.getType() != TokenTypeC.RBRACE && token.getType() != TokenTypeC.EOF) {
            stms.addElement(parseStatement());
        }

        if (!eat(TokenTypeC.RBRACE)) {
            skipTo(TokenTypeC.RBRACE, TokenTypeC.SEMI);
        }

        return new Block(stms);
    }

    // Exp ::= PrimaryExp | BinopRHS
    // top-level parsing function for an expression
    private Exp parseExp() throws IOException {
        Exp lhs = parsePrimaryExp();
        return parseBinopRHS(0, lhs); // check for binops following exp
    }

    // parsePrimaryExp ::= INT_CONST | FLOAT_CONST | CHAR_CONST | BOOLEAN_CONST | NEGATIVE | NOT | Identifier
    // parse exp before any binop
    private Exp parsePrimaryExp() throws IOException {
        switch (token.getType()) {

            case INT_CONST:
                int intValue = token.getAttribute().getIntVal();
                eat(TokenTypeC.INT_CONST);
                return new IntegerLiteral(intValue);

            case FLOAT_CONST:
                float floatValue = token.getAttribute().getFloatVal();
                eat(TokenTypeC.FLOAT_CONST);
                return new FloatLiteral(floatValue);

            case BOOLEAN_CONST:
                boolean booleanVal = token.getAttribute().getBooleanVal();
                eat(TokenTypeC.BOOLEAN_CONST);
                return new BooleanLiteral(booleanVal);
            case STRING_CONTS:
                String strValue = token.getAttribute().getStringVal();
                eat(TokenTypeC.STRING_CONTS);
                return new StringLiteral(strValue);
            case CHAR_CONST:
                char charVal = token.getAttribute().getCharVal();
                eat(TokenTypeC.CHAR_CONST);
                return new CharLiteral(charVal);

            case ID:
                Identifier id = parseIdentifier();
                identifiers.add(id);
                return new IdentifierExp(id.getName());

            case NOT:
                eat(TokenTypeC.NOT);
                return new Not(parseExp());

            case NEGATIVE:
                eat(TokenTypeC.NEGATIVE);
                return new Negative(parseExp());

            case LPAREN:
                eat(TokenTypeC.LPAREN);
                Exp exp = parseExp();
                eat(TokenTypeC.RPAREN);
                return exp;

            default:
                // unrecognizable expression
                eat(TokenTypeC.EXPRESSION);
                token = lexer.getToken();
                return null;
        }
    }

    // parse expressions according to operator precedence levels
    private Exp parseBinopRHS(int level, Exp lhs) throws IOException {
        // continuously parse exp until a lower order operator comes up
        while (true) {
            // grab operator precedence (-1 for non-operator token)
            Integer val = binopLevels.get(token.getType());
            int tokenLevel = (val != null) ? val.intValue() : -1;

            // either op precedence is lower than prev op or token is not an op
            if (tokenLevel < level) {
                return lhs;
            }

            // save binop before parsing rhs of exp
            TokenTypeC binop = token.getType();
            eat(binop);

            Exp rhs = parsePrimaryExp(); // parse rhs of exp

            // grab operator precedence (-1 for non-operator token)
            val = binopLevels.get(token.getType());
            int nextLevel = (val != null) ? val.intValue() : -1;

            // if next op has higher precedence than prev op, make recursive call
            if (tokenLevel < nextLevel) {
                rhs = parseBinopRHS(tokenLevel + 1, rhs);
            }

            // build AST for exp
            switch (binop) {
                case AND:
                    lhs = new And(lhs, rhs);
                    break;
                case OR:
                    lhs = new Or(lhs, rhs);
                    break;
                case EQ:
                    lhs = new Equal(lhs, rhs);
                    break;
                case NEQ:
                    lhs = new NotEqual(lhs, rhs);
                    break;
                case LT:
                    lhs = new LessThan(lhs, rhs);
                    break;
                case RT:
                    lhs = new MoreThan(lhs, rhs);
                    break;
                case LT_EQ:
                    lhs = new LessThanEqual(lhs, rhs);
                    break;
                case RT_EQ:
                    lhs = new MoreThanEqual(lhs, rhs);
                    break;
                case PLUS:
                    lhs = new Plus(lhs, rhs);
                    break;
                case MINUS:
                    lhs = new Minus(lhs, rhs);
                    break;
                case TIMES:
                    lhs = new Times(lhs, rhs);
                    break;
                case DIV:
                    lhs = new Divide(lhs, rhs);
                    break;
                case MOD:
                    lhs = new Modules(lhs, rhs);
                    break;
                case LBRACKET:
                    lhs = new ArrayLookup(lhs, rhs);
                    eat(TokenTypeC.RBRACKET);
                    break;
                default:
                    eat(TokenTypeC.OPERATOR);
                    break;
            }
        }
    }

}
