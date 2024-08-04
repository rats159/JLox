package dev.rats159.lox.interpreting;

import dev.rats159.lox.constructs.LoxObject;
import dev.rats159.lox.errors.LoxRuntimeError;
import dev.rats159.lox.lexing.Token;

import java.util.HashMap;
import java.util.Map;

public class VariableEnvironment {
   private final Map<String, LoxObject> values = new HashMap<>();
   public final VariableEnvironment enclosing;

   public VariableEnvironment() {
      enclosing = null;
   }

   public VariableEnvironment(VariableEnvironment enclosing) {
      this.enclosing = enclosing;
   }

   public void define(String name, LoxObject value) {
      values.put(name, value);
   }

   public LoxObject get(Token name) {
      if (values.containsKey(name.lexeme())) {
         return values.get(name.lexeme());
      } else if (this.enclosing != null) {
         return enclosing.get(name);
      } else {
         throw new LoxRuntimeError(name, "Undefined variable '%s'".formatted(name.lexeme()));
      }
   }

   public void assign(Token name, LoxObject value) {
      if (values.containsKey(name.lexeme())) {
         values.put(name.lexeme(), value);
      } else if (enclosing != null) {
         enclosing.assign(name, value);
      } else {
         throw new LoxRuntimeError(name, "Undefined variable '%s'".formatted(name.lexeme()));
      }
   }

   public LoxObject getAt(int distance, String name) {
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
