parser grammar DE6Parser;

options { tokenVocab=DE6Lexer; }   //use tokens from StringLexer.g4

script
    :	separator? EOF
	|	separator* statement (separator* statement)* separator* EOF
	;

separator
	:	(COMMENT? NEWLINE)+
	;
string: STRING ;

statement
    :   action
	|	conditional_statement
	|   if_elseif_else
	|	description
	|	export_stmt
	|	removeAllPrivateTags
	|	version
	;

// set of statements that can be in a conditional statement.
action
    : assign
    | assign_if_exists
    | initialize
    | delete
	| function
	| echo
;

assign           : lvalue OP_ASSIGN value ;
initialize       : ID OP_ASSIGN (value | list) ;
assign_if_exists : lvalue OP_ASSIGN_IF_EXISTS value ;
delete           : OP_DELETE lvalue ;
function         : ID L_BRACKET argumentlist? R_BRACKET ;
echo             : ECHO value | ECHO ;

number : INTEGER  #intvalue
       | FLOAT    #floatvalue ;
value
    : number        # numberValue
    | variable      # idValue
    | STRING        # stringValue
    | tagpath       # tagpathValue
    | function      # functionValue
    | list          # listValue
    ;

valuelist
	:	value (COMMA value)*
	;

argument
    : number       # numberArgument
    | variable     # variableArgument
    | string       # stringArgument
    | tagpath      # tagpathArgument
    | function     # functionArgument
    | list         # listArgument
    ;

argumentlist
	:	argument (COMMA argument)*
	;

lvalue : tagpath       # tagpathLvalue ;

variable : ID ;

conditional_statement
    :   (condition QUESTION_MARK action) (COLON action)?;

condition
	:	value conditionOperator value   # booleanExpression
	|   function                        # booleanFunction
	;

conditionOperator
    :   EQUALS | MATCHES | NOT_EQUALS | NOT_MATCHES;

if_elseif_else
    :   IF L_PAREN condition R_PAREN separator* block separator* (ELSEIF L_PAREN condition R_PAREN separator* block separator*)* (ELSE separator* block separator*)?
    ;

block
    : L_BRACE block_statement* R_BRACE
    ;

block_statement
    :   separator | statement separator?
    ;

description
	:	DESCRIBE ID STRING           # describeNamedVariable
	|	DESCRIBE ID HIDDEN_TOKEN     # describeHiddenVariable
	|	DESCRIBE ID CONST_TOKEN      # describeConstantVariable
	|	DESCRIBE ID IMMUTABLE_TOKEN  # describeImmutableVariable
	;

export_stmt : EXPORT ID STRING ;

removeAllPrivateTags : REMOVE_ALL_PRIVATE_TAGS ;

version : VERSION_WORD STRING ;

list_element : NEWLINE? (string | number | tagpath | variable | function | list);
list_entry : list_element NEWLINE? ;
list : L_BRACE NEWLINE? (list_entry (COMMA list_entry)*)? NEWLINE? R_BRACE ;

tag : public_tag | pvt_tag;

element: public_tag | pvt_tag ;
seq_element: (element itemnumber?) | seq_wildcard ;

tagpath: (element | seq_element) | ((seq_element SLASH)+ (element | seq_element)) ;

itemnumber: L_BRACKET (INTEGER | ITEM_WILDCARD) R_BRACKET ;

seq_wildcard: SEQ_WILDCARD;

public_tag : PUBLIC_GROUP COMMA PUBLIC_ELEMENT ;
pvt_tag    : PVT_GROUP COMMA PVT_ELEMENT ;
