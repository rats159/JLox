package dev.rats159.lox.interpreting;

import dev.rats159.lox.Lox;
import dev.rats159.lox.constructs.*;
import dev.rats159.lox.lexing.Token;
import dev.rats159.lox.lexing.TokenType;
import dev.rats159.lox.parsing.Expression;
import dev.rats159.lox.parsing.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    public final VariableEnvironment globals = new VariableEnvironment();
    private VariableEnvironment environment = globals;
    private final Map<Expression, Integer> locals = new HashMap<>();

    public Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return (double) System.currentTimeMillis();
            }

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public String toString() {
                return "<native clock fn>";
            }
        });

        globals.define("print", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                System.out.println(stringify(args.getFirst()));
                return null;
            }

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public String toString() {
                return "<native print fn>";
            }
        });
    }

    public void interpret(List<Statement> statements) {
        try {
            for (Statement statement : statements) {
                execute(statement);
            }
        } catch (LoxRuntimeError e) {
            Lox.runtimeError(e);
        }
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left = evaluate(expression.left());
        Object right = evaluate(expression.right());

        return switch (expression.operator().type()) {
            case MINUS -> {
                ensureNumberOperands(expression.operator(), left, right);
                yield (double) left - (double) right;
            }
            case SLASH -> {
                ensureNumberOperands(expression.operator(), left, right);
                yield (double) left / (double) right;
            }
            case STAR -> {
                ensureNumberOperands(expression.operator(), left, right);
                yield (double) left * (double) right;
            }
            case MOD -> {
                ensureNumberOperands(expression.operator(), left, right);
                yield (double) left % (double) right;
            }
            case PLUS -> {
                if (left instanceof Double l && right instanceof Double r) {
                    yield l + r;
                }

                if (left instanceof String l && right instanceof String r) {
                    yield l + r;
                }

                throw new LoxRuntimeError(expression.operator(), "Operands must be mutually addable. Lox supports adding numbers to numbers and strings to strings");
            }
            case GREATER -> {
                ensureNumberOperands(expression.operator(), left, right);
                yield (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                ensureNumberOperands(expression.operator(), left, right);
                yield (double) left >= (double) right;
            }
            case LESS -> {
                ensureNumberOperands(expression.operator(), left, right);
                yield (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                ensureNumberOperands(expression.operator(), left, right);
                yield (double) left <= (double) right;
            }
            case BANG_EQUAL -> !isEqual(left, right);
            case EQUAL_EQUAL -> isEqual(left, right);
            default -> null;
        };
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.inner());
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal literal) {
        return literal.value();
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expr) {
        Object right = evaluate(expr.right());

        return switch (expr.operator().type()) {
            case MINUS -> {
                ensureNumberOperand(expr.operator(), right);
                yield -(double) right;
            }
            case BANG -> !isTruthy(right);
            default -> null; // Unreachable
        };
    }

    @Override
    public Object visitVariableExpression(Expression.Variable variable) {
        return lookUpVariable(variable.name(), variable);
    }


    @Override
    public Object visitAssignmentExpression(Expression.Assignment assignment) {
        Object value = evaluate(assignment.value());
        this.environment.assign(assignment.name(), value);
        return value;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical logicalExpression) {
        Object left = evaluate(logicalExpression.left());

        if (logicalExpression.operator().type() == TokenType.OR) {
            if (isTruthy(left)) {
                return left;
            }
        } else /* AND */ {
            if (!isTruthy(left)) {
                return left;
            }
        }

        return evaluate(logicalExpression.right());

    }

    @Override
    public Object visitCallExpression(Expression.Call call) {
        Object callee = evaluate(call.callee());

        List<Object> args = new ArrayList<>();
        for (Expression argument : call.args()) {
            args.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable function)) {
            throw new LoxRuntimeError(call.paren(), "This type is not callable.");
        } else if (args.size() != function.arity()) {
            throw new LoxRuntimeError(call.paren(), "Expected " + function.arity() + " arguments but got " + args.size() + ".");
        } else {
            return function.call(this, args);
        }
    }

    @Override
    public Object visitGetExpression(Expression.Get get) {
        Object obj = evaluate(get.object());

        if (obj instanceof LoxInstance instance) {
            return instance.get(get.name());
        }

        throw new LoxRuntimeError(get.name(), "Only instances have properties.");
    }

    @Override
    public Object visitSetExpression(Expression.Set expr) {
        Object object = evaluate(expr.object());

        if (!(object instanceof LoxInstance inst)) {
            throw new LoxRuntimeError(expr.name(), "Only instances have fields.");
        } else {
            Object value = evaluate(expr.value());
            inst.set(expr.name(), value);
            return value;
        }

    }

    @Override
    public Object visitThisExpression(Expression.This thisExpression) {
        return lookUpVariable(thisExpression.keyword(), thisExpression);
    }

    @Override
    public Object visitSuperExpression(Expression.Super expr) {
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass) environment.getAt(distance, "super");

        LoxInstance object = (LoxInstance) environment.getAt(distance - 1, "this");

        LoxFunction method = superclass.findMethod(expr.method().lexeme());

        if(method == null){
            throw new LoxRuntimeError(expr.method(),"Method does not exist on superclass");
        }
        return method.bind(object);
    }

    private Object evaluate(Expression expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean b) return b;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private void ensureNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new LoxRuntimeError(operator, "Operand must be a number.");
    }

    private void ensureNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        System.out.println(left + " " + right);
        throw new LoxRuntimeError(operator, "Operands must be numbers.");
    }

    @Override
    public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
        evaluate(statement.expression());
        return null;
    }

    @Override
    public Void visitVariableStatement(Statement.VariableStatement statement) {
        Object value = null;

        if (statement.initializer() != null) {
            value = evaluate(statement.initializer());
        }

        this.environment.define(statement.name().lexeme(), value);
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block block) {
        executeBlock(block.statements(), new VariableEnvironment(environment));
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If ifStatement) {
        if (isTruthy(evaluate(ifStatement.condition()))) {
            execute(ifStatement.thenBranch());
        } else if (ifStatement.elseBranch() != null) {
            execute(ifStatement.elseBranch());
        }

        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While whileStatement) {
        while (isTruthy(evaluate(whileStatement.condition()))) {
            execute(whileStatement.body());
        }
        return null;

    }

    @Override
    public Void visitFunction(Statement.Function statement) {
        LoxFunction fn = new LoxFunction(statement, this.environment, false);

        this.environment.define(statement.name().lexeme(), fn);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return returnStatement) {
        Object value = null;
        if (returnStatement.value() != null) value = evaluate(returnStatement.value());

        throw new Return(value);
    }

    @Override
    public Void visitClassStatement(Statement.Class stmt) {
        Object superclass = null;
        if (stmt.superclass() != null) {
            superclass = evaluate(stmt.superclass());
            if (!(superclass instanceof LoxClass)) {
                throw new LoxRuntimeError(stmt.superclass().name(), "Superclass must be a class.");
            }
        }
        environment.define(stmt.name().lexeme(), null);

        if (stmt.superclass() != null) {
            environment = environment.enclosing;
        }

        Map<String, LoxFunction> methods = new HashMap<>();

        for (Statement.Function method : stmt.methods()) {
            LoxFunction function = new LoxFunction(method, environment, method.name().lexeme().equals("init"));
            methods.put(method.name().lexeme(), function);
        }

        LoxClass klass = new LoxClass(stmt.name().lexeme(), (LoxClass) superclass, methods);

        environment.assign(stmt.name(), klass);
        return null;
    }

    public void executeBlock(List<Statement> statements, VariableEnvironment environment) {
        VariableEnvironment previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    public void resolve(Expression expr, int depth) {
        this.locals.put(expr, depth);
    }

    private Object lookUpVariable(Token name, Expression expression) {
        Integer distance = this.locals.getOrDefault(expression, null);

        if (distance != null) {
            return this.environment.getAt(distance, name.lexeme());
        } else {
            return globals.get(name);
        }

    }
}
