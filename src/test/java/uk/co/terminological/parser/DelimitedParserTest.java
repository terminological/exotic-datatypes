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
	
	public static void main(String[] args) throws StateMachineException {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		for (Deferred<List<String>, StateMachineException> line: DelimitedParserBuilder.excelCsv(r)) {
			System.out.println(line.get());
		}
	}
	
}
