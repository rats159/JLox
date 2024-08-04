package dev.rats159.lox.constructs;

public sealed interface LoxComparable extends LoxEquateable, LoxObject permits LoxNumber {
    LoxBoolean greater(LoxComparable other);
    LoxBoolean greaterOrEqual(LoxComparable other);
    LoxBoolean less(LoxComparable other);
    LoxBoolean lessOrEqual(LoxComparable other);
}
