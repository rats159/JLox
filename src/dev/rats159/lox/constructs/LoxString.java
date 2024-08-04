package dev.rats159.lox.constructs;

import dev.rats159.lox.errors.LoxTypeError;

public final class LoxString implements LoxAddable, LoxObject, LoxMultipliable {
    public String value;
    public LoxString(String value) {
        this.value = value;
    }

    @Override
    public String toLangString() {
        return this.value;
    }

    @Override
    public String type() {
        return "string";
    }

    @Override
    public boolean isTruthy() {
        return !this.value.isEmpty();
    }

    @Override
    public LoxAddable add(LoxAddable other) {
        return new LoxString(this.value + other.toLangString());
    }

    @Override
    public LoxMultipliable multiply(LoxMultipliable other) throws LoxTypeError {
        return switch(other){
            case LoxNumber num -> new LoxString(this.value.repeat((int) num.value));
            default -> throw new LoxTypeError("Unable to add types %s and %s".formatted(this.type(), other.type()));
        };
    }
}
