package dev.rats159.lox.constructs;

public interface LoxObject {
    String toLangString();
    String type();

    boolean isTruthy();
}
