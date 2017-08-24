// $ANTLR 2.7.4: "json.g" -> "JSONParserAntlr.java"$

package com.sdicons.json.parser.impl;
import com.sdicons.json.model.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

public class JSONParserAntlr extends antlr.LLkParser       implements JSONParserAntlrTokenTypes
 {

protected JSONParserAntlr(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public JSONParserAntlr(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected JSONParserAntlr(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public JSONParserAntlr(TokenStream lexer) {
  this(lexer,1);
}

public JSONParserAntlr(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

	public final JSONValue  value(
		String aStreamName
	) throws RecognitionException, TokenStreamException {
		JSONValue val=JSONNull.NULL;
		
		
		switch ( LA(1)) {
		case LCURLY:
		{
			val=object(aStreamName);
			break;
		}
		case LBRACK:
		{
			val=array(aStreamName);
			break;
		}
		case TRUE:
		case FALSE:
		case NULL:
		case STRING:
		case NUMBER:
		{
			val=atomic(aStreamName);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return val;
	}
	
	public final JSONObject  object(
		String aStreamName
	) throws RecognitionException, TokenStreamException {
		JSONObject lResult= new JSONObject();
		
		Token  begin = null;
		Token  i = null;
		Token  j = null;
		
		JSONValue lVal;
		begin = LT(1);
		match(LCURLY);
		lResult.setLineCol(begin.getLine(), begin.getColumn()); lResult.setStreamName(aStreamName);
		{
		switch ( LA(1)) {
		case STRING:
		{
			i = LT(1);
			match(STRING);
			match(COLON);
			lVal=value(aStreamName);
			lResult.getValue().put(i.getText(), lVal);
			{
			_loop6:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					j = LT(1);
					match(STRING);
					match(COLON);
					lVal=value(aStreamName);
					lResult.getValue().put(j.getText(), lVal);
				}
				else {
					break _loop6;
				}
				
			} while (true);
			}
			break;
		}
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(RCURLY);
		return lResult;
	}
	
	public final JSONArray  array(
		String aStreamName
	) throws RecognitionException, TokenStreamException {
		JSONArray lResult=new JSONArray();
		
		Token  begin = null;
		
		JSONValue lVal;
		begin = LT(1);
		match(LBRACK);
		lResult.setLineCol(begin.getLine(), begin.getColumn()); lResult.setStreamName(aStreamName);
		{
		switch ( LA(1)) {
		case TRUE:
		case FALSE:
		case NULL:
		case STRING:
		case NUMBER:
		case LCURLY:
		case LBRACK:
		{
			lVal=value(aStreamName);
			lResult.getValue().add(lVal);
			{
			_loop10:
			do {
				if ((LA(1)==COMMA)) {
					match(COMMA);
					lVal=value(aStreamName);
					lResult.getValue().add(lVal);
				}
				else {
					break _loop10;
				}
				
			} while (true);
			}
			break;
		}
		case RBRACK:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		match(RBRACK);
		return lResult;
	}
	
	public final JSONValue  atomic(
		String aStreamName
	) throws RecognitionException, TokenStreamException {
		JSONValue val=JSONNull.NULL;
		
		Token  t = null;
		Token  f = null;
		Token  n = null;
		Token  str = null;
		Token  num = null;
		
		switch ( LA(1)) {
		case TRUE:
		{
			t = LT(1);
			match(TRUE);
			val = new JSONBoolean(true); val.setLineCol(t.getLine(), t.getColumn()); val.setStreamName(aStreamName);
			break;
		}
		case FALSE:
		{
			f = LT(1);
			match(FALSE);
			val = new JSONBoolean(false);val.setLineCol(f.getLine(), f.getColumn()); val.setStreamName(aStreamName);
			break;
		}
		case NULL:
		{
			n = LT(1);
			match(NULL);
			val.setLineCol(n.getLine(), n.getColumn()); val.setStreamName(aStreamName);
			break;
		}
		case STRING:
		{
			str = LT(1);
			match(STRING);
			val = new JSONString(str.getText());val.setLineCol(str.getLine(), str.getColumn()); val.setStreamName(aStreamName);
			break;
		}
		case NUMBER:
		{
			num = LT(1);
			match(NUMBER);
			String lTxt = num.getText().toLowerCase();
			if(lTxt.indexOf('e')>=0 || lTxt.indexOf('.')>=0) val = new JSONDecimal(new BigDecimal(lTxt));
			else val = new JSONInteger(new BigInteger(lTxt));
			val.setLineCol(num.getLine(), num.getColumn());
			val.setStreamName(aStreamName);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		return val;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"TRUE",
		"FALSE",
		"NULL",
		"STRING",
		"NUMBER",
		"LCURLY",
		"COLON",
		"COMMA",
		"RCURLY",
		"LBRACK",
		"RBRACK",
		"LPAREN",
		"RPAREN",
		"QUOTES",
		"ESC",
		"HEX_DIGIT",
		"ZERO",
		"NONZERO",
		"DIGIT",
		"INTEGER",
		"EXPONENT",
		"WS",
		"SL_COMMENT"
	};
	
	
	}
