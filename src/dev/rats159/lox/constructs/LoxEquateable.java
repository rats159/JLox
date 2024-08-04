package dev.rats159.lox.constructs;

public sealed interface LoxEquateable extends LoxObject permits LoxComparable, LoxNumber {
    LoxBoolean equal(LoxEquateable other);
    LoxBoolean unequal(LoxEquateable other);
}
