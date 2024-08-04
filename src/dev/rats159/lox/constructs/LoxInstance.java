package dev.rats159.lox.constructs;

import dev.rats159.lox.errors.LoxRuntimeError;
import dev.rats159.lox.lexing.Token;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance implements LoxObject{
    private final LoxClass klass;
    private final Map<String, LoxObject> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }

    public void set(Token name, LoxObject value) {
        fields.put(name.lexeme(), value);
    }

    public LoxObject get(Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }

        LoxFunction method = klass.findMethod(name.lexeme());
        if (method != null) return method.bind(this);

        throw new LoxRuntimeError(name,
                "Undefined property '" + name.lexeme() + "'.");
    }

    @Override
    public String toLangString() {
        return this.toString();
    }

    @Override
    public String type() {
        return "instance";
    }

    @Override
    public boolean isTruthy() {
        return true;
    }
}
