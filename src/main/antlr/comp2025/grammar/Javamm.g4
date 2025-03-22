grammar Javamm;

@header {
    package pt.up.fe.comp2025;
}

CLASS : 'class' ;
INT : 'int' ;
PUBLIC : 'public' ;
RETURN : 'return' ;
IMPORT : 'import' ;
EXTENDS : 'extends' ;
BOOLEAN : 'boolean' ;
STATIC : 'static' ;
VOID : 'void' ;
MAIN : 'main' ;
STRING : 'String' ;
IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;
NEW : 'new' ;
THIS : 'this' ;
TRUE : 'true' ;
FALSE : 'false' ;
LENGTH : 'length' ;

DOT : '.' ;
SEMI : ';' ;
LBRACE : '{' ;
RBRACE : '}' ;
LBRACK : '[' ;
RBRACK : ']' ;
LPAREN : '(' ;
RPAREN : ')' ;
ELLIPSIS : '...' ;
COMMA : ',' ;
ASSIGN : '=' ;
AND : '&&' ;
LT : '<' ;
PLUS : '+' ;
MINUS : '-' ;
MULT : '*' ;
DIV : '/' ;
NOT : '!' ;

INTEGER : '0' | [1-9][0-9]* ;
ID : [a-zA-Z_$][a-zA-Z_$0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;

program
    : importDecl* classDecl EOF
    ;

importDecl
    : IMPORT path+=ID (DOT path+=ID)* SEMI
    ;

classDecl
    : CLASS name=ID (EXTENDS superClass=ID)?
        LBRACE
        varDeclaration*
        methodDecl*
        RBRACE
    ;

varDeclaration
    : type name=ID SEMI #VarDecl
    ;

type locals [boolean isArray = false]
    : name=INT (LBRACK RBRACK {$isArray = true;})? #IntType
    | name=INT ELLIPSIS #IntVarArgType
    | name=BOOLEAN #BooleanType
    | name=ID #ClassType
    | name=STRING #StringType
    ;

methodDecl locals[boolean isPublic=false, boolean isStatic=false]
    : (PUBLIC {$isPublic=true;})?
        returnType=type name=ID
        LPAREN (param (COMMA param)*)? RPAREN
        LBRACE
            varDeclaration*
            stmt*
            RETURN expr SEMI
        RBRACE #RegularMethodDecl
    | (PUBLIC {$isPublic=true;})? (STATIC {$isStatic=true;}) VOID name=MAIN
        LPAREN STRING LBRACK RBRACK argName=ID RPAREN
        LBRACE
            varDeclaration*
            stmt*
        RBRACE #MainMethodDecl
    ;

param
    : type name=ID
    ;

stmt
    : LBRACE stmt* RBRACE #BlockStmt
    | IF LPAREN expr RPAREN stmt ELSE stmt #IfStmt
    | WHILE LPAREN expr RPAREN stmt #WhileStmt
    | expr SEMI #ExprStmt
    | name=ID LBRACK expr RBRACK ASSIGN expr SEMI #ArrayAssignStmt
    | name=ID ASSIGN expr SEMI #AssignStmt
    | RETURN expr SEMI #ReturnStmt
    ;

expr
    : LPAREN expr RPAREN #ParenExpr
    | expr LBRACK expr RBRACK #ArrayAccessExpr
    | expr DOT LENGTH #LengthExpr
    | expr DOT name=ID LPAREN (expr (COMMA expr)*)? RPAREN #MethodCallExpr
    | NEW INT LBRACK expr RBRACK #NewIntArrayExpr
    | NEW name=ID LPAREN RPAREN #NewObjectExpr
    | LBRACK (expr (COMMA expr)*)? RBRACK #ArrayExpr
    | NOT expr #NotExpr
    | expr op=(MULT | DIV) expr #BinaryExpr
    | expr op=(PLUS | MINUS) expr #BinaryExpr
    | expr op=LT expr #BinaryExpr
    | expr op=AND expr #BinaryExpr
    | value=INTEGER #IntegerLiteral
    | TRUE #BooleanLiteral
    | FALSE #BooleanLiteral
    | name=ID #VarRefExpr
    | THIS #ThisExpr
    ;

