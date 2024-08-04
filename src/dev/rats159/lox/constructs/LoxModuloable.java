package dev.rats159.lox.constructs;

public sealed interface LoxModuloable extends LoxObject permits LoxNumber {
    LoxModuloable mod(LoxModuloable other);
}
