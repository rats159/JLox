package dev.rats159.lox.lexing;

import dev.rats159.lox.constructs.LoxObject;

public record Token(TokenType type, String lexeme, LoxObject literal, int line) {
   @Override
   public boolean equals(Object obj) {
      return this == obj;
   }
}
