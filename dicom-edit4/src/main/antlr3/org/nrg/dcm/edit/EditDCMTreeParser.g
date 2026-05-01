tree grammar EditDCMTreeParser;

options {
	tokenVocab = EditDCMParser;
	ASTLabelType = CommonTree;
}

@header {
	package org.nrg.dcm.edit;

	import org.nrg.dicom.mizer.exceptions.MultipleInitializationException;
	import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
	import org.nrg.dicom.mizer.scripts.ScriptFunction;
	import org.nrg.dicom.mizer.values.*;
	import org.nrg.dicom.mizer.variables.BasicVariable;
	import org.nrg.dicom.mizer.variables.Variable;

	import java.io.IOException;
	import java.util.Collections;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Map;
	import java.util.HashMap;
	import java.util.TreeMap;
}

@members {
	private final Map<String,Variable> variables = new TreeMap<String,Variable>();
	private final Map<String,ScriptFunction> functions = new HashMap<String,ScriptFunction>();
	
  /**
   * Adapted from JSON interpreter string extractor by Richard Clark
   **/
  private static String extractString(final CommonTree token) {
    final StringBuffer sb = new StringBuffer(token.getText());
    
    sb.deleteCharAt(0); // remove leading and trailing quotes
    sb.deleteCharAt(sb.length() - 1);
    
    int i = 0;
    while (i < sb.length() - 1) {
      final int iesc = sb.indexOf("\\", i);  // look for a backslash
      if (-1 == iesc) {
        break;  // no backlashes found; move on
      }
      
      final int cesc = sb.charAt(iesc + 1);
      switch (cesc) {
        case 'b': // backspace
          sb.replace(iesc, iesc + 2, "\b");
          break;
          
        case 't': // tab
          sb.replace(iesc, iesc + 2, "\t");
          break;
          
        case 'n': // newline
          sb.replace(iesc, iesc + 2, "\n");
          break;
          
        case 'f': // form feed
          sb.replace(iesc, iesc + 2, "\f");
          break;
          
        case 'r': // return
          sb.replace(iesc, iesc + 2, "\r");
          break;
          
        case '"': // double quote
          sb.replace(iesc, iesc + 2, "\"");
          break;
          
        case '\'':  // single quote
          sb.replace(iesc, iesc + 2, "'");
          break;
          
        case '\\':  // backslash
          sb.replace(iesc, iesc + 2, "\\");
          break;
          
        case 'u': // unicode escape
          throw new UnsupportedOperationException();
          
        case '0': // octal escape
          throw new UnsupportedOperationException();
        
        default:
          throw new UnsupportedOperationException("no support for escape code \\" + cesc);
       }
       i = iesc + 1;
    }
    
    return sb.toString();
  }
	
  private static TagPattern tagPattern(final CommonTree token) throws RecognitionException {
    try {
      return new TagPattern(token.getText().substring(1,10));
    } catch (ScriptEvaluationException e) {
      throw new RuntimeException("invalid tag pattern " + token.getText(), e);
    }
  }
	
  private static int tagLiteral(final CommonTree token) {
    final StringBuffer hex = new StringBuffer("0x");
    final String text = token.getText();
    hex.append(text.substring(1, 5));
    hex.append(text.substring(6, 10));
    return Integer.decode(hex.toString()).intValue();
  }
        

	
	private Variable getVariable(final CommonTree token) {
		return getVariable(token.getText());
	}
		
	public Variable getVariable(final String label) {
		final String canonical = label.toLowerCase();
		if (variables.containsKey(canonical)) {
			return (Variable)variables.get(canonical);
		} else {
			final Variable v = new BasicVariable(label);
			variables.put(canonical, v);
			return v;
		}
	}

	/**
	 * Unify the provided variable with matching variables in this environment.
	 * If a similarly-named variable already exists in this environment, replace
	 * that one with the provided v and return v. Otherwise, do not add v to the
	 * environment and return null.
	 * If v has no initial value set, but the previous variable does, v's initial
	 * value is set to that of the previous variable.
	 * @param v Variable that should replace any similarly-named variables in
	 *           this parser's environment.
	 * @return v if a similarly-named variable already existed here, null otherwise
	 */
	public Variable unify(final Variable v) {
		final String cname = v.getName().toLowerCase();
		final Variable vold = variables.get(cname);
		if (null == vold) {
			return null;
		} else {
			variables.put(cname, v);
			if (null == v.getInitialValue()) {
				try {
					v.setInitialValue(vold.getInitialValue());
				} catch (MultipleInitializationException e) {
					// really shouldn't happen
					throw new RuntimeException(e);
				}
			}
			for (final Variable var : variables.values()) {
				final Value iv = var.getInitialValue();
				if (null != iv) {
					iv.replace(v);
				}
			}
			return v;
		}
	}
	
	public final Map<String,Variable> getVariables() {
		return Collections.unmodifiableMap(variables);
	}
	
	private final GeneratorScope scope = new GeneratorScope();
	
	
	public void setGenerator(final String label, final ValueGenerator g) {
		scope.setGenerator(label, g);
	}
	
	public void setFunction(final String label, final ScriptFunction f) {
		functions.put(label, f);
	}
		
	private Value apply(final String name, final List termlist)
	throws ScriptEvaluationException {
		final ScriptFunction f = (ScriptFunction)functions.get(name);
		if (null == f) {
			throw new ScriptEvaluationException("undefined function " + name);
		} else {
			return f.apply(termlist);
    		}
	}
	
	/*
	 * testing only
	 */
	private static void process(final ANTLRStringStream input) throws RecognitionException {
	  final EditDCMLexer lexer = new EditDCMLexer(input);
	  final EditDCMParser parser = new EditDCMParser(new CommonTokenStream(lexer));
	  final EditDCMParser.script_return script = parser.script();  // start rule method
	  
	  final CommonTree ast = (CommonTree) script.getTree();
	  if (null == ast) {
	    System.out.println("empty script");
	  } else {
	    final EditDCMTreeParser treeParser = new EditDCMTreeParser(new CommonTreeNodeStream(ast));
	    final List<Statement> statements = treeParser.script();
	    
	    System.out.println(statements);
	    
	    // dump the variables
	    System.out.println(treeParser.variables);
	  }
	}
	
	public static void main(final String args[]) throws IOException,RecognitionException {
	  if (0 == args.length) {
	  System.out.println("processing stdin");
	   process(new ANTLRInputStream(System.in));
	  } else {
	    for (int i = 0; i < args.length; i++) {
	      System.out.println("script file " + args[i] + ":");
	      process(new ANTLRFileStream(args[i]));
	    }
	  }
  }
  
  /*
   * testing only
   */
}

