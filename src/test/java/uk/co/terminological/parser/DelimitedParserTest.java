package uk.co.terminological.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.co.terminological.datatypes.Deferred;

public class DelimitedParserTest {

	static Reader r = new StringReader(
			"the,cat,sat,on,the,mat\r\n" +
			"the,\"cat,sat\"on,the,mat\n"
		);
	
	static Reader tsv = new StringReader(
			"the\tcat\tsat\ton\tthe\tmat\r\n" +
			"the\tcat\tsat\ton\tthe\tmat\n"
		);
	
	public static void main(String[] args) throws ParserException {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		for (Deferred<List<String>, ParserException> line: DelimitedParserBuilder.excelCsv(r)) {
			System.out.println(line.get());
		}
		
		for (Deferred<List<String>, ParserException> line: DelimitedParserBuilder.tsv(tsv)) {
			System.out.println(line.get());
		}
		
	}
	
}
