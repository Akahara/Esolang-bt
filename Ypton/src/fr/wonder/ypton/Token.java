package fr.wonder.ypton;

public class Token {
	
	public final TokenBase base;
	public String text;
	public Token sectionPair;
	
	public Token(TokenBase base, String text) {
		this.base = base;
		this.text = text;
	}
	
	public void linkSectionPair(Token pair) {
		this.sectionPair = pair;
		pair.sectionPair = this;
	}
	
	@Override
	public String toString() {
		return text+"("+base+")";
	}

}
