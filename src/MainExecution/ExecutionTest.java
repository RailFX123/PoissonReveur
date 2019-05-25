/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainExecution;

import ast.Program;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import lexerC.TokenC;
import lexerC.TokenTypeC;
import parser.Parser;
import parserC.ParserC;
import semantic.SemanticAnalyzer;
import semanticC.SemanticAnalyzerC;
import visitor.PrintVisitor;

/**
 *
 * @author david
 */
public class ExecutionTest {

    public String iniciar(String ok) {
        String analisis = "";

        if (ok.contains(".java")) {
            analisis = analizadorLexico(ok);
            analizadorSintactico(ok);
            analisis = analisis + "\n" + analizadorSemantico(ok);
        } else if (ok.contains(".c")) {
            analisis = lexicoC(ok);
            sintacticoC(ok);
            analisis = analisis + "\n" + semanticoC(ok);
        }

        return analisis;
    }

    private String analizadorLexico(String files) {
        String tks = "";
        try {
            FileReader file = null;
            try {
                file = new FileReader(files);
            } catch (FileNotFoundException e) {
                System.err.println(files + " was not found!");
            }

            // create lexer
            Lexer lexer = new Lexer(file);

            // start tokenizing file
            System.out.println("Tokenizing " + files + "...");
            long startTime = System.currentTimeMillis();
            int numTokens = 0;
            Token token;
            do {
                token = lexer.getToken();
                numTokens++;

                if (token.getType() == TokenType.UNKNOWN) {
                    // print token type and location
                    System.err.print(token.getType());
                    tks += token.getType() + "";
                    System.err.print(" (" + token.getLineNumber() + "," + token.getColumnNumber() + ")");
                    tks += "\n" + " (" + token.getLineNumber() + "," + token.getColumnNumber() + ")";
                    System.out.println();
                    continue;
                }

                System.out.print(token.getType());
                tks += token.getType();
                System.out.print(" (" + token.getLineNumber() + "," + token.getColumnNumber() + ")");
                tks += " (" + token.getLineNumber() + "," + token.getColumnNumber() + ")";

                // print out semantic values for ID and INT_CONST tokens
                if (token.getType() == TokenType.ID) {
                    System.out.println(": " + token.getAttribute().getIdVal());
                    tks += ": " + token.getAttribute().getIdVal() + "\n\n";
                } else if (token.getType() == TokenType.INT_CONST) {
                    System.out.println(": " + token.getAttribute().getIntVal());
                    tks += ": " + token.getAttribute().getIntVal() + "\n\n";
                } else if (token.getType() == TokenType.FLOAT_CONST) {
                    System.out.println(": " + token.getAttribute().getFloatVal());
                    tks += ": " + token.getAttribute().getFloatVal() + "\n\n";
                } else if (token.getType() == TokenType.CHAR_CONST) {
                    System.out.println(": " + token.getAttribute().getCharVal());
                    tks += ": " + token.getAttribute().getStringVal() + "\n\n";
                } else if (token.getType() == TokenType.STRING_CONTS) {
                    System.out.println(": " + token.getAttribute().getIdVal());
                    tks += ": " + token.getAttribute().getIdVal() + "\n\n";
                } else if (token.getType() == TokenType.BOOLEAN_CONST) {
                    System.out.println(": " + token.getAttribute().getBooleanVal());
                    tks += ": " + token.getAttribute().getBooleanVal() + "\n\n";
                } else {
                    System.out.println();
                }

            } while (token.getType() != TokenType.EOF);

            long endTime = System.currentTimeMillis();

            // print out statistics
            System.out.println("---");
            System.out.println("Number of tokens: " + numTokens);
            System.out.println("Execution time: " + (endTime - startTime) + "ms");
            System.out.println();
            return tks + "--- \n " + "Number of tokens: " + numTokens + "\n"
                    + "Execution time: " + (endTime - startTime) + "ms";
        } catch (IOException e) {
            System.err.println("Error lexico: " + e.getMessage());
            return "Error lexico: " + e.getMessage();
        }

    }

    private void analizadorSintactico(String files) {
        try {
            FileReader file = null;
            try {
                file = new FileReader(files);
            } catch (FileNotFoundException e) {
                System.err.println(files + " was not found!");
            }
            Parser parser = new Parser(file);
            System.out.println("Analisis sintactico " + files + "...");

            // initiate parse and clock time
            long startTime = System.currentTimeMillis();
            Program program = parser.parseProgram();
            long endTime = System.currentTimeMillis();

            // print out statistics
            System.out.println("File has finished parsing!");
            System.out.println("Execution time: " + (endTime - startTime) + "ms");
            System.out.println(parser.getErrors() + " errors reported");
            System.out.println("---");

            // print out ASTs
            PrintVisitor printer = new PrintVisitor();
            printer.visit(program);
            System.out.println();
        } catch (IOException e) {
            System.err.println("Error Analizador Sintactico: " + e.getMessage());
        }
    }

    private String analizadorSemantico(String files) {
        String tks = "\n";
        try {
            FileReader file = null;
            try {
                file = new FileReader(files);
            } catch (FileNotFoundException e) {
                System.err.println(files + " was not found!");
            }
            SemanticAnalyzer semantic = new SemanticAnalyzer(file);
            System.out.println("Analyzing " + files + "...");
            tks += "Analisis " + files;
            // initiate parse and clock time
            long startTime = System.currentTimeMillis();
            semantic.analyzeProgram();
            long endTime = System.currentTimeMillis();

            // print out statistics
            System.out.println("File has finished analyzing!");
            System.out.println("Execution time: " + (endTime - startTime) + "ms");
            System.out.println(semantic.getErrors() + " errors reported");
            System.out.println("---");
            return tks + "--- \n " + "File has finished analyzing!" + "\n"
                    + "Execution time: " + (endTime - startTime) + "ms"
                    + semantic.getErrors() + " errors reported";
        } catch (IOException e) {
            System.err.println("Error Analizador Sintactico: " + e.getMessage());
            return "Error Analizador Sintactico: " + e.getMessage();
        }
    }

