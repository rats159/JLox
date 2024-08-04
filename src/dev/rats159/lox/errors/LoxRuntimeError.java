package dev.rats159.lox.errors;

import dev.rats159.lox.lexing.Token;

/**
 * The base class for all lox errors. This is the only error that should actually be thrown. All other errors should be caught and transformed
*/
public class LoxRuntimeError extends RuntimeException{
   public final Token token;

   public LoxRuntimeError(Token token, String message) {
      super(message);
      this.token = token;
   }
}
