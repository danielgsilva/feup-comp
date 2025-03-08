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
    : importDecl* classDeclaration EOF
    ;

importDecl
    : IMPORT ID (DOT ID)* SEMI
    ;

classDeclaration
    : CLASS name=ID (EXTENDS ID)?
        LBRACE
        varDeclaration*
        methodDecl*
        RBRACE
    ;

varDeclaration
    : type name=ID SEMI
    ;

type
    : INT LBRACK RBRACK
    | INT ELLIPSIS
    | BOOLEAN
    | name= INT
    | ID
    | STRING
    ;

methodDecl locals[boolean isPublic=false]
    : (PUBLIC {$isPublic=true;})?
        type name=ID
        LPAREN (param (COMMA param)*)? RPAREN
        LBRACE
            varDeclaration*
            stmt*
            RETURN expr SEMI
        RBRACE
    | PUBLIC? STATIC VOID MAIN
        LPAREN STRING LBRACK RBRACK ID RPAREN
        LBRACE
            varDeclaration*
            stmt*
        RBRACE
    ;

param
    : type name=ID
    ;

stmt
    : LBRACE stmt* RBRACE
    | IF LPAREN expr RPAREN stmt ELSE stmt
    | WHILE LPAREN expr RPAREN stmt
    | expr SEMI
    | ID LBRACK expr RBRACK ASSIGN expr SEMI
    | expr ASSIGN expr SEMI
    | RETURN expr SEMI
    ;

expr
    : LPAREN expr RPAREN
    | expr LBRACK expr RBRACK
    | expr DOT LENGTH
    | expr DOT ID LPAREN (expr (COMMA expr)*)? RPAREN
    | NEW INT LBRACK expr RBRACK
    | NEW ID LPAREN RPAREN
    | LBRACK (expr (COMMA expr)*)? RBRACK
    | NOT expr
    | expr op=(MULT | DIV) expr
    | expr op=(PLUS | MINUS) expr
    | expr op=LT expr
    | expr op=AND expr
    | value=INTEGER
    | TRUE
    | FALSE
    | name=ID
    | THIS
    ;

