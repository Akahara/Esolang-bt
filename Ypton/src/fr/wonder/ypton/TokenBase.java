package fr.wonder.ypton;


public enum TokenBase {
	
	/* Used as-is */
	
	VAR_VARIABLE("\\$\\$[a-z](?:\\w|[.\\[\\]])*"),
	VAR_RAW_VAR(null),
	
	LIT_INT("\\d+"),
	LIT_FLOAT("\\d+\\.\\d*"),
	LIT_STR(null),
	LIT_BOOL_TRUE("(?i)true"),
	LIT_BOOL_FALSE("(?i)false"),
	
	KW_IFNT("(?i)ifnt"),
	KW_OTHERWISE("(?i)otherwise"),
	KW_FOR("(?i)for"),
	KW_WHILE("(?i)while"),
	KW_DEF("(?i)def"),
	KW_RETURN("(?i)return"),
	
	/* Used as section tokens */
	
	/** not to mistake with {@link #OP_EQUALS}, this one is <code>=</code> */
	KW_EQUAL("="),
	KW_EQUAL_PLUS("+="),
	KW_EQUAL_MINUS("-="),
	KW_EQUAL_MUL("*="),
	KW_EQUAL_DIV("/="),
	KW_EQUAL_MOD("%="),
	
	/** line break ';', stripped by the Tokenizer to split individual lines */
	TK_LINE_BREAK(";"),
	TK_LINE_BREAK_NOT("...!;"),
	/** new line '\n', stripped by the Tokenizer, they are completely ignored during code generation */
	TK_SPACE(" "),
	TK_ARROW("->"),
	TK_COMMA(","),
	TK_BRACE_OPEN("{"), TK_BRACE_CLOSE("}"),
	TK_BRACKET_OPEN("["), TK_BRACKET_CLOSE("]"),
	TK_PARENTHESIS_OPEN("("), TK_PARENTHESIS_CLOSE(")"),
	TK_DOUBLE_QUOTE("\""),
	TK_APOSTROPHE("'"),
	TK_BACK_APOSTROPHE("`"),
	
	/** not to mistake with {@link #KW_EQUAL}, this one is <code>==</code> */
	OP_EQUALS("=="),
	OP_LEQUALS("<="),
	OP_GEQUALS(">="),
	OP_NEQUALS("!="),
	OP_PLUS("±"),
	OP_MINUS("!±"),
	OP_MUL("x"),
	OP_DIV("/"),
	OP_GREATER(">"),
	OP_LOWER("<"),
	OP_MOD("%"),
	OP_NOT("!"),
	OP_POWER("^"),
	OP_SHR(">>"),
	OP_SHL("<<"),
	
	;
	
	public final String syntax;
	
	private TokenBase(String syntax) {
		this.syntax = syntax;
	}
	
}
