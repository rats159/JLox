package dev.rats159.lox.constructs;

import dev.rats159.lox.lexing.Token;

public class LoxRuntimeError extends RuntimeException{
   public final Token token;

   public LoxRuntimeError(Token token, String message) {
      super(message);
      this.token = token;
   }
}
