package dev.rats159.lox.interpreting;

import dev.rats159.lox.Lox;
import dev.rats159.lox.constructs.*;
import dev.rats159.lox.errors.LoxRuntimeError;
import dev.rats159.lox.errors.LoxTypeError;
import dev.rats159.lox.lexing.Token;
import dev.rats159.lox.lexing.TokenType;
import dev.rats159.lox.parsing.Expression;
import dev.rats159.lox.parsing.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expression.Visitor<LoxObject>, Statement.Visitor<Void> {

    public final VariableEnvironment globals = new VariableEnvironment();
    private VariableEnvironment environment = globals;
    private final Map<Expression, Integer> locals = new HashMap<>();

    public Interpreter() {
        globals.define("clock", new LoxBuiltin(0) {
            @Override
            public String name() {
                return "clock";
            }

            @Override
            public LoxObject call(Interpreter interpreter, List<LoxObject> args) {
                return new LoxNumber(System.currentTimeMillis());
            }
        });

        globals.define("print", new LoxBuiltin(1) {
            @Override
            public String name() {
                return "print";
            }

            @Override
            public LoxObject call(Interpreter interpreter, List<LoxObject> args) {
                System.out.println(args.getFirst().toLangString());
                return null;
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

    @Override
    public LoxObject visitBinaryExpression(Expression.Binary expression) {
        LoxObject left = evaluate(expression.left());
        LoxObject right = evaluate(expression.right());

        return switch (expression.operator().type()) {
            case MINUS -> {
                if(left instanceof LoxSubtractable l && right instanceof LoxSubtractable r){
                    yield l.subtract(r);
                }
                throw new LoxRuntimeError(expression.operator(), "Unsubtractable types `%s` and `%s`".formatted(left.type(),right.type()));
            }
            case SLASH -> {
                if(left instanceof LoxDivisible l && right instanceof LoxDivisible r){
                    yield l.divide(r);
                }
                throw new LoxRuntimeError(expression.operator(), "Undivisible types `%s` and `%s`".formatted(left.type(),right.type()));

            }
            case STAR -> {
                try{
                    if(left instanceof LoxMultipliable l && right instanceof LoxMultipliable r){
                        yield l.multiply(r);
                    }
                }catch(LoxTypeError err){
                    throw new LoxRuntimeError(expression.operator(),err.getMessage());
                }
                throw new LoxRuntimeError(expression.operator(), "Unmultipliable types `%s` and `%s`".formatted(left.type(),right.type()));
            }
            case MOD -> {
                if(left instanceof LoxModuloable l && right instanceof LoxModuloable r){
                    yield l.mod(r);
                }
                throw new LoxRuntimeError(expression.operator(), "Unmoduloable types `%s` and `%s`".formatted(left.type(),right.type()));
            }
            case PLUS -> {
                try{
                    if (left instanceof LoxAddable l && right instanceof LoxAddable r) {
                        yield l.add(r);
                    }
                }catch(LoxTypeError err){
                    throw new LoxRuntimeError(expression.operator(),err.getMessage());
                }

                throw new LoxRuntimeError(expression.operator(), "Unaddable types `%s` and `%s`".formatted(left.type(),right.type()));
            }
            case GREATER -> {
                if (left instanceof LoxComparable l && right instanceof LoxComparable r) {
                    yield l.greater(r);
                }
                throw new LoxRuntimeError(expression.operator(), "Incomparable types `%s` and `%s`".formatted(left.type(),right.type()));
            }
            case GREATER_EQUAL -> {
                if (left instanceof LoxComparable l && right instanceof LoxComparable r) {
                    yield l.greaterOrEqual(r);
                }
                throw new LoxRuntimeError(expression.operator(), "Incomparable types `%s` and `%s`".formatted(left.type(),right.type()));
            }
            case LESS -> {
                if (left instanceof LoxComparable l && right instanceof LoxComparable r) {
                    yield l.less(r);
                }
                throw new LoxRuntimeError(expression.operator(), "Incomparable types `%s` and `%s`".formatted(left.type(),right.type()));
            }
            case LESS_EQUAL -> {
                if (left instanceof LoxComparable l && right instanceof LoxComparable r) {
                    yield l.lessOrEqual(r);
                }
                throw new LoxRuntimeError(expression.operator(), "Incomparable types `%s` and `%s`".formatted(left.type(),right.type()));
            }
            case BANG_EQUAL -> {
                if (left instanceof LoxEquateable l && right instanceof LoxEquateable r) {
                    yield l.unequal(r);
                }
                throw new LoxRuntimeError(expression.operator(), "Inequatable types `%s` and `%s`".formatted(left.type(),right.type()));
            }
            case EQUAL_EQUAL -> {
                if (left instanceof LoxEquateable l && right instanceof LoxEquateable r) {
                    yield l.equal(r);
                }
                throw new LoxRuntimeError(expression.operator(), "Inequatable types `%s` and `%s`".formatted(left.type(),right.type()));
            }
            default -> null;
        };
    }

    @Override
    public LoxObject visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.inner());
    }

    @Override
    public LoxObject visitLiteralExpression(Expression.Literal literal) {
        return literal.value();
    }

    @Override
    public LoxObject visitUnaryExpression(Expression.Unary expr) {
        LoxObject right = evaluate(expr.right());

        return switch (expr.operator().type()) {
            case MINUS-> {
                if (right instanceof LoxNegatable r) {
                    yield r.negate();
                }
                throw new LoxRuntimeError(expr.operator(), "Unnegatable type `%s`".formatted(right.type()));
            }
            case BANG -> {
                if (right instanceof LoxInvertable r) {
                    yield r.invert();
                }
                throw new LoxRuntimeError(expr.operator(), "Uninvertable type `%s`".formatted(right.type()));
            }
            default -> null; // Unreachable
        };
    }

    @Override
    public LoxObject visitVariableExpression(Expression.Variable variable) {
        return lookUpVariable(variable.name(), variable);
    }


    @Override
    public LoxObject visitAssignmentExpression(Expression.Assignment assignment) {
        LoxObject value = evaluate(assignment.value());
        this.environment.assign(assignment.name(), value);
        return value;
    }

    @Override
    public LoxObject visitLogicalExpression(Expression.Logical logicalExpression) {
        LoxObject left = evaluate(logicalExpression.left());

        if (logicalExpression.operator().type() == TokenType.OR) {
            if (left.isTruthy()) {
                return left;
            }
        } else /* AND */ {
            if (!left.isTruthy()) {
                return left;
            }
        }

        return evaluate(logicalExpression.right());

    }

    @Override
    public LoxObject visitCallExpression(Expression.Call call) {
        LoxObject callee = evaluate(call.callee());

        List<LoxObject> args = new ArrayList<>();
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
    public LoxObject visitGetExpression(Expression.Get get) {
        Object obj = evaluate(get.object());

        if (obj instanceof LoxInstance instance) {
            return instance.get(get.name());
        }

        throw new LoxRuntimeError(get.name(), "Only instances have properties.");
    }

    @Override
    public LoxObject visitSetExpression(Expression.Set expr) {
        LoxObject object = evaluate(expr.object());

        if (!(object instanceof LoxInstance inst)) {
            throw new LoxRuntimeError(expr.name(), "Only instances have fields.");
        } else {
            LoxObject value = evaluate(expr.value());
            inst.set(expr.name(), value);
            return value;
        }

    }

    @Override
    public LoxObject visitThisExpression(Expression.This thisExpression) {
        return lookUpVariable(thisExpression.keyword(), thisExpression);
    }

    @Override
    public LoxObject visitSuperExpression(Expression.Super expr) {
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass) environment.getAt(distance, "super");

        LoxInstance object = (LoxInstance) environment.getAt(distance - 1, "this");

        LoxFunction method = superclass.findMethod(expr.method().lexeme());

        if(method == null){
            throw new LoxRuntimeError(expr.method(),"Method does not exist on superclass");
        }
        return method.bind(object);
    }

    private LoxObject evaluate(Expression expr) {
        return expr.accept(this);
    }

    @Override
    public Void visitExpressionStatement(Statement.ExpressionStatement statement) {
        evaluate(statement.expression());
        return null;
    }

    @Override
    public Void visitVariableStatement(Statement.VariableStatement statement) {
        LoxObject value = null;

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
        if (evaluate(ifStatement.condition()).isTruthy()) {
            execute(ifStatement.thenBranch());
        } else if (ifStatement.elseBranch() != null) {
            execute(ifStatement.elseBranch());
        }

        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While whileStatement) {
        while (evaluate(whileStatement.condition()).isTruthy()) {
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
        LoxObject value = null;
        if (returnStatement.value() != null) value = evaluate(returnStatement.value());

        throw new LoxReturn(value);
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

    private LoxObject lookUpVariable(Token name, Expression expression) {
        Integer distance = this.locals.getOrDefault(expression, null);

        if (distance != null) {
            return this.environment.getAt(distance, name.lexeme());
        } else {
            return globals.get(name);
        }

    }
}
