package dev.rats159.lox.constructs;

public abstract class LoxBuiltin implements LoxCallable{
    protected int arity;

    public LoxBuiltin(int arity){
        this.arity = arity;
    }

    @Override
    public String toLangString() {
        return this.name();
    }

    @Override
    public String toString() {
        return "<native %s function>".formatted(this.name());
    }

    public abstract String name();

    @Override
    public String type() {
        return "function";
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public int arity() {
        return this.arity;
    }
}
