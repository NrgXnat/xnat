grammar Comment;

lines: line+ EOF ;

line: (string | text | comment)* NEWLINE;

text: word;
comment: COMMENT (word | string)*? ;
string: DQ (word | comment)* DQ ;
word: NOT_NL_DQ+ ;

// Don't skip white spapce here. It messes with finding token postition in input.
//WS : [ \t]+ -> skip ; // skip spaces, tabs

NOT_NL_DQ : ~('\n'|'\r'|'"') ;
DQ  : '"' ;
// COMMENT can not be preceded by ':'
COMMENT :   '//'{ _input.LA(-3) != ':'}? ;
NEWLINE	:	'\r'? '\n';

