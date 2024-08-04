package dev.rats159.lox.constructs;

import dev.rats159.lox.interpreting.Interpreter;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    public final String name;
    public final LoxClass superclass;
    private final Map<String, LoxFunction> methods;


    public LoxClass(String name,LoxClass superclass, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
        this.superclass = superclass;
    }

    @Override
    public String toString() {
        return name;
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    @Override
    public LoxObject call(Interpreter interpreter, List<LoxObject> args) {
        LoxInstance instance = new LoxInstance(this);

        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, args);
        }

        return instance;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public String toLangString() {
        return this.name;
    }

    @Override
    public String type() {
        return "class";
    }

    @Override
    public boolean isTruthy() {
        return true;
    }
}
