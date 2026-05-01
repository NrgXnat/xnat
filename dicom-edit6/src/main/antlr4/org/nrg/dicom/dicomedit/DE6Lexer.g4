lexer grammar DE6Lexer;

WS          :  [ \t]+ -> skip;

D_QUOTE    :  '"' ;
COMMA      :  ',' ;
L_PAREN    :  '(' ;
R_PAREN    :  ')' ;
L_BRACKET  :  '[' ;
R_BRACKET  :  ']' ;
L_BRACE    :  '{' ;
R_BRACE    :  '}' ;
SLASH      :  SLASH_F ;

// operators
OP_ASSIGN           : ':=' ;
OP_ASSIGN_IF_EXISTS : '?=' ;
OP_DELETE           : '-' ;

// condition operators
QUESTION_MARK :  '?'  ;
COLON         :  ':'  ;
EQUALS	      :  '='  ;
NOT_EQUALS    :  '!=' ;
MATCHES       :  '~'  ;
NOT_MATCHES	  :  '!~' ;
IF            :  'if' ;
ELSEIF        :  'elseif' ;
ELSE          :  'else' ;

DESCRIBE        :  'describe';
HIDDEN_TOKEN    :  'hidden';
CONST_TOKEN     :  'const';
IMMUTABLE_TOKEN	:  'immutable';
EXPORT          :  'export';

// hard-coded functions
ECHO                    :  'echo' ;
REMOVE_ALL_PRIVATE_TAGS :  'removeAllPrivateTags' ;
VERSION_WORD            :  'version' ;

HEXDIGIT_WILDCARD :  [Xx] ;
ITEM_WILDCARD     :  [%] ;
SEQ_WILDCARD      :  [*+.] ;
ID                :  (LETTER|'_') (LETTER|DIGIT|'_')* ;
INTEGER	          :  '0' | ('-'? NON_ZERO_DIGIT DIGIT*);
FLOAT             :  '-'? ( '0' '.' DIGIT+ | NON_ZERO_DIGIT DIGIT* '.' DIGIT+) ;
STRING            :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"' ;

PUBLIC_GROUP      :  '(' EVEN_HEXWORD ;
PVT_GROUP         :  '(' ODD_HEXWORD ;
PUBLIC_ELEMENT    :  HEXWORD ')';
//PVT_ELEMENT       :  '[' ID ']' HEXDIGIT HEXDIGIT ')';
PVT_ELEMENT    : OPEN PVT_CREATOR_ID CLOSE HEXDIGIT HEXDIGIT ')';

COMMENT :   '//' ~('\n'|'\r')* ;
NEWLINE	:	'\r'? '\n';

OPEN: L_BRACE  -> mode(PVT_CREATOR_MODE) ;

// space thru ~, allowing '\'. disallowing ESC.
// except we have to disallow '?'. Parsing gets confused by '?' (TEST_SYMBOL) in condition.
// This is in the PVT_CREATOR_MODE so not sure why this matters.
fragment DICOM_DEFAULT_LO_CHAR
//    :   [\b-~]
    :   [\b->@-~]
    ;

fragment LETTER          :  ('a'..'z' | 'A'..'Z') ;
fragment DIGIT	         :  ('0' | NON_ZERO_DIGIT);
fragment NON_ZERO_DIGIT  :  [1-9] ;
fragment ESC_SEQ         :  '\\' ( [nrtbf()\\] | [0-7] [0-7] [0-7] ) ;

fragment ODD_HEXDIGIT    :  [13579bBdDfF] | [#];
fragment EVEN_HEXDIGIT   :  [02468aAcCeE] | [@];
fragment HEXDIGIT        :  ODD_HEXDIGIT | EVEN_HEXDIGIT | HEXDIGIT_WILDCARD ;
fragment EVEN_HEXWORD    :  HEXDIGIT HEXDIGIT HEXDIGIT EVEN_HEXDIGIT ;
fragment ODD_HEXWORD     :  HEXDIGIT HEXDIGIT HEXDIGIT ODD_HEXDIGIT ;
fragment HEXWORD         :  HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT ;
fragment SLASH_F         : '/' ;
fragment SPLAT_F         : '*' ;

mode PVT_CREATOR_MODE ;
PVT_CREATOR_ID : LETTER ( LETTER | DIGIT | ' ' | '-' | '_' | '.' | ':')* ;
//PVT_CREATOR_ID : LETTER ( DICOM_DEFAULT_LO_CHAR)* ;
//PVT_CREATOR_ID : DICOM_DEFAULT_LO_CHAR+ ;
CLOSE : R_BRACE  -> mode(DEFAULT_MODE) ;

