package uk.co.terminological.parser;

import java.io.Reader;
import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.terminological.parser.Tokens.*;

public class TokeniserTest {
	
	public static void main(String[] args) {
		new TokeniserTest().testTokeniser();
		new TokeniserTest().testTokeniser2();
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTokeniser() {
		Tokeniser tsv = new Tokeniser(
			new EOL("\n"),
			new SEP("\t")
				);
		
		Reader r = new StringReader(
			"the\tcat\tsat\ton\tthe\tmat\n" +
			"the\tcat\tsat\ton\tthe\tmat\n"
		);
		
		for (Token token: tsv.iterable(r)) {
			System.out.print(token.toString());
		}
		
		
	}

	@Test
	public void testTokeniser2() {
		Tokeniser tsv = new Tokeniser(
			new EOL("\r\n"),
			new EOL("\n"),
			new EOL("\r"),
			new SEP(","),
			new ENC_ESC("\"")
				);
		
		Reader r = new StringReader(
			"the,cat,sat,on,the,mat\r\n" +
			"the,\"cat,sat\"on,the,mat\n"
		);
		
		for (Token token: tsv.iterable(r)) {
			System.out.print(token.toString());
		}
	}
	
}
