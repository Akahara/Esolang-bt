package fr.wonder.ypton;

import static fr.wonder.ypton.SectionToken.SEC_BRACES;
import static fr.wonder.ypton.SectionToken.SEC_BRACKETS;
import static fr.wonder.ypton.SectionToken.SEC_PARENTHESIS;
import static fr.wonder.ypton.TokenBase.KW_DEF;
import static fr.wonder.ypton.TokenBase.KW_FOR;
import static fr.wonder.ypton.TokenBase.KW_IFNT;
import static fr.wonder.ypton.TokenBase.KW_OTHERWISE;
import static fr.wonder.ypton.TokenBase.KW_RETURN;
import static fr.wonder.ypton.TokenBase.KW_WHILE;
import static fr.wonder.ypton.TokenBase.LIT_BOOL_FALSE;
import static fr.wonder.ypton.TokenBase.LIT_BOOL_TRUE;
import static fr.wonder.ypton.TokenBase.LIT_FLOAT;
import static fr.wonder.ypton.TokenBase.LIT_INT;
import static fr.wonder.ypton.TokenBase.LIT_STR;
import static fr.wonder.ypton.TokenBase.TK_BRACE_CLOSE;
import static fr.wonder.ypton.TokenBase.TK_BRACE_OPEN;
import static fr.wonder.ypton.TokenBase.TK_LINE_BREAK;
import static fr.wonder.ypton.TokenBase.VAR_VARIABLE;

public class Tokens {
	
	public static final SectionToken[] SECTIONS = SectionToken.values();
	
	/** All of these must have different start and stop token bases */
	public static final SectionToken[] CODE_SECTIONS = {
			SEC_PARENTHESIS,
			SEC_BRACES,
			SEC_BRACKETS,
	};
	
	// the order matters for the tokenizer
	public static final TokenBase[] BASES = {
			LIT_INT, LIT_FLOAT, LIT_BOOL_TRUE,				// literals
			LIT_BOOL_FALSE, 
			KW_IFNT, KW_OTHERWISE, KW_FOR, 	// keywords
			KW_WHILE, KW_DEF, 
			KW_RETURN,
			VAR_VARIABLE, // variable elements (MUST be read last by the tokenizer)
	};
	
	public static final TokenBase[] SPLITS = {
			TK_LINE_BREAK,
			TK_BRACE_OPEN,
			TK_BRACE_CLOSE,
	};
	
	/** Keywords that can be used with/without a parenthesis header and have a body of a single line or multiple enclosed with braces */
	public static final TokenBase[] SINGLE_LINE_KEYWORDS = {
			KW_IFNT,
			KW_OTHERWISE,
			KW_FOR,
			KW_WHILE,
	};
	
	public static boolean isLiteral(TokenBase base) {
		return base == LIT_BOOL_FALSE ||
				base == LIT_BOOL_TRUE ||
				base == LIT_INT ||
				base == LIT_FLOAT ||
				base == LIT_STR;
	}
	
}
