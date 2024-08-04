package dev.rats159.lox.constructs;

import dev.rats159.lox.errors.LoxTypeError;

public final class LoxNumber implements LoxAddable, LoxObject, LoxSubtractable, LoxDivisible, LoxMultipliable, LoxModuloable, LoxComparable, LoxNegatable, LoxEquateable {
    public double value;

    public LoxNumber(double v) {
        this.value = v;
    }

    @Override
    public LoxAddable add(LoxAddable other) throws LoxTypeError {
        return switch (other) {
            case LoxNumber num -> new LoxNumber(this.value + num.value);
            case LoxString str -> new LoxString(this.toLangString() + str.value);
            default -> throw new LoxTypeError("Unable to add types %s and %s".formatted(this.type(), other.type()));
        };
    }

    @Override
    public String toString() {
        return "LoxNumber<%f>".formatted(this.value);
    }

    @Override
    public String toLangString() {
        return String.valueOf(this.value);
    }

    @Override
    public String type() {
        return "number";
    }

    @Override
    public boolean isTruthy() {
        return this.value != 0;
    }

    @Override
    public LoxSubtractable subtract(LoxSubtractable other) {
        return switch (other) {
            case LoxNumber num -> new LoxNumber(this.value - num.value);
        };
    }

    @Override
    public LoxDivisible divide(LoxDivisible other) {
        return switch (other) {
            case LoxNumber num -> new LoxNumber(this.value / num.value);
        };
    }

    @Override
    public LoxMultipliable multiply(LoxMultipliable other) throws LoxTypeError {
        return switch (other) {
            case LoxNumber num -> new LoxNumber(this.value * num.value);
            case LoxBoolean bool -> new LoxNumber(bool.value? this.value : 0);
            case LoxString loxString -> loxString.multiply(this);
        };
    }

    @Override
    public LoxModuloable mod(LoxModuloable other) {
        return switch (other) {
            case LoxNumber num -> new LoxNumber(this.value % num.value);
        };
    }

    @Override
    public LoxBoolean greater(LoxComparable other) {
        return switch (other) {
            case LoxNumber num -> new LoxBoolean(this.value > num.value);
        };
    }

    @Override
    public LoxBoolean greaterOrEqual(LoxComparable other) {
        return switch (other) {
            case LoxNumber num -> new LoxBoolean(this.value >= num.value);
        };
    }

    @Override
    public LoxBoolean less(LoxComparable other) {
        return switch (other) {
            case LoxNumber num -> new LoxBoolean(this.value < num.value);
        };
    }

    @Override
    public LoxBoolean lessOrEqual(LoxComparable other) {
        return switch (other) {
            case LoxNumber num -> new LoxBoolean(this.value <= num.value);
        };
    }

    @Override
    public LoxBoolean equal(LoxEquateable other) {
        return switch (other) {
            case LoxNumber num -> new LoxBoolean(this.value == num.value);
        };
    }

    @Override
    public LoxBoolean unequal(LoxEquateable other) {
        return new LoxBoolean(!(this.equal(other).value));
    }

    @Override
    public LoxNegatable negate() {
        return new LoxNumber(-this.value);
    }
}
