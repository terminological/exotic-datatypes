package uk.co.terminological.parser;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.terminological.parser.Matcher.Match;

public class MatcherTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDefault() {
		
		Matcher m = new Matcher.Default();
		assertTrue(m.consume('a'));
		assertTrue(m.state().equals(Match.SUCCEEDED));
		assertTrue(m.token().get()[0] == 'a');
		
	}

	@Test
	public void testOfToken() {
		
		Matcher m = new Matcher.OfToken(new Tokens.CharSequenceToken("abcdef"));
		assertTrue(m.consume('a'));
		assertTrue(m.state().equals(Match.IN_PROGRESS));
		assertTrue(m.token().get()[0] == 'a');
		assertTrue(m.consume('b'));
		assertTrue(m.consume('c'));
		assertTrue(m.consume('d'));
		assertTrue(m.consume('e'));
		assertTrue(m.consume('f'));
		assertTrue(m.state().equals(Match.SUCCEEDED));
		
		m = new Matcher.OfToken(new Tokens.CharSequenceToken("abcdef"));
		assertTrue(m.consume('a'));
		assertTrue(m.state().equals(Match.IN_PROGRESS));
		assertTrue(m.token().get()[0] == 'a');
		assertTrue(m.consume('b'));
		assertTrue(m.consume('c'));
		assertTrue(m.consume('d'));
		assertFalse(m.consume('x'));
		assertFalse(m.consume('f'));
		assertTrue(m.state().equals(Match.FAIL));
		
	}
	
}
