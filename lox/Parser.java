package lox;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!this.isAtEnd()) {
            statements.add(this.declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (this.match(TokenType.VAR))
                return this.varDeclaration();

            return this.statement();
        } catch (ParseError error) {
            this.synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = this.consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (this.match(TokenType.EQUAL)) {
            initializer = this.expression();
        }

        this.consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (this.match(TokenType.PRINT)) {
            return this.printStatement();
        }

        if (this.match(TokenType.LEFT_BRACE))
            return new Stmt.Block(this.blockStatement());

        return this.expressionStatement();
    }

    private List<Stmt> blockStatement() {
        List<Stmt> statements = new ArrayList<>();

        while (!this.check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(this.declaration());
        }

        this.consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");

        return statements;
    }

    private Stmt printStatement() {
        Expr value = this.expression();
        this.consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr value = this.expression();
        this.consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(value);
    }

    private Expr expression() {
        return this.assigement();
    }

    private Expr assigement() {
        Expr expr = this.equality();

        if (this.match(TokenType.EQUAL)) {
            Token equals = this.previous();
            Expr value = this.assigement();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = this.comparison();

        while (this.match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = this.previous();
            Expr right = this.comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = this.term();

        while (this.match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = this.previous();
            Expr right = this.term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = this.factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = this.previous();
            Expr right = this.factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = this.unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = this.previous();
            Expr right = this.unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = this.previous();
            Expr right = this.unary();
            return new Expr.Unary(operator, right);
        }

        return this.primary();
    }

    private Expr primary() {
        Token currentToken = this.peek();
        this.advance();

        switch (currentToken.type) {
            case FALSE:
                return new Expr.Literal(false);
            case TRUE:
                return new Expr.Literal(true);
            case NIL:
                return new Expr.Literal(null);
            case NUMBER:
            case STRING:
                return new Expr.Literal(this.previous().literal);
            case IDENTIFIER:
                return new Expr.Variable(this.previous());
            case LEFT_PAREN:
                Expr expr = this.expression();
                this.consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
                return new Expr.Grouping(expr);
            default:
                throw error(currentToken, "Expect expression.");
        }
    }

    private boolean match(
            TokenType... types) {
        for (TokenType type : types) {
            if (this.check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        return this.tokens.get(current).type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;

        return previous();
    }

    private boolean isAtEnd() {
        return this.peek().type == TokenType.EOF;
    }

    private Token peek() {
        return this.tokens.get(this.current);
    }

    private Token previous() {
        return this.tokens.get(this.current - 1);
    }

    private Token consume(TokenType type, String errorMessage) {
        if (this.check(type))
            return advance();

        throw error(peek(), errorMessage);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);

        return new ParseError();
    }

    private void synchronize() {
        this.advance();

        while (!this.isAtEnd()) {
            if (this.previous().type == TokenType.SEMICOLON)
                return;

            switch (this.peek().type) {
                case FOR:
                case WHILE:
                case FUN:
                case VAR:
                case CLASS:
                case IF:
                case PRINT:
                case RETURN:
                    return;
                default:
                    break;
            }

            this.advance();
        }
    }
}
