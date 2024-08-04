package dev.rats159.lox.constructs;

import dev.rats159.lox.interpreting.Interpreter;

import java.util.List;

public interface LoxCallable {
   Object call(Interpreter interpreter, List<Object> args);
   int arity();
}
