package dev.rats159.lox.interpreting;

import java.util.List;

public interface LoxCallable {
   Object call(Interpreter interpreter, List<Object> args);
   int arity();
}
