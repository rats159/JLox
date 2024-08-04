package dev.rats159.lox.constructs;

public sealed interface LoxDivisible extends LoxObject permits LoxNumber {
    LoxDivisible divide(LoxDivisible other);
}
