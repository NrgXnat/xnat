lexer grammar EditDCMLexer;

@header {
	package org.nrg.dcm.edit;
}

LEFT	:	'[';
RIGHT	:	']';
COMMA	:	',';

CONSTRAINS :	':';
DELETE	:	'-';
EQUALS	:	'=';
MATCHES	:	'~';
ASSIGN	:	':=';

ECHO	:	'echo';
NEW	:	'new';
DESCRIBE:	'describe';
HIDDEN	:	'hidden';
EXPORT  :	'export';


/* predefined identifiers:
 * UID (with new)
 * study index TBD
 * substring (as function)
 */
ID  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

COMMENT
    :   '//' ~('\n'|'\r')* { $channel=HIDDEN; }
    ;

WS  :   ( ' '
        | '\t'
        | ('\\' '\r'? '\n')
        ) {$channel=HIDDEN;}
    ;
    
NEWLINE	:	'\r'? '\n';



STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;

NUMBER	:	'0' | ('-'? '1'..'9' DIGIT*);

TAG	:	LPAREN HEXWORD COMMA HEXWORD RPAREN;

TAGPATTERN
	:	LPAREN HEXPATWORD COMMA HEXPATWORD RPAREN;

fragment LPAREN	:	'(';

fragment RPAREN	:	')';

fragment DIGIT	:	('0'..'9');

fragment HEXWORD
	:	HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT;

fragment HEXDIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment HEXPATWORD
	:	HEXPATTERN HEXPATTERN HEXPATTERN HEXPATTERN;
	
fragment HEXPATTERN 
	:	'X' | '@' | '#' | HEXDIGIT;


fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT
    ;
