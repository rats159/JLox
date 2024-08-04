package dev.rats159.lox.constructs;

import dev.rats159.lox.interpreting.Interpreter;
import dev.rats159.lox.interpreting.VariableEnvironment;
import dev.rats159.lox.parsing.Statement;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Statement.Function decl;
    private final VariableEnvironment closure;
    private final boolean isInitializer;

    public LoxFunction(Statement.Function decl, VariableEnvironment closure, boolean isInitializer) {
        this.isInitializer = isInitializer;
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
            if(this.isInitializer){
                return closure.getAt(0,"this");
            }
            return returnValue.value;
        }

        if (isInitializer) {
            return closure.getAt(0, "this");
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

    public LoxFunction bind(LoxInstance loxInstance) {
        VariableEnvironment environment = new VariableEnvironment(closure);
        environment.define("this", loxInstance);
        return new LoxFunction(this.decl, environment, this.isInitializer);

    }
}
