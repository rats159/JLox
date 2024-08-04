package dev.rats159.lox;

import dev.rats159.lox.interpreting.Interpreter;
import dev.rats159.lox.errors.LoxRuntimeError;
import dev.rats159.lox.lexing.Token;
import dev.rats159.lox.lexing.TokenType;
import dev.rats159.lox.lexing.Tokenizer;
import dev.rats159.lox.parsing.Parser;
import dev.rats159.lox.parsing.Statement;
import dev.rats159.lox.resolving.Resolver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();

    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("Usage: jlox [filename]");
            System.exit(1);
        } else if (args.length == 1) {
            try {
                Lox.runFile(args[0]);
            } catch (FileNotFoundException e) {
                System.err.printf("File %s could not be found.\n", args[0]);
                throw new RuntimeException(e);
            }
        } else {
            Lox.enterRepl();
        }
    }

    private static void runFile(String location) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(location));
        Lox.run(new String(bytes, Charset.defaultCharset()));

        if (Lox.hadError) {
            System.exit(-1);
        }

        if (Lox.hadRuntimeError) {
            System.exit(-1);
        }
    }

    private static void enterRepl() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            Lox.run(line);
            Lox.hadError = false;
        }
    }

    private static void run(String source) {
        Tokenizer scanner = new Tokenizer(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);

        List<Statement> statements = parser.parse();

        if (Lox.hadError) {
            return;
        }

        Resolver resolver = new Resolver(Lox.interpreter);
        resolver.resolve(statements);

        if (Lox.hadError) {
            return;
        }

        Lox.interpreter.interpret(statements);

    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.printf("[line %d] Error%s: %s\n", line, where, message);
        hadError = true;
    }

    public static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '%s'".formatted(token.lexeme()), message);
        }
    }

    public static void runtimeError(LoxRuntimeError error) {
        System.err.printf("%s\n[line %d]\n", error.getMessage(), error.token.line());
        Lox.hadRuntimeError = true;
    }
}
