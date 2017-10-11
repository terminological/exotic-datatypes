package uk.co.terminological.parser;

import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.datatypes.Deferred;
import uk.co.terminological.parser.StateMachineExecutor.ErrorHandler;
import uk.co.terminological.parser.Tokens.ENC;
import uk.co.terminological.parser.Tokens.ENC_ESC;
import uk.co.terminological.parser.Tokens.EOL;
import uk.co.terminological.parser.Tokens.ESC;
import uk.co.terminological.parser.Tokens.SEP;

public class DelimitedParserBuilder {

	StateMachine machine;
	ErrorHandler handler = ErrorHandler.DEBUG;
	Token seperator = new SEP(",");
	Token[] eol = new Token[] {new EOL("\r\n"),new EOL("\n"),new EOL("\r")};
	Token[] enc_esc = new Token[] {};
	
	static Logger log = LoggerFactory.getLogger(DelimitedParserBuilder.class);
	
	//initial config options
	public static DelimitedParserBuilder machine(String seperator, String enclosedBy, String escapedWith) {
		if (!enclosedBy.equals(escapedWith)) {
			DelimitedParserBuilder out = new DelimitedParserBuilder();
			out.enc_esc = new Token[] {new ENC(enclosedBy), new ESC(escapedWith)};
			out.machine = DelimitedParserMachine.OPTIONALLY_ENCLOSED_CSV;
			return out;
		} else {
			return machine(seperator, enclosedBy);
		}
	}
	
	public static DelimitedParserBuilder machine(String seperator, String enclosedBy) {
		DelimitedParserBuilder out = new DelimitedParserBuilder();
		out.enc_esc = new Token[] {new ENC_ESC(enclosedBy)};
		out.seperator = new SEP(seperator);
		out.machine = DelimitedParserMachine.EXCEL_CSV;
		return out;
	}
	
	public static DelimitedParserBuilder machine(String seperator) {
		DelimitedParserBuilder out = new DelimitedParserBuilder();
		out.enc_esc = new Token[] {};
		out.seperator = new SEP(seperator);
		out.machine = DelimitedParserMachine.IANA_TSV;
		return out;
	}
	
	public static DelimitedParserBuilder machine() {
		DelimitedParserBuilder out = new DelimitedParserBuilder();
		out.enc_esc = new Token[] {};
		out.machine = DelimitedParserMachine.IANA_TSV;
		return out;
	}
	
	public DelimitedParserBuilder mandatoryEnclosure() {
		if (machine.equals(DelimitedParserMachine.OPTIONALLY_ENCLOSED_CSV)) {
			machine = DelimitedParserMachine.MANDATORY_ENCLOSED_CSV;
		} else if (machine.equals(DelimitedParserMachine.EXCEL_CSV)) {
			machine = DelimitedParserMachine.ENCLOSED_EXCEL_CSV;
		} else {
			log.debug("Cannot enforce enclosure in IANA_TSV parser");
		}
		return this;
	}
	
	//Fluent configuration
	public DelimitedParserBuilder lineEnding(String lineEnding) {
		this.eol = new Token[] { new EOL(lineEnding) };
		return this;
	}
	
	public DelimitedParserBuilder errorHandler(ErrorHandler handler) {
		this.handler = handler;
		return this;
	}
	
	public DelimitedParserBuilder strict() {
		this.errorHandler(ErrorHandler.STRICT);
		return this;
	}
	
	//builder
	public DelimitedParserExecutor build(Reader in) {
		Token[] tmp = Stream.concat(
						Stream.concat(
								Stream.of(seperator),
								Arrays.stream(eol)), 
						Arrays.stream(enc_esc))
                .toArray(Token[]::new);
		Tokeniser lex = new Tokeniser(tmp);
		Iterator<Token> input = lex.iterator(in);
		return new DelimitedParserExecutor(machine,input);
	}
	
	//Common configuration options
	public static Iterable<Deferred<List<String>,StateMachineException>> excelCsv(Reader in) {
		return new Iterable<Deferred<List<String>,StateMachineException>>() {
			public Iterator<Deferred<List<String>,StateMachineException>> iterator() {
				return machine(",","\"").lineEnding("\r\n").build(in);
			}
		};
	}
	
	public static Iterable<Deferred<List<String>,StateMachineException>> tsv(Reader in) {
		return new Iterable<Deferred<List<String>,StateMachineException>>() {
			public Iterator<Deferred<List<String>,StateMachineException>> iterator() {
				return machine("\t").build(in);
			}
		};
	}
	
	public static Iterable<Deferred<List<String>,StateMachineException>> pipe(Reader in) {
		return new Iterable<Deferred<List<String>,StateMachineException>>() {
			public Iterator<Deferred<List<String>,StateMachineException>> iterator() {
				return machine("|").build(in);
			}
		};
	}
	
	public static Iterable<Deferred<List<String>,StateMachineException>> mysql(Reader in) {
		return new Iterable<Deferred<List<String>,StateMachineException>>() {
			public Iterator<Deferred<List<String>,StateMachineException>> iterator() {
				return machine(",","\"","\\").build(in);
			}
		};
	}

	
}
