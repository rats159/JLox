package dev.rats159.lox.lexing;

import dev.rats159.lox.Lox;
import dev.rats159.lox.constructs.LoxNumber;
import dev.rats159.lox.constructs.LoxObject;
import dev.rats159.lox.constructs.LoxString;

import java.util.*;

import static dev.rats159.lox.lexing.TokenType.*;

public class Tokenizer {
   private static final Map<String, TokenType> keywords = new HashMap<>();

   static {
      keywords.put("and", AND);
      keywords.put("class", CLASS);
      keywords.put("else", ELSE);
      keywords.put("false", FALSE);
      keywords.put("for", FOR);
      keywords.put("fun", FUN);
      keywords.put("if", IF);
      keywords.put("nil", NIL);
      keywords.put("or", OR);
      keywords.put("return", RETURN);
      keywords.put("super", SUPER);
      keywords.put("this", THIS);
      keywords.put("true", TRUE);
      keywords.put("var", VAR);
      keywords.put("while", WHILE);
   }

   private final String source;
   private final List<Token> tokens = new ArrayList<>();

   private int start = 0;
   private int current = 0;
   private int line = 1;

   public Tokenizer(String source) {
      this.source = source;
   }

   public List<Token> scanTokens() {
      while (!this.isDone()) {
         start = current;
         scanToken();
      }

      this.tokens.add(new Token(EOF, "", null, line));
      return this.tokens;
   }

   private boolean isDone() {
      return this.current >= this.source.length();
   }

   private void scanToken() {
      char c = advance();
      switch (c) {
         case '(' -> addToken(LEFT_PAREN);
         case ')' -> addToken(RIGHT_PAREN);
         case '{' -> addToken(LEFT_BRACE);
         case '}' -> addToken(RIGHT_BRACE);
         case ',' -> addToken(COMMA);
         case '.' -> addToken(DOT);
         case '-' -> addToken(MINUS);
         case '+' -> addToken(PLUS);
         case ';' -> addToken(SEMICOLON);
         case '*' -> addToken(STAR);
         case '%' -> addToken(MOD);
         case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
         case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
         case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
         case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
         case '/' -> {
            if (match('/')) {
               this.comment();
            } else if(match('*')){
               this.multilineComment();
            }else {
               addToken(SLASH);
            }
         }
         case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> this.number();
         case ' ', '\r', '\t' -> {
         }
         case '\n' -> line++;
         case '"' -> this.string();
         default -> {
            if (this.isAlpha(c)) {
               this.identifier();
            } else {
               Lox.error(this.line, "Unexpected character `%c`".formatted(c));
            }
         }
      }
   }

   private void comment(){
      while (peek() != '\n' && !this.isDone()) {
         advance();
      }
   }

   private void multilineComment(){
      while (peek() != '*' && peek(1) != '/' && !this.isDone()) {
         if (peek() == '\n') {
            this.line++;
         }
         this.advance();
      }

      if (this.isDone()) {
         Lox.error(line, "Unterminated comment.");
         return;
      }

      this.advance();
   }

   private void string() {
      while (peek() != '"' && !this.isDone()) {
         if (peek() == '\n') {
            this.line++;
         }
         this.advance();
      }

      if (this.isDone()) {
         Lox.error(line, "Unterminated string.");
         return;
      }

      this.advance();

      // Trim the surrounding quotes.
      String value = source.substring(start + 1, current - 1);
      this.addToken(STRING, new LoxString(value));
   }

   private boolean isDigit(char c) {
      return c >= '0' && c <= '9';
   }

   private char peekNext() {
      if (current + 1 >= source.length()) return '\0';
      return source.charAt(current + 1);
   }

   private void number() {
      while (isDigit(peek())) advance();

      // Look for a fractional part.
      if (peek() == '.' && isDigit(peekNext())) {
         // Consume the "."
         advance();

         while (isDigit(peek())) advance();
      }

      addToken(NUMBER, new LoxNumber(Double.parseDouble(source.substring(start, current))));
   }

   private void identifier() {
      while (isAlphaNumeric(peek())) advance();

      String text = source.substring(start, current);
      TokenType type = keywords.get(text);
      if (type == null) {
         type = IDENTIFIER;
      }
      addToken(type);
   }

   private boolean isAlpha(char c) {
      return (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') ||
        c == '_';
   }

   private boolean isAlphaNumeric(char c) {
      return isAlpha(c) || isDigit(c);
   }

   private char peek() {
      if (this.isDone()) {
         return '\0';
      }

      return source.charAt(current);
   }

   private char peek(int distance) {
      if (this.isDone()) {
         return '\0';
      }

      return source.charAt(current + distance);
   }

   private boolean match(char expected) {
      if (this.isDone()) {
         return false;
      }
      if (this.source.charAt(this.current) != expected) {
         return false;
      }

      this.current++;
      return true;
   }

   private char advance() {
      return source.charAt(current++);
   }

   private void addToken(TokenType type) {
      addToken(type, null);
   }

   private void addToken(TokenType type, LoxObject literal) {
      String text = source.substring(start, current);
      tokens.add(new Token(type, text, literal, line));
   }
}