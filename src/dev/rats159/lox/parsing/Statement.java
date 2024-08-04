package dev.rats159.lox.parsing;

import dev.rats159.lox.lexing.Token;

import javax.swing.plaf.nimbus.State;
import java.util.List;

public sealed interface Statement permits Statement.Block, Statement.Class, Statement.ExpressionStatement, Statement.Function, Statement.If, Statement.Return, Statement.VariableStatement, Statement.While {
   interface Visitor<T> {
      T visitExpressionStatement(ExpressionStatement statement);

      T visitVariableStatement(VariableStatement statement);

      T visitBlockStatement(Block block);

      T visitIfStatement(If ifStatement);

      T visitWhileStatement(While whileStatement);

      T visitFunction(Function function);

      T visitReturnStatement(Return returnStatement);

      T visitClassStatement(Class classStatement);
   }

   <R> R accept(Visitor<R> visitor);

   record ExpressionStatement(Expression expression) implements Statement {
      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitExpressionStatement(this);
      }
   }

   record VariableStatement(Token name, Expression initializer) implements Statement {

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitVariableStatement(this);
      }
   }

   record Block(List<Statement> statements) implements Statement{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitBlockStatement(this);
      }
   }

   record If(Expression condition, Statement thenBranch, Statement elseBranch) implements Statement{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitIfStatement(this);
      }
   }

   record While(Expression condition, Statement body) implements Statement{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitWhileStatement(this);
      }
   }

   record Function(Token name, List<Token> parameters, List<Statement> body) implements Statement{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitFunction(this);
      }
   }

   record Return(Token keyword, Expression value) implements Statement {

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitReturnStatement(this);
      }
   }

   record Class(Token name, Expression.Variable superclass, List<Statement.Function> methods) implements Statement{

      @Override
      public <R> R accept(Visitor<R> visitor) {
         return visitor.visitClassStatement(this);
      }
   }

}
