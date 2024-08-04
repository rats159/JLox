package dev.rats159.lox.constructs;

import dev.rats159.lox.Lox;
import dev.rats159.lox.lexing.Token;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme(), value);
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }

        LoxFunction method = klass.findMethod(name.lexeme());
        if (method != null) return method.bind(this);

        throw new LoxRuntimeError(name,
                "Undefined property '" + name.lexeme() + "'.");
    }
}
