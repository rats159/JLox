package dev.rats159.lox.parsing;

import dev.rats159.lox.lexing.Token;

import java.util.List;

public sealed interface Expression permits Expression.Assignment, Expression.Binary, Expression.Call, Expression.Get, Expression.Grouping, Expression.Literal, Expression.Logical, Expression.Set, Expression.Super, Expression.This, Expression.Unary, Expression.Variable {
   <R> R accept(Visitor<R> visitor);



   record Binary(Expression left, Token operator, Expression right) implements Expression {
      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitBinaryExpression(this);
      }
   }

   record Grouping(Expression inner) implements Expression {
      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitGroupingExpression(this);
      }
   }

   record Literal(Object value) implements Expression {
      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitLiteralExpression(this);
      }
   }

   record Unary(Token operator, Expression right) implements Expression {
      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitUnaryExpression(this);
      }
   }

   record Variable(Token name) implements Expression{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitVariableExpression(this);
      }
   }

   record Assignment(Token name, Expression value) implements Expression{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitAssignmentExpression(this);
      }
   }

   record Logical(Expression left, Token operator, Expression right) implements Expression{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitLogicalExpression(this);
      }
   }

   record Call(Expression callee, Token paren, List<Expression> args) implements Expression{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitCallExpression(this);
      }
   }

   record Get(Expression object, Token name) implements Expression{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitGetExpression(this);
      }
   }

   record Set(Expression object, Token name, Expression value) implements Expression{
      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitSetExpression(this);
      }
   }

   record This(Token keyword) implements Expression{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitThisExpression(this);
      }
   }

   record Super(Token keyword, Token method) implements Expression{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitSuperExpression(this);
      }
   }

   interface Visitor<T> {
      T visitBinaryExpression(Binary expression);
      T visitGroupingExpression(Grouping expression);
      T visitLiteralExpression(Literal literal);
      T visitUnaryExpression(Unary unary);
      T visitVariableExpression(Variable variable);
      T visitAssignmentExpression(Assignment assignment);
      T visitLogicalExpression(Logical logicalExpression);
      T visitCallExpression(Call call);
      T visitGetExpression(Get get);
      T visitSetExpression(Set set);
      T visitThisExpression(This thisExpression);
      T visitSuperExpression(Super superExpression);
   }
}