    /*
    Parte del Código que lee C
     */
    private String lexicoC(String files) {
        String tks = "";
        try {
            FileReader file = null;
            try {
                file = new FileReader(files);
            } catch (FileNotFoundException e) {
                System.err.println(files + " was not found!");
            }

            // Create lexer
            lexerC.LexerC lexico = new lexerC.LexerC(file);

            // start tokenizing file
            System.out.println("Tokenizing " + files + "...");
            long startTime = System.currentTimeMillis();
            int numTokens = 0;
            TokenC token;
            do {
                token = lexico.getToken();
                numTokens++;

                if (token.getType() == TokenTypeC.UNKNOWN) {
                    // print token type and location
                    System.err.print(token.getType());
                    tks += token.getType() + "";
                    System.err.print(" (" + token.getLineNumber() + "," + token.getColumnNumber() + ")");
                    tks += "\n" + " (" + token.getLineNumber() + "," + token.getColumnNumber() + ")";
                    System.out.println();
                    continue;
                }

                System.out.print(token.getType());
                tks += token.getType();
                System.out.print(" (" + token.getLineNumber() + "," + token.getColumnNumber() + ")");
                tks += " (" + token.getLineNumber() + "," + token.getColumnNumber() + ")";

                // print out semantic values for ID and INT_CONST tokens
                if (token.getType() == TokenTypeC.ID) {
                    System.out.println(": " + token.getAttribute().getIdVal());
                    tks += ": " + token.getAttribute().getIdVal() + "\n\n";
                } else if (token.getType() == TokenTypeC.INT_CONST) {
                    System.out.println(": " + token.getAttribute().getIntVal());
                    tks += ": " + token.getAttribute().getIntVal() + "\n\n";
                } else if (token.getType() == TokenTypeC.FLOAT_CONST) {
                    System.out.println(": " + token.getAttribute().getFloatVal());
                    tks += ": " + token.getAttribute().getFloatVal() + "\n\n";
                } else if (token.getType() == TokenTypeC.STRING_CONTS) {
                    System.out.println(": " + token.getAttribute().getStringVal());
                    tks += ": " + token.getAttribute().getFloatVal() + "\n\n";
                } else if (token.getType() == TokenTypeC.CHAR_CONST) {
                    System.out.println(": " + token.getAttribute().getCharVal());
                    tks += ": " + token.getAttribute().getCharVal() + "\n\n";
                } else if (token.getType() == TokenTypeC.BOOLEAN_CONST) {
                    System.out.println(": " + token.getAttribute().getBooleanVal());
                    tks += ": " + token.getAttribute().getBooleanVal() + "\n\n";
                } else {
                    System.out.println();
                }
            } while (token.getType() != TokenTypeC.EOF);

            long endTime = System.currentTimeMillis();

            // print out statistics
            System.out.println("---");
            System.out.println("Number of tokens: " + numTokens);
            System.out.println("Execution time: " + (endTime - startTime) + "ms");
            System.out.println();
            return tks + "--- \n " + "Number of tokens: " + numTokens + "\n"
                    + "Execution time: " + (endTime - startTime) + "ms";
        } catch (IOException e) {
            System.err.println("Error lexico: " + e.getMessage());
            return "Error lexico: " + e.getMessage();
        }
    } // End fucion lexicoC

    private void sintacticoC(String files) {
        try {
            FileReader file = null;

            try {
                file = new FileReader(files);
            } catch (FileNotFoundException e) {
                System.err.println(files + " was not found!");
            }

            ParserC parser = new ParserC(file);
            System.out.println("Analisis sintactico " + files + "...");

            // initiate parse and clock time
            long startTime = System.currentTimeMillis();
            Program program = parser.parseProgram();
            long endTime = System.currentTimeMillis();

            // print out statistics
            System.out.println("File has finished parsing!");
            System.out.println("Execution time: " + (endTime - startTime) + "ms");
            System.out.println(parser.getErrors() + " errors reported");
            System.out.println("---");

            // print out ASTs
            PrintVisitor printer = new PrintVisitor();
            printer.visit(program);
            System.out.println();
        } catch (IOException e) {
            System.err.println("Error Analizador Sintactico: " + e.getMessage());
        }
    }// End sintacticoC

    private String semanticoC(String files) {
        String tks = "\n";
        try {
            FileReader file = null;
            try {
                file = new FileReader(files);
            } catch (FileNotFoundException e) {
                System.err.println(files + " was not found!");
            }

            SemanticAnalyzerC semantic = new SemanticAnalyzerC(file);
            System.out.println("Analyzing " + files + "...");
            tks += "Analisis " + files;
            // initiate parse and clock time
            long startTime = System.currentTimeMillis();
            semantic.analyzeProgram();
            long endTime = System.currentTimeMillis();

            // print out statistics
            System.out.println("File has finished analyzing!");
            System.out.println("Execution time: " + (endTime - startTime) + "ms");
            System.out.println(semantic.getErrors() + " errors reported");
            System.out.println("---");
            return tks + "--- \n " + "File has finished analyzing!" + "\n"
                    + "Execution time: " + (endTime - startTime) + "ms"
                    + semantic.getErrors() + " errors reported";
        } catch (IOException e) {
            System.err.println("Error Analizador Sintactico: " + e.getMessage());
            return "Error Analizador Sintactico: " + e.getMessage();
        }
    }
}
