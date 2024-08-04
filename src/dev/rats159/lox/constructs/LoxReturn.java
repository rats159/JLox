package dev.rats159.lox.constructs;

public class LoxReturn extends RuntimeException{
   public final LoxObject value;

   public LoxReturn(LoxObject value){
      super(null,null,false,false);
      this.value = value;
   }
}
