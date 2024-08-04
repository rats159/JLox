package dev.rats159.lox.constructs;

public sealed interface LoxNegatable extends LoxObject permits LoxNumber{
    LoxNegatable negate();
}
