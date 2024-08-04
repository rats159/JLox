package dev.rats159.lox.constructs;

import dev.rats159.lox.errors.LoxTypeError;

public sealed interface LoxMultipliable extends LoxObject permits LoxBoolean, LoxNumber, LoxString {
    LoxMultipliable multiply(LoxMultipliable other) throws LoxTypeError;
}
