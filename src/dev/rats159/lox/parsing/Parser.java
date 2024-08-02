package dev.rats159.lox.parsing;

import dev.rats159.lox.Lox;
import dev.rats159.lox.lexing.Token;
import dev.rats159.lox.lexing.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.rats159.lox.lexing.TokenType.*;

public class Parser {
   private static class ParseError extends RuntimeException {
   }

   private final List<Token> tokens;
   private int current = 0;

   public Parser(List<Token> tokens) {
      this.tokens = tokens;
   }

   public List<Statement> parse() {
      List<Statement> statements = new ArrayList<>();
      while (!this.isAtEnd()) {
         statements.add(declaration());
      }

      return statements;
   }

   private Statement declaration() {
      try {
         if (match(FUN)) return function("function");
         if (match(VAR)) return varDeclaration();

         return statement();
      } catch (ParseError error) {
         synchronize();
         return null;
      }
   }

   private Statement.Function function(String kind) {
      Token name = consume(IDENTIFIER, "Expect " + kind + " name.");

      consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
      List<Token> parameters = new ArrayList<>();
      if (!check(RIGHT_PAREN)) {
         do {
            if (parameters.size() >= 255) {
               error(peek(), "Can't have more than 255 parameters.");
            }

            parameters.add(
              consume(IDENTIFIER, "Expect parameter name."));
         } while (match(COMMA));
      }
      consume(RIGHT_PAREN, "Expect ')' after parameters.");

      consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
      List<Statement> body = block();
      return new Statement.Function(name, parameters, body);
   }

   private Statement varDeclaration() {
      Token name = consume(IDENTIFIER, "Expect variable name.");

      Expression initializer = null;
      if (match(EQUAL)) {
         initializer = expression();
      }

      consume(SEMICOLON, "Expect ';' after variable declaration.");
      return new Statement.VariableStatement(name, initializer);
   }

   private Statement statement() {
      if (match(FOR)) {
         return forStatement();
      }
      if (match(IF)) {
         return ifStatement();
      }
      if (match(PRINT)) {
         return printStatement();
      }
      if (match(RETURN)) {
         return returnStatement();
      }
      if (match(WHILE)) {
         return whileStatement();
      }
      if (match(LEFT_BRACE)) {
         return new Statement.Block(block());
      }

      return expressionStatement();
   }

   private Statement returnStatement() {
      Token keyword = previous();
      Expression value = null;
      if (!check(SEMICOLON)) {
         value = expression();
      }

      consume(SEMICOLON, "Expect ';' after return value.");
      return new Statement.Return(keyword, value);
   }

   private Statement forStatement(){
      consume(LEFT_PAREN,"Expect '(' after 'for'");

      Statement initializer;
      if (match(SEMICOLON)) {
         initializer = null;
      } else if (match(VAR)) {
         initializer = varDeclaration();
      } else {
         initializer = expressionStatement();
      }

      Expression condition = null;
      if (!check(SEMICOLON)) {
         condition = expression();
      }
      consume(SEMICOLON, "Expect ';' after loop condition.");

      Expression increment = null;
      if (!check(RIGHT_PAREN)) {
         increment = expression();
      }
      consume(RIGHT_PAREN, "Expect ')' after for clauses.");

      Statement body = statement();

      if (increment != null) {
         body = new Statement.Block(
           Arrays.asList(
             body,
             new Statement.ExpressionStatement(increment)
           )
         );
      }

      if (condition == null) condition = new Expression.Literal(true);
      body = new Statement.While(condition, body);

      if (initializer != null) {
         body = new Statement.Block(Arrays.asList(initializer, body));
      }

      return body;
   }

   private Statement ifStatement() {
      consume(LEFT_PAREN, "Expect '(' after 'if'.");
      Expression condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after if condition.");

      Statement thenBranch = statement();
      Statement elseBranch = null;
      if (match(ELSE)) {
         elseBranch = statement();
      }

      return new Statement.If(condition, thenBranch, elseBranch);
   }

   private List<Statement> block() {
      List<Statement> statements = new ArrayList<>();

      while (!check(RIGHT_BRACE) && !isAtEnd()) {
         statements.add(declaration());
      }

      consume(RIGHT_BRACE, "Expect '}' after block.");
      return statements;
   }

   private Statement whileStatement() {
      consume(LEFT_PAREN, "Expect '(' after 'while'.");
      Expression condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after condition.");
      Statement body = statement();

      return new Statement.While(condition, body);
   }

   private Statement printStatement() {
      Expression value = expression();
      consume(SEMICOLON, "Expect ';' after value.");
      return new Statement.PrintStatement(value);
   }

