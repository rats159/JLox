package dev.rats159.lox.interpreting;

import dev.rats159.lox.constructs.LoxRuntimeError;
import dev.rats159.lox.lexing.Token;

import java.util.HashMap;
import java.util.Map;

public class VariableEnvironment {
   private final Map<String, Object> values = new HashMap<>();
   public final VariableEnvironment enclosing;

   public VariableEnvironment() {
      enclosing = null;
   }

   public VariableEnvironment(VariableEnvironment enclosing) {
      this.enclosing = enclosing;
   }

   public void define(String name, Object value) {
      values.put(name, value);
   }

   public Object get(Token name) {
      if (values.containsKey(name.lexeme())) {
         return values.get(name.lexeme());
      } else if (this.enclosing != null) {
         return enclosing.get(name);
      } else {
         throw new LoxRuntimeError(name, "Undefined variable '%s'".formatted(name.lexeme()));
      }
   }

   public void assign(Token name, Object value) {
      if (values.containsKey(name.lexeme())) {
         values.put(name.lexeme(), value);
      } else if (enclosing != null) {
         enclosing.assign(name, value);
      } else {
         throw new LoxRuntimeError(name, "Undefined variable '%s'".formatted(name.lexeme()));
      }
   }

   public Object getAt(int distance, String name) {
      return ancestor(distance).values.get(name);
   }

   public VariableEnvironment ancestor(int distance) {
      VariableEnvironment environment = this;

      for (int i = 0; i < distance && environment != null; i++) {
         environment = environment.enclosing;
      }

      return environment;
   }
}
