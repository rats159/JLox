package dev.rats159.lox.constructs;

import dev.rats159.lox.errors.LoxTypeError;

public final class LoxBoolean implements LoxObject, LoxAddable, LoxMultipliable, LoxInvertable {
    public boolean value;

    public LoxBoolean(boolean value){
        this.value = value;
    }

    @Override
    public LoxAddable add(LoxAddable other) throws LoxTypeError{
        return switch(other){
            case LoxBoolean loxBoolean -> new LoxBoolean(this.value || loxBoolean.value);
            case LoxString loxString -> new LoxString(this.toLangString() + loxString.value);
            default -> throw new LoxTypeError("Unable to add types %s and %s".formatted(this.type(), other.type()));
        };
    }

    @Override
    public LoxMultipliable multiply(LoxMultipliable other) throws LoxTypeError {
        return switch(other){
            case LoxBoolean loxBoolean -> new LoxBoolean(this.value && loxBoolean.value);
            default -> throw new LoxTypeError("Unable to add types %s and %s".formatted(this.type(), other.type()));
        };
    }

    @Override
    public String toLangString() {
        return String.valueOf(this.value);
    }

    @Override
    public String type() {
        return "boolean";
    }

    @Override
    public boolean isTruthy() {
        return this.value;
    }

    @Override
    public LoxInvertable invert() {
        return new LoxBoolean(!this.value);
    }
}