   private Statement expressionStatement() {
      Expression expr = expression();
      consume(SEMICOLON, "Expect ';' after expression.");
      return new Statement.ExpressionStatement(expr);
   }

   private Expression expression() {
      return assignment();
   }

   private Expression logicalOr() {
      Expression expr = logicalAnd();

      while (match(OR)) {
         Token operator = previous();
         Expression right = logicalAnd();
         expr = new Expression.Logical(expr, operator, right);
      }

      return expr;
   }

   private Expression logicalAnd() {
      Expression expr = equality();

      while (match(AND)) {
         Token operator = previous();
         Expression right = equality();
         expr = new Expression.Logical(expr, operator, right);
      }

      return expr;
   }

   private Expression assignment() {
      Expression expr = logicalOr();

      if (match(EQUAL)) {
         Token equals = previous();
         Expression value = assignment();

         if (expr instanceof Expression.Variable var) {
            Token name = var.name();
            return new Expression.Assignment(name, value);
         }

         error(equals, "Invalid assignment target.");
      }

      return expr;
   }

   private Expression equality() {
      Expression expr = comparison();

      while (this.match(BANG_EQUAL, EQUAL_EQUAL)) {
         Token operator = previous();
         Expression right = comparison();
         expr = new Expression.Binary(expr, operator, right);
      }

      return expr;
   }

   private Expression comparison() {
      Expression expr = term();

      while (this.match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
         Token operator = previous();
         Expression right = term();
         expr = new Expression.Binary(expr, operator, right);
      }

      return expr;
   }

   private Expression term() {
      Expression expr = factor();

      while (match(MINUS, PLUS)) {
         Token operator = previous();
         Expression right = factor();
         expr = new Expression.Binary(expr, operator, right);
      }

      return expr;
   }

   private Expression factor() {
      Expression expr = unary();

      while (match(SLASH, STAR, MOD)) {
         Token operator = previous();
         Expression right = unary();
         expr = new Expression.Binary(expr, operator, right);
      }

      return expr;
   }

   private Expression unary() {
      if (match(BANG, MINUS)) {
         Token operator = previous();
         Expression right = unary();
         return new Expression.Unary(operator, right);
      }

      return call();
   }

   private Expression call() {
      Expression expr = primary();

      while (true) {
         if (match(LEFT_PAREN)) {
            expr = finishCall(expr);
         } else {
            break;
         }
      }

      return expr;
   }

   private Expression finishCall(Expression callee) {
      List<Expression> arguments = new ArrayList<>();
      if (!check(RIGHT_PAREN)) {
         do {
            if(arguments.size() >= 255){
               error(peek(), "Functions can't have more then 255 arguments");
            }
            arguments.add(expression());
         } while (match(COMMA));
      }

      Token paren = consume(RIGHT_PAREN,
        "Expect ')' after arguments.");

      return new Expression.Call(callee, paren, arguments);
   }

   private Expression primary() {
      if (match(FALSE)) return new Expression.Literal(false);
      if (match(TRUE)) return new Expression.Literal(true);
      if (match(NIL)) return new Expression.Literal(null);

      if (match(NUMBER, STRING)) {
         return new Expression.Literal(previous().literal());
      }

      if (match(IDENTIFIER)) {
         return new Expression.Variable(previous());
      }

      if (match(LEFT_PAREN)) {
         Expression expr = expression();
         consume(RIGHT_PAREN, "Expect ')' after expression.");
         return new Expression.Grouping(expr);
      }

      throw error(peek(), "Expect expression.");
   }


   private Token consume(TokenType type, String message) {
      if (check(type)) return advance();

      throw error(peek(), message);
   }

   private ParseError error(Token token, String message) {
      Lox.error(token, message);
      return new ParseError();
   }

   private void synchronize() {
      advance();

      while (!isAtEnd()) {
         if (previous().type() == SEMICOLON) return;

         switch (peek().type()) {
            case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
               return;
            }
         }

         advance();
      }
   }

   private boolean match(TokenType... types) {
      for (TokenType type : types) {
         if (check(type)) {
            advance();
            return true;
         }
      }

      return false;
   }

   private boolean check(TokenType type) {
      if (isAtEnd()) return false;
      return peek().type() == type;
   }

   private Token advance() {
      if (!isAtEnd()) current++;
      return previous();
   }

   private boolean isAtEnd() {
      return peek().type() == EOF;
   }

   private Token peek() {
      return tokens.get(current);
   }

   private Token previous() {
      return tokens.get(current - 1);
   }
}
