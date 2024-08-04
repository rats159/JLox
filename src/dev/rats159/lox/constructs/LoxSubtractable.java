package dev.rats159.lox.constructs;

public sealed interface LoxSubtractable extends LoxObject permits LoxNumber {
    LoxSubtractable subtract(LoxSubtractable other);
}