script returns [List<Statement> sl]
	@init {
		$sl = new ArrayList<Statement>();
	}
	:	(s=statement { if (null != s) $sl.add(s); })*;

statement returns [Statement statement]
	:	action	{ $statement = new Statement($action.op); }
	|	^(CONSTRAINED a=action cl=constraint) {
			switch (cl.size()) {
				case 0: {
					$statement = new Statement(a);
					break;
				}
				case 1: {
					final Constraint c = new Constraint((ConstraintMatch)cl.get(0));
					$statement = new Statement(c, a);
					break;
				}
				default: {
					final Constraint c = new Constraint(new ConstraintConjunction(cl));
					$statement = new Statement(c, a);
					break;
				}
			}
		}
	|	description	{ $statement = null; }
	|	initialization	{ $statement = null; }
	;
	
action returns [Operation op]
	:	assignment	{ $op = $assignment.operation; }
	|	deletion	{ $op = $deletion.deletion; }
	|	echo		{ $op = $echo.echo; }
	;

assignment returns [Operation operation]
	:	^(ASSIGN lvalue value) {
			$operation = new Assignment($lvalue.tp, $value.v);
		}
	|	^(NEW TAG ID termlist) {
			$operation = new Assignment(tagPattern($TAG),
				new GeneratedValue(scope, tagLiteral($TAG), $ID.getText(), $termlist.tl));
		}
	;
	
constraint returns [List conditions]
	@init {
		$conditions = new ArrayList();
	}
	:	(c=condition { $conditions.add(c); })+
	;

deletion returns [Deletion deletion]
	:	^(DELETE lvalue) {
			$deletion = new Deletion($lvalue.tp);
		}
	;

initialization
	:	^(INITIALIZE ID value) {
			try {
				getVariable($ID).setInitialValue($value.v);
			} catch (MultipleInitializationException e) {
				e.printStackTrace();
				throw new RecognitionException();	// TODO: better
			}
		}
	;
	
description
	:	^(DESCRIBE id=ID desc=STRING) {
			getVariable(id).setDescription(extractString(desc));
		}
	|	^(HIDDEN id=ID) {
			getVariable(id).setIsHidden(true);
		}
	|	^(EXPORT id=ID field=STRING) {
			getVariable(id).setExportField(extractString(field));
		}
	;
	
echo returns [Echo echo]
	:	^(ECHO v=value) {
			$echo = new Echo(v);
		}
	|	ECHO {
			$echo = new Echo("");
		};

term returns [Value v]
	:	STRING	{ $v = new ConstantValue(extractString($STRING)); }
	|	NUMBER	{ $v = new IntegerValue($NUMBER.getText()); }
	|	TAG		{ $v = new SingleTagValue(tagLiteral($TAG)); }
	|	ID		{ $v = new VariableValue(getVariable($ID)); }
	|	^(FUNCTION ID termlist)	{
	   		try {
	   			$v = apply($ID.getText(), $termlist.tl);
	   		} catch (ScriptEvaluationException e) {
	   			throw new RecognitionException(input);	// TODO: better
	   		}
	   	}
	;
	
termlist returns [List tl]
	@init {
		tl = new ArrayList();
	}
	:	(t=term { tl.add(t); })*
	;
	
lvalue returns [TagPattern tp] :
		t=TAG { $tp = tagPattern($TAG); }
	|	t=TAGPATTERN { $tp = tagPattern($TAGPATTERN); }
	;
	
value returns [Value v]
 	:	t=term { $v = t; }
	|	^(FORMAT STRING termlist) {
			$v = new MessageFormatValue(extractString($STRING), $termlist.tl);
		}
	;

condition returns [ConstraintMatch cm]
	:	^(EQUALS v1=value v2=value) {
			$cm = new ValueEqualsConstraint(v1, v2);
		}
	|	^(MATCHES s=value p=value) {
			$cm = new ValueRegexConstraint(s, p);
		}
	;
