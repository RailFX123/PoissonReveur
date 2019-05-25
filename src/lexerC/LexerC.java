package lexerC;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lexer.Token;
import lexer.TokenType;

public class LexerC {

    private BufferedReader stream; //input stream reader
    private TokenC nextToken;
    private int nextChar;
    private int lineNumber = 1; //current line number
    private int columnNumber = 1; //current column number

    private final static Map<String, TokenTypeC> reservedWords; //reserved words dictionary
    private final static Map<Character, TokenTypeC> simbolos; //punctuation characters dictionary
    private final static Map<String, TokenTypeC> operators; //operator characters dictionary

    private int errors; //number of errors

    static {

        reservedWords = new HashMap<String, TokenTypeC>();
        reservedWords.put("int", TokenTypeC.INT);
        reservedWords.put("float", TokenTypeC.FLOAT);
        reservedWords.put("char", TokenTypeC.CHAR);
        reservedWords.put("boolean", TokenTypeC.BOOLEAN);
        reservedWords.put("#", TokenTypeC.ASTERISC);
        reservedWords.put("if", TokenTypeC.IF);
        reservedWords.put(".", TokenTypeC.PUNTO);
        reservedWords.put("else", TokenTypeC.ELSE);
        reservedWords.put("while", TokenTypeC.WHILE);
        reservedWords.put("main", TokenTypeC.MAIN);
        reservedWords.put("String", TokenTypeC.STRING);
        reservedWords.put("cout", TokenTypeC.COUT);
        reservedWords.put("cin", TokenTypeC.CIN);
        reservedWords.put("include", TokenTypeC.INCLUDE);
        reservedWords.put("return", TokenTypeC.RETURN);
        reservedWords.put("<", TokenTypeC.LMAYINC);
        reservedWords.put(">", TokenTypeC.RMAYINC);
        reservedWords.put("printf", TokenTypeC.PRINTF);

        simbolos = new HashMap<>();
        simbolos.put('(', TokenTypeC.LPAREN);
        simbolos.put(')', TokenTypeC.RPAREN);
        simbolos.put('[', TokenTypeC.LBRACKET);
        simbolos.put(']', TokenTypeC.RBRACKET);
        simbolos.put('{', TokenTypeC.LBRACE);
        simbolos.put('}', TokenTypeC.RBRACE);
        simbolos.put(';', TokenTypeC.SEMI);
        simbolos.put(',', TokenTypeC.COMMA);
        simbolos.put('=', TokenTypeC.ASSIGN);
        simbolos.put('-', TokenTypeC.NEGATIVE);
        simbolos.put('!', TokenTypeC.NOT);

        operators = new HashMap<>();
        operators.put("&&", TokenTypeC.AND);
        operators.put("||", TokenTypeC.OR);
        operators.put("==", TokenTypeC.EQ);
        operators.put("!=", TokenTypeC.NEQ);
        operators.put("<", TokenTypeC.LT);
        operators.put(">", TokenTypeC.RT);
        operators.put("<=", TokenTypeC.LT_EQ);
        operators.put(">=", TokenTypeC.RT_EQ);
        operators.put("+", TokenTypeC.PLUS);
        operators.put("-", TokenTypeC.MINUS);
        operators.put("*", TokenTypeC.TIMES);
        operators.put("/", TokenTypeC.DIV);
        operators.put("%", TokenTypeC.MOD);
    }

    public LexerC(FileReader file) throws FileNotFoundException {
        this.stream = new BufferedReader(file);
        nextChar = getChar();
    }

    public int getErrors() {
        return errors;
    }

    // handles I/O for char stream
    private int getChar() {
        try {
            return stream.read();
        } catch (IOException e) {
            System.err.print(e.getMessage());
            System.err.println("IOException occured in Lexer::getChar()");
            return -1;
        }
    }

    // detect and skip possible '\n', '\r' and '\rn' line breaks
    private boolean skipNewline() {
        if (nextChar == '\n') {
            lineNumber++;
            columnNumber = 1;
            nextChar = getChar();
            return true;
        }
        if (nextChar == '\r') {
            lineNumber++;
            columnNumber = 1;
            nextChar = getChar();

            // skip over next char if '\n'
            if (nextChar == '\n') {
                nextChar = getChar();
            }
            return true;
        }
        // newline char not found
        return false;
    }

