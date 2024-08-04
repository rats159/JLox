package dev.rats159.lox.constructs;

public sealed interface LoxInvertable extends LoxObject permits LoxBoolean  {
    LoxInvertable invert();
}
