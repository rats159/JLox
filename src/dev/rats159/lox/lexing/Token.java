package dev.rats159.lox.lexing;

public record Token(TokenType type, String lexeme, Object literal, int line) {
   @Override
   public String toString() {
      if(this.literal == null){
         return "%s %s %s".formatted(type.toString(), lexeme, "null");
      }
      return "%s %s %s".formatted(type.toString(), lexeme, literal.toString());
   }
}
