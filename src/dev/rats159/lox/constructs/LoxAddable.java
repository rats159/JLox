package dev.rats159.lox.constructs;

import dev.rats159.lox.errors.LoxTypeError;

public sealed interface LoxAddable extends LoxObject permits LoxBoolean, LoxNumber, LoxString {
    LoxAddable add(LoxAddable other) throws LoxTypeError;
}