    // return the next token without consuming it
    public TokenC peek() throws IOException {
        // advance token only if its been reset by getToken()
        if (nextToken == null) {
            nextToken = getToken();
        }

        return nextToken;
    }

    // return the next token in the input stream (EOF signals end of input)
    public TokenC getToken() throws IOException {
        // check if peek() was called
        if (nextToken != null) {
            TokenC token = nextToken;
            nextToken = null; // allow peek to call for next token
            return token;
        }

        // skip whitespace character
        while (Character.isWhitespace(nextChar)) {
            // check if whitespace char is a newline
            if (!skipNewline()) {
                columnNumber++;
                nextChar = getChar();
            }

            // offset colNum for tab chars
            if (nextChar == '\t') {
                columnNumber += 3;
            }
        }

        // identifier or reserved word ([a-zA-Z][a-zA-Z0-9_]*)
        if (Character.isLetter(nextChar)) {
            // create new idVal starting with first char of identifier
            String current = Character.toString((char) nextChar);
            columnNumber++;
            nextChar = getChar();

            // include remaining sequence of chars that are letters, digits, or _
            while (Character.isLetterOrDigit(nextChar)) {
                current += (char) nextChar;
                columnNumber++;
                nextChar = getChar();
            }

            // check if identifier is a reserved word
            TokenTypeC type = reservedWords.get(current);

            if (type != null) {
                return new TokenC(type, new TokenAttribute(), lineNumber, columnNumber - current.length());
            }

            if (current.equals("true")) {
                return new TokenC(TokenTypeC.BOOLEAN_CONST, new TokenAttribute(true), lineNumber, columnNumber - current.length());
            } else if (current.equals("false")) {
                return new TokenC(TokenTypeC.BOOLEAN_CONST, new TokenAttribute(false), lineNumber, columnNumber - current.length());
            }

            // token is an identifier
            return new TokenC(TokenTypeC.ID, new TokenAttribute(current), lineNumber, columnNumber - current.length());
        }

        if (nextChar == '\"') {
            nextChar = getChar();
            columnNumber++;
            String current = "";
            while (Character.isLetterOrDigit(nextChar) || Character.isWhitespace(nextChar)) {
                current += (char) nextChar;
                columnNumber++;
                nextChar = getChar();

            }
            if (nextChar == '\"') {
                nextChar = getChar();
                columnNumber++;
                return new TokenC(TokenTypeC.STRING_CONTS, new lexerC.TokenAttribute(current), lineNumber, columnNumber - 1);
            }
            Character.isUnicodeIdentifierPart(nextChar);

            return new TokenC(TokenTypeC.UNKNOWN, new lexerC.TokenAttribute(), lineNumber, columnNumber - 1);
        }

        // integer literal ([0-9]+) OR float literal ([0-9]+.[0-9]+)
        if (Character.isDigit(nextChar)) {

            // create string representation of number
            String numString = Character.toString((char) nextChar);
            columnNumber++;
            nextChar = getChar();

            // concatenate remaining sequence of digits
            while (Character.isDigit(nextChar)) {
                numString += (char) nextChar;
                columnNumber++;
                nextChar = getChar();
            }

            if (nextChar == '.') {
                //stream.mark(0);
                nextChar = getChar();
                columnNumber++;

                if (Character.isDigit(nextChar)) {
                    numString += '.';
                    // concatenate remaining sequence of digits
                    while (Character.isDigit(nextChar)) {
                        numString += (char) nextChar;
                        columnNumber++;
                        nextChar = getChar();
                    }

                    return new TokenC(TokenTypeC.FLOAT_CONST, new TokenAttribute(Float.parseFloat(numString)), lineNumber, columnNumber - numString.length());
                }
                while (!Character.isWhitespace(nextChar)) {
                    columnNumber++;
                    numString += nextChar;
                    nextChar = getChar();
                }

                return new TokenC(TokenTypeC.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - numString.length() + 1);
            }

            // return integer literal token
            return new TokenC(TokenTypeC.INT_CONST, new TokenAttribute(Integer.parseInt(numString)), lineNumber, columnNumber - numString.length());
        }

        if (nextChar == '\'') {
            nextChar = getChar();
            columnNumber++;
            if (Character.isAlphabetic(nextChar)) {
                char current = (char) nextChar;
                stream.mark(0);
                nextChar = getChar();
                columnNumber++;

                if (nextChar == '\'') {
                    nextChar = getChar();
                    columnNumber++;
                    return new TokenC(TokenTypeC.CHAR_CONST, new TokenAttribute(current), lineNumber, columnNumber - 1);
                }
                stream.reset();
            }

            return new TokenC(TokenTypeC.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - 1);
        }

        // EOF reached
        if (nextChar == -1) {
            return new TokenC(TokenTypeC.EOF, new TokenAttribute(), lineNumber, columnNumber);
        }

        // check for binops
        switch (nextChar) {

            case '&':
                columnNumber++;
                nextChar = getChar();

                // check if next char is '&' to match '&&' binop
                if (nextChar == '&') {
                    nextChar = getChar();
                    return new TokenC(TokenTypeC.AND, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else {
                    return new TokenC(TokenTypeC.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - 1);
                }
            case '#':
                // check if next char is '&' to match '&&' binop
                if (nextChar == '#') {
                    nextChar = getChar();
                    return new TokenC(TokenTypeC.ASTERISC, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else {
                    return new TokenC(TokenTypeC.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - 1);
                }

            case '|':
                columnNumber++;
                nextChar = getChar();

                // check if next char is '|' to match '||' binop
                if (nextChar == '|') {
                    nextChar = getChar();
                    return new TokenC(TokenTypeC.OR, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else {
                    return new TokenC(TokenTypeC.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - 1);
                }

            case '=':
                columnNumber++;
                nextChar = getChar();

                // check if next char is '=' to match '==' binop
                if (nextChar == '=') {
                    nextChar = getChar();
                    return new TokenC(TokenTypeC.EQ, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else {
                    return new TokenC(TokenTypeC.ASSIGN, new TokenAttribute(), lineNumber, columnNumber - 1);
                }

            case '!':
                columnNumber++;
                nextChar = getChar();

                // check if next char is '!' to match '!=' binop
                if (nextChar == '=') {
                    nextChar = getChar();
                    return new TokenC(TokenTypeC.NEQ, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else {
                    return new TokenC(TokenTypeC.NOT, new TokenAttribute(), lineNumber, columnNumber - 1);
                }

            case '<':
                columnNumber++;
                nextChar = getChar();

                // check if next char is '<' to match '<=' binop
                if (nextChar == '=') {
                    nextChar = getChar();
                    return new TokenC(TokenTypeC.LT_EQ, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else {
                    return new TokenC(TokenTypeC.LT, new TokenAttribute(), lineNumber, columnNumber - 1);
                }

            case '>':
                columnNumber++;
                nextChar = getChar();

                // check if next char is '<' to match '<=' binop
                if (nextChar == '=') {
                    nextChar = getChar();
                    return new TokenC(TokenTypeC.RT_EQ, new TokenAttribute(), lineNumber, columnNumber - 2);
                } else {
                    return new TokenC(TokenTypeC.RT, new TokenAttribute(), lineNumber, columnNumber - 1);
                }

            case '+':
                columnNumber++;
                nextChar = getChar();
                return new TokenC(TokenTypeC.PLUS, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '-':
                columnNumber++;
                nextChar = getChar();
                return new TokenC(TokenTypeC.MINUS, new TokenAttribute(), lineNumber, columnNumber - 1);
            case '.':
                columnNumber++;
                nextChar = getChar();
                return new TokenC(TokenTypeC.PUNTO, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '*':
                columnNumber++;
                nextChar = getChar();
                return new TokenC(TokenTypeC.TIMES, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '/':
                columnNumber++;
                nextChar = getChar();
                return new TokenC(TokenTypeC.DIV, new TokenAttribute(), lineNumber, columnNumber - 1);

            case '%':
                columnNumber++;
                nextChar = getChar();
                return new TokenC(TokenTypeC.MOD, new TokenAttribute(), lineNumber, columnNumber - 1);
        }

        // check for punctuation
        TokenTypeC type = simbolos.get((char) nextChar);
        columnNumber++;
        nextChar = getChar();

        // found punctuation token
        if (type != null) {
            return new TokenC(type, new TokenAttribute(), lineNumber, columnNumber - 1);
        }

        // token type is unknown
        return new TokenC(TokenTypeC.UNKNOWN, new TokenAttribute(), lineNumber, columnNumber - 1);
    }
}
