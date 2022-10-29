package fr.wonder.ypton;

import static fr.wonder.ypton.TokenBase.*;

import java.util.LinkedList;
import java.util.List;

public class Tokenizer {
	
	private final String source;
	
	private final List<Token> tokens = new LinkedList<>();
	private final LinkedList<SectionToken> openedSections = new LinkedList<>();
	private final LinkedList<Token> openedSectionTokens = new LinkedList<>();
	
	private Tokenizer(String line) {
		this.source = line;
	}
	
	private void tokenize() throws CompilationException {
		TokenBase quoteEnd = null;
		boolean quoteIsComment = false;
		int quoteBeginPosition = -1;
		
		TokenBase latestOpenedSection = null;
		
		int currentTokenBegin = 0;
		
		for(int i = 0; i < source.length(); ) {
			// read a text character
			if(quoteEnd != null) {
				if(source.charAt(i) == '\\') {
					if(i == source.length()-1)
						throw new CompilationException("Unexpected source end");
					i += 2;
				} else {
					if(source.startsWith(quoteEnd.syntax, i)) {
						if(!quoteIsComment) {
							int ss = quoteBeginPosition;
							int st = i;
							if(quoteEnd == TK_BACK_APOSTROPHE)
								tokens.add(new Token(VAR_RAW_VAR, source.substring(ss, st)));
							else
								tokens.add(new Token(LIT_STR, source.substring(ss, st)));
						}
						i += quoteEnd.syntax.length();
						quoteEnd = null;
						currentTokenBegin = i;
					} else {
						// advance by 1 text character
						i++;
					}
				}
				continue;
			}
			
			// check if the section closes
			if(latestOpenedSection != null && source.startsWith(latestOpenedSection.syntax, i)) {
				readNonSectionToken(currentTokenBegin, i);
				
				int ss = i;
				int st = i+latestOpenedSection.syntax.length();
				Token t = new Token(latestOpenedSection, source.substring(ss, st));
				t.linkSectionPair(openedSectionTokens.getLast());
				tokens.add(t);
				openedSections.removeLast();
				openedSectionTokens.removeLast();
				
				i += latestOpenedSection.syntax.length();
				latestOpenedSection = getLatestOpenedSection();
				currentTokenBegin = i;
				continue;
			}
			
			// check for a section begin
			SectionToken del = getMatchingSection(i);
			if(del != null) {
				readNonSectionToken(currentTokenBegin, i);
				
				int delLen = del.start.syntax.length();
				int stop = i+delLen;
				if(del.repeatable)
					while(source.startsWith(del.start.syntax, stop))
						stop += delLen;
				if(del.quote) {
					quoteEnd = del.stop;
					quoteBeginPosition = stop;
					quoteIsComment = false;
				} else {
					Token t = new Token(del.start, source.substring(i, stop));
					tokens.add(t);
					if(del.stop != null) {
						openedSections.add(del);
						openedSectionTokens.add(t);
						latestOpenedSection = del.stop;
					}
				}
				
				i = stop;
				currentTokenBegin = i;
				continue;
			}
			
			// no token could be read, advance by 1
			i++;
		}
		
		// read the last non section token
		readNonSectionToken(currentTokenBegin, source.length());
		
		// check unclosed sections
		if(!openedSections.isEmpty()) {
			throw new CompilationException("Unclosed section");
		} else if(quoteEnd != null) {
			throw new CompilationException("Unclosed text section");
		}
		
	}
	
	private TokenBase getLatestOpenedSection() {
		if(openedSections.isEmpty())
			return null;
		return openedSections.getLast().stop;
	}
	
	private SectionToken getMatchingSection(int loc) {
		for(SectionToken del : Tokens.SECTIONS) {
			if(source.startsWith(del.start.syntax, loc)) {
				return del;
			}
		}
		return null;
	}
	
	private void readNonSectionToken(int begin, int end) throws CompilationException {
		if(begin == end)
			return;
		String text = source.substring(begin, end);
		TokenBase b = getBase(text);
		if(b == null) {
			throw new CompilationException("Unresolved token in [" + text + "]");
		} else {
			tokens.add(new Token(b, text));
		}
	}
	
	private static TokenBase getBase(String split) {
		for(TokenBase b : Tokens.BASES)
			if(split.matches(b.syntax))
				return b;
		return null;
	}
	
	private void finalizeTokens() {
		// remove spaces
		tokens.removeIf(t -> t.base == TokenBase.TK_SPACE);
		
		for(int i = 1; i < tokens.size(); i++) {
			// TODO rework the tokens finalization
			// previous, current and next tokens
			Token ptk = tokens.get(i-1);
			Token tk = tokens.get(i);
			Token ntk = i+1 < tokens.size() ? tokens.get(i+1) : null;
			
			if(tk.base == TK_ARROW) {
				// replace <(int) . int> and <int . (int)> by <float>
				String floatText = "";
				boolean isFloat = false;
				if(ptk.base == LIT_INT) {
					floatText = ptk.text;
					tokens.remove(i-1);
					i--;
					isFloat = true;
				}
				floatText += ".";
				if(ntk != null && ntk.base == LIT_INT) {
					floatText += ntk.text;
					tokens.remove(i+1);
					isFloat = true;
				}
				if(isFloat) {
					tokens.set(i, new Token(LIT_FLOAT, floatText));
				}
			}
		}
	}

	public static Token[] tokenize(String source) throws CompilationException {
		Tokenizer instance = new Tokenizer(source);
		instance.tokenize();
		instance.finalizeTokens();
		return instance.tokens.toArray(Token[]::new);
	}
	
}
