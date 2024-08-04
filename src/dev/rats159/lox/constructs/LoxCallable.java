package dev.rats159.lox.constructs;

import dev.rats159.lox.interpreting.Interpreter;

import java.util.List;

public interface LoxCallable extends LoxObject {
   LoxObject call(Interpreter interpreter, List<LoxObject> args);
   int arity();
}
