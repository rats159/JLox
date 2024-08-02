package dev.rats159.lox.interpreting;

import dev.rats159.lox.parsing.Statement;

import java.util.List;

public class LoxFunction implements LoxCallable {
   private final Statement.Function decl;
   private final VariableEnvironment closure;

   public LoxFunction(Statement.Function decl, VariableEnvironment closure) {
      this.decl = decl;
      this.closure = closure;
   }

   @Override
   public Object call(Interpreter interpreter, List<Object> arguments) {
      VariableEnvironment environment = new VariableEnvironment(this.closure);
      for (int i = 0; i < this.decl.parameters().size(); i++) {
         environment.define(this.decl.parameters().get(i).lexeme(), arguments.get(i));
      }

      try {
         interpreter.executeBlock(this.decl.body(), environment);
      } catch (Return returnValue) { // ... weird...
         return returnValue.value;
      }

      return null;
   }

   @Override
   public int arity() {
      return this.decl.parameters().size();
   }

   @Override
   public String toString() {
      return "<fn %s>".formatted(this.decl.name());
   }
}
