package dev.rats159.lox.errors;

public class LoxTypeError extends Exception{
    public LoxTypeError(String message){
        super(message);
    }
}
