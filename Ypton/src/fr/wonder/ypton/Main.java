package fr.wonder.ypton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {
	
	private static final boolean NICE_ERRORS = false;
	
	public static void main(String[] args) throws IOException {
		try (InputStream is = new FileInputStream(new File("test.yp"));
				OutputStream os = new FileOutputStream(new File("out.py"))) {
			String[] lines = new String(is.readAllBytes()).replaceAll("\r", "").split("\n");
			compile(lines, os);
		}
	}
	
	private static void compile(String[] lines, OutputStream os) throws IOException {
		os.write("printedOnce=False\ndef printOnce(*x, **kwargs):\n  global printedOnce\n  if not printedOnce:\n    printedOnce = True\n    print(*x, **kwargs)\n".getBytes());
		
		int maxIndentation = 0;
		int[] indentations = new int[lines.length];
		for(int l = 0; l < lines.length; l++) {
			String line = lines[l];
			try {
				int indentation = getIndentation(line);
				lines[l] = line = line.stripLeading();
				indentations[l] = indentation;
				maxIndentation = Math.max(indentation, maxIndentation);
			} catch (CompilationException e) {
				printError(lines, l, e);
			}
		}
		Context context = new Context();
		for(int l = 0; l < lines.length; l++) {
			String line = lines[l];
			try {
				int trueIndentation = maxIndentation - indentations[l];
				Token[] tokens = Tokenizer.tokenize(line);
				reworkTokens(tokens, context);
				os.write("  ".repeat(l == 0 ? 0 : Math.max(trueIndentation, maxIndentation - indentations[l-1])).getBytes());
				os.write(("print(\"" + RANDOM_LINE_INFO[(int)(Math.random()*RANDOM_LINE_INFO.length)] + l + "\")").getBytes());
				os.write('\n');
				os.write("  ".repeat(trueIndentation).getBytes());
				for(Token t : tokens)
					os.write((translateToken(t, context) + " ").getBytes());
				os.write('\n');
			} catch (CompilationException e) {
				printError(lines, l, e);
			}
		}
	}
	
	private static void printError(String[] lines, int errorLine, CompilationException e) {
		if(NICE_ERRORS) {
			System.err.println("line\n" + lines[errorLine] + "\ncaused an error:");
			e.printStackTrace();
		} else {
			if(lines.length <= 1)
				System.err.println("Wow! You managed to make a mistake with less than 2 lines!");
			else
				System.err.println("There is an error in your script! And its probably not on line " + (int)(Math.random() * lines.length + 1) + "...");
		}
	}
	
	private static int getIndentation(String line) throws CompilationException {
		if(line.isEmpty())
			return 0;
		char c = line.charAt(0);
		int i = 0;
		while(line.length() > i && line.charAt(i) == c)
			i++;
		if(c == ' ') {
			if(i % 2 != 0)
				throw new CompilationException("Invalid indentation");
			return i/2;
		} else if(c == '\t') {
			return i;
		}
		return 0;
	}
	
	private static void reworkTokens(Token[] tokens, Context context) throws CompilationException {
		for(Token t : tokens) {
			if(t.base.name().startsWith("KW")) {
				for(int i = 0; i < t.text.length(); i++) {
					if(Character.isUpperCase(t.text.charAt(i)) != context.keywordMixedCaseIsUpper)
						throw new CompilationException("Mixed case not respected");
					context.keywordMixedCaseIsUpper = !context.keywordMixedCaseIsUpper;
				}
			}
		}
		
		for(Token t : tokens)
			if(t.base == TokenBase.VAR_VARIABLE)
				t.text = t.text.substring(2); // remove $$
		
		if(tokens[0].base == TokenBase.VAR_VARIABLE) {
			context.variables.add(tokens[0].text);
		}
		
		for(int i = 1; i < tokens.length; i++) {
			if((tokens[i].base == TokenBase.VAR_VARIABLE || tokens[i].base == TokenBase.VAR_RAW_VAR) && tokens[i-1].base == TokenBase.TK_PARENTHESIS_CLOSE) {
				int idx = 0;
				while(tokens[idx] != tokens[i-1].sectionPair)
					idx++;
				// shift idx..i
				Token t = tokens[i];
				for(int j = i; j > idx; j--)
					tokens[j] = tokens[j-1];
				tokens[idx] = t;
			}
		}
		
		for(Token t : tokens) {
			if(t.base == TokenBase.VAR_RAW_VAR && t.text.equals("System.Kernel.cout.ifopened.writeAndCloseImediatelyAfter"))
				t.text = "printOnce";
		}
		
		if(tokens[tokens.length-1].base != TokenBase.TK_LINE_BREAK && tokens[tokens.length-1].base != TokenBase.TK_LINE_BREAK_NOT)
			throw new CompilationException("Missing a line break");
	}
	
	private static String translateToken(Token t, Context context) throws CompilationException {
		switch(t.base) {
		case VAR_VARIABLE: {
			List<String> matchingVariables = new ArrayList<>();
			for(String v : context.variables)
				if(v.matches(t.text))
					matchingVariables.add(v);
			if(matchingVariables.size() != 1)
				throw new CompilationException(matchingVariables.size() + " matching variables for " + t.text + ": " + String.join(",", matchingVariables));
			return matchingVariables.get(0);
		}
		case VAR_RAW_VAR:
			return t.text;
		case KW_EQUAL:
		case KW_EQUAL_DIV:
		case KW_EQUAL_MINUS:
		case KW_EQUAL_MOD:
		case KW_EQUAL_MUL:
		case KW_EQUAL_PLUS:
			return t.text;
		case KW_DEF: return "def";
		case KW_FOR: return "for";
		case KW_IFNT: return "if";
		case KW_OTHERWISE: return "else";
		case KW_RETURN: return "return";
		case KW_WHILE: return "while";
		case LIT_BOOL_FALSE: return "False";
		case LIT_BOOL_TRUE: return "True";
		case LIT_FLOAT:
			return t.text;
		case LIT_INT:
			return "" + Integer.parseInt(t.text, 7);
		case LIT_STR:
			return rawCaesar(t.text, t.text.length());
		case OP_DIV:
		case OP_EQUALS:
		case OP_GEQUALS:
		case OP_GREATER:
		case OP_LEQUALS:
		case OP_LOWER:
		case OP_NEQUALS:
		case OP_MOD:
		case OP_SHL:
		case OP_SHR:
			return t.text;
		case OP_MINUS: return "-";
		case OP_MUL: return "x";
		case OP_NOT: return "not";
		case OP_PLUS: return "+";
		case OP_POWER: return "**";
		case TK_ARROW: return ".";
		case TK_APOSTROPHE:
		case TK_BACK_APOSTROPHE:
		case TK_DOUBLE_QUOTE:
			throw new CompilationException("? " + t);
		case TK_LINE_BREAK: return "";
		case TK_LINE_BREAK_NOT: return ":";
		case TK_BRACE_CLOSE:
		case TK_BRACE_OPEN:
		case TK_BRACKET_CLOSE:
		case TK_BRACKET_OPEN:
		case TK_COMMA:
		case TK_PARENTHESIS_CLOSE:
		case TK_PARENTHESIS_OPEN:
		case TK_SPACE:
			return t.text;
		}
		throw new CompilationException("?? " + t);
	}
	
	private static final String[] RANDOM_LINE_INFO = {
		">",
		">>>",
		"Hello from line ",
		"No bug until ",
		"",
		"here ",
		"im at ",
		"foo ",
		"running ",
		"working ",
		"? ",
		"line ",
		"l",
		"l ",
	};
	
	private static String rawCaesar(String text, int offset) {
		StringBuilder sb = new StringBuilder();
		sb.append('"');
		for(char c : text.toCharArray())
			sb.append("\\x" + Integer.toString(c+offset, 16));
		sb.append('"');
		return sb.toString();
	}
	
}

class CompilationException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public CompilationException(String message) {
		super(message);
	}
	
}

class Context {
	
	boolean keywordMixedCaseIsUpper = false;
	List<String> variables = new ArrayList<>();
	
}