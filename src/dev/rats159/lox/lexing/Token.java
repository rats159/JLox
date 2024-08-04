package dev.rats159.lox.lexing;

public record Token(TokenType type, String lexeme, Object literal, int line) {
   @Override
   public boolean equals(Object obj) {
      return this == obj;
   }
}
