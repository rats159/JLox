package dev.rats159.lox.constructs;

public class LoxNil implements LoxObject{
    @Override
    public String toLangString() {
        return "nil";
    }

    @Override
    public String type() {
        return "nil";
    }

    @Override
    public boolean isTruthy() {
        return false;
    }
}
