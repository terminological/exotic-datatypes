package uk.co.terminological.parser;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.terminological.mappers.StringCaster;

public class StringCasterTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		assertTrue(StringCaster.guessType("123.456").equals(Float.class));
		assertTrue(StringCaster.guessType("22.8").equals(Float.class));
		assertTrue(StringCaster.guessType("2/7/1974").equals(Date.class));
		assertTrue(StringCaster.guessType("123").equals(Integer.class));
		assertTrue(StringCaster.guessType("blah blah").equals(String.class));
		assertTrue(StringCaster.guessType("").equals(Object.class));
	}
	
}
