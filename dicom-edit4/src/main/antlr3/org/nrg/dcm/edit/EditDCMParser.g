parser grammar EditDCMParser;

options {
	output = AST;
	tokenVocab = EditDCMLexer;
}

tokens {
	CONSTRAINED;
	FORMAT;
	FUNCTION;
	INITIALIZE;
}

@header {
	package org.nrg.dcm.edit;
}
	
script	:	separator!? EOF!
	|	separator!? statement (separator! statement)* separator!? EOF!;

separator 
	:	(COMMENT? NEWLINE)+
	;
	
statement
	:	action
	|	constraint action -> ^(CONSTRAINED action constraint)
	|	initialization
	|	description
	;

action	:	assignment | deletion | echo;

constraint
	:	(condition CONSTRAINS!)+
	;
	
lvalue	:	TAG | TAGPATTERN;
		
assignment
	:	lvalue ASSIGN value -> ^(ASSIGN lvalue value)
	|	TAG ASSIGN NEW ID -> ^(NEW TAG ID)
	|	TAG ASSIGN NEW ID LEFT termlist RIGHT -> ^(NEW TAG ID termlist)
	;
	
echo	:	ECHO value -> ^(ECHO value)
	|	ECHO
	;

deletion:	DELETE lvalue -> ^(DELETE lvalue)
	;

initialization
	:	ID ASSIGN value -> ^(INITIALIZE ID value)
	;
	
description
	:	DESCRIBE ID STRING -> ^(DESCRIBE ID STRING)
	|	DESCRIBE ID HIDDEN -> ^(HIDDEN ID)
	|	EXPORT ID STRING   -> ^(EXPORT ID STRING)
	;
	
value	:	STRING termlist -> ^(FORMAT STRING termlist)
	|	term
	;
	
term	:	STRING
	|	NUMBER
	|	TAG
	|	ID LEFT termlist RIGHT -> ^(FUNCTION ID termlist)
	|	ID
	;
	
termlist 
	:	term (COMMA! term)*
	;
	
condition
options { backtrack = true; }
	:	value EQUALS value -> ^(EQUALS value value)
	|	value MATCHES value -> ^(MATCHES value value)
	;
