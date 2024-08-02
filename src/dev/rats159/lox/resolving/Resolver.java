package dev.rats159.lox.resolving;

import dev.rats159.lox.Lox;
import dev.rats159.lox.interpreting.Interpreter;
import dev.rats159.lox.lexing.Token;
import dev.rats159.lox.parsing.Expression;
import dev.rats159.lox.parsing.Statement;

import java.util.*;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
   private final Interpreter interpreter;
   private final Stack<Map<String,Boolean>> scopes = new Stack<>();
   private FunctionType currentFunction = FunctionType.NONE;

   private enum FunctionType {
      NONE,
      FUNCTION
   }

   public Resolver(Interpreter interpreter) {
      this.interpreter = interpreter;
   }

   @Override
   public Void visitBinaryExpression(Expression.Binary expression) {
      resolve(expression.left());
      resolve(expression.right());
      return null;
   }

   @Override
   public Void visitGroupingExpression(Expression.Grouping expression) {
      resolve(expression.inner());
      return null;
   }

   @Override
   public Void visitLiteralExpression(Expression.Literal literal) {
      return null; //Literals don't need resolving
   }

   @Override
   public Void visitUnaryExpression(Expression.Unary unary) {
      resolve(unary.right());
      return null;
   }

   @Override
   public Void visitVariableExpression(Expression.Variable var) {
      if (!scopes.isEmpty() &&
        scopes.peek().get(var.name().lexeme()) == Boolean.FALSE) {
         Lox.error(var.name(),
           "Can't read local variable in its own initializer.");
      }

      resolveLocal(var, var.name());
      return null;
   }

   @Override
   public Void visitAssignmentExpression(Expression.Assignment assignment) {
      resolve(assignment.value());
      resolveLocal(assignment, assignment.name());
      return null;
   }

   @Override
   public Void visitLogicalExpression(Expression.Logical logicalExpression) {
      resolve(logicalExpression.left());
      resolve(logicalExpression.right());
      return null;
   }

   @Override
   public Void visitCallExpression(Expression.Call call) {
      resolve(call.callee());

      for(Expression arg : call.args()){
         resolve(arg);
      }

      return null;
   }






   @Override
   public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
      resolve(statement.expression());
      return null;
   }

   @Override
   public Void visitPrintStatement(Statement.PrintStatement statement) {
      resolve(statement.expression());
      return null;
   }

   @Override
   public Void visitVariableStatement(Statement.VariableStatement statement) {
      declare(statement.name());
      if(statement.initializer() != null){
         resolve(statement.initializer());
      }
      define(statement.name());
      return null;
   }

   @Override
   public Void visitBlockStatement(Statement.Block block) {
      beginScope();
      resolve(block.statements());
      endScope();
      return null;
   }

   @Override
   public Void visitIfStatement(Statement.If ifStatement) {
      resolve(ifStatement.condition());
      resolve(ifStatement.thenBranch());
      if(ifStatement.elseBranch() != null) {
         resolve(ifStatement.elseBranch());
      }
      return null;
   }

   @Override
   public Void visitWhileStatement(Statement.While whileStatement) {
      resolve(whileStatement.condition());
      resolve(whileStatement.body());
      return null;
   }

   @Override
   public Void visitFunction(Statement.Function stmt) {
      declare(stmt.name());
      define(stmt.name());

      resolveFunction(stmt, FunctionType.FUNCTION);
      return null;
   }

   @Override
   public Void visitReturnStatement(Statement.Return returnStatement) {

      if (currentFunction == FunctionType.NONE){
         Lox.error(returnStatement.keyword(), "Cannot return from top-level code.");
      }

      if(returnStatement.value() != null){
         resolve(returnStatement.value());
      }
      return null;
   }







   public void resolve(List<Statement> statements) {
      for (Statement statement : statements) {
         resolve(statement);
      }
   }

   private void resolve(Statement stmt) {
      stmt.accept(this);
   }

   private void resolve(Expression expr) {
      expr.accept(this);
   }

   private void beginScope(){
      this.scopes.push(new HashMap<>());
   }

   private void endScope(){
      this.scopes.pop();
   }

   private void declare(Token name){
      if(scopes.isEmpty()){
         return; // We're in global scope
      }

      var scope = scopes.peek();
      if (scope.containsKey(name.lexeme())) {
         Lox.error(name,
           "Already a variable with this name in this scope.");
      }

      scope.put(name.lexeme(),false);
   }

   private void define(Token name){
      if(scopes.isEmpty()){
         return; // We're in global scope
      }

      scopes.peek().put(name.lexeme(),true);
   }

   private void resolveLocal(Expression expr, Token name) {
      for (int i = scopes.size() - 1; i >= 0; i--) {
         if (scopes.get(i).containsKey(name.lexeme())) {
            this.interpreter.resolve(expr, scopes.size() - 1 - i);
            return;
         }
      }
   }

   private void resolveFunction(Statement.Function function, FunctionType type) {
      FunctionType enclosingFunction = currentFunction;

      currentFunction = type;

      beginScope();
      for (Token param : function.parameters()) {
         declare(param);
         define(param);
      }
      resolve(function.body());
      endScope();
      currentFunction = enclosingFunction;
   }
}
