package uk.co.terminological.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.parser.Tokens.ENC;
import uk.co.terminological.parser.Tokens.EOF;
import uk.co.terminological.parser.Tokens.EOL;
import uk.co.terminological.parser.Tokens.ESC;
import uk.co.terminological.parser.Tokens.SEP;
import uk.co.terminological.parser.Tokens.ENC_ESC;;

/**
 * A utility for reading delimited files such as CSV, TSV or pipe delimited files.
 * This can handle different separators, line endings, and mandatory or optional enclosure
 * of the fields, with escaping within enclosed fields. Usual configurations are provided by the static factory 
 * methods or you can roll your own with the constructors. This can handle multi character delimiters and terminators
 * @author terminological
 *
 */
public class DelimitedParser {

	Tokeniser lex;
	StateMachine machine;
	Reader reader;
	static Logger log = LoggerFactory.getLogger(DelimitedParser.class);
	
	public static DelimitedParser enclosedWindowsCsv(Reader in) {
		return new DelimitedParser(in,",","\r\n","\"","\"",true);
	}

	public static DelimitedParser windowsCsv(Reader in) {
		return new DelimitedParser(in,",","\r\n","\"","\"",false);
	}
	
	public static DelimitedParser csv(Reader in) {
		return new DelimitedParser(in,",","\n","\"","\"",false);
	}
	
	public static DelimitedParser enclosedCsv(Reader in) {
		return new DelimitedParser(in,",","\n","\"","\"",true);
	}

	public static DelimitedParser windowsTsv(Reader in) {
		return new DelimitedParser(in,"\t","\r\n");
	}
	
	public static DelimitedParser tsv(Reader in) {
		return new DelimitedParser(in,"\t","\n");
	}

	public static DelimitedParser windowsPipe(Reader in) {
		return new DelimitedParser(in,"|","\r\n");
	}
	
	public static DelimitedParser pipe(Reader in) {
		return new DelimitedParser(in,"|","\n");
	}
	
	/**
	 * Creates a delimited file parser with custom options which has the same escape character as enclosure and enclosure is optional.
	 * This is a common configuration for e.g. csv from MS Excel.
	 * @param reader - a reader providing input
	 * @param sep - the separator - eg. ","
	 * @param term - the terminator - eg. "\r\n"
	 * @param enc - the enclosing characters - eg. "\""
	 */
	
	public DelimitedParser(Reader reader, String sep, String term, String enc) {
		this(reader,sep,term,enc,enc,false);
	}

	/**
	 * Creates a delimited file parser with custom options which fields are not enclosed or escaped, and delimiters are disallowed within fields.
	 * This is a common configuration for e.g. tsv or pipe seperated
	 * @param reader - a reader providing input
	 * @param sep - the separator - eg. ","
	 * @param term - the terminator - eg. "\r\n"
	 */
	public DelimitedParser(Reader reader, String sep, String term) {
		this.reader = reader;
		this.lex = new Tokeniser(new SEP(sep),new EOL(term));
		// No enclosures
		this.machine = IANA_TSV;
	}
	
	/**
	 * Creates a delimited file parser with custom options
	 * @param reader - a reader providing input
	 * @param sep - the separator - eg. ","
	 * @param term - the terminator - eg. "\r\n"
	 * @param enc - the enclosing characters - eg. "\""
	 * @param escape - the escaping characters - e.g. "\\"
	 * @param enclosedMandatory - defines whether fields are always enclosed or not
	 */
	public DelimitedParser(Reader reader, String sep, String term, String enc, String escape, boolean enclosedMandatory) {
		this.reader = reader;
		if (enc.equals(escape)) {
			this.lex = new Tokeniser(new SEP(sep), new EOL(term), new ENC_ESC(escape));
			this.machine = EXCEL_CSV;
		} else {
			this.lex = new Tokeniser(new ENC(enc),new SEP(sep), new EOL(term), new ESC(escape));
			if (!enclosedMandatory) {
				this.machine = OPTIONALLY_ENCLOSED_CSV;
			} else {
				this.machine = MANDATORY_ENCLOSED_CSV;
			}
		}
	}

	public DelimitedParser(Reader reader, String sep, String term, String enc, String escape) {
		this(reader,sep,term,enc,escape,false);
	}
	
	/**
	 * State machine for clean TSV type files where separator and EOL is forbidden within field
	 * and there is no escaping. only SEP, EOL and EOF tokens are expected. Also pipe separated
	 */
	public static StateMachine IANA_TSV = StateMachine	
		.inState(States.LINE_TERMINATED)
		.withTransition(States.FIELD_TERMINATED, Token.isType(SEP.class), States.FIELD_TERMINATED)
		.withTransition(States.FIELD_TERMINATED, t->t instanceof EOL, States.LINE_TERMINATED)
		.withTransition(States.FIELD_TERMINATED, Token.isType(EOF.class), States.LINE_TERMINATED)
		.withTransition(States.FIELD_TERMINATED, t->true, States.READING_UNENCLOSED)

		.withTransition(States.READING_UNENCLOSED, Token.isType(SEP.class), States.FIELD_TERMINATED)
		.withTransition(States.READING_UNENCLOSED, Token.isType(EOL.class), States.LINE_TERMINATED)
		.withTransition(States.READING_UNENCLOSED, t->true, States.READING_UNENCLOSED)

		.withTransition(States.LINE_TERMINATED, Token.isType(SEP.class), States.FIELD_TERMINATED)
		.withTransition(States.LINE_TERMINATED, Token.isType(EOL.class), States.LINE_TERMINATED)
		.withTransition(States.LINE_TERMINATED, Token.isType(EOF.class), States.FILE_TERMINATED)
		.withTransition(States.LINE_TERMINATED, t->true, States.READING_UNENCLOSED)
		
		.withTransition(States.FILE_TERMINATED, t->true, State.stopped());
	;
	
	/**
	 * A state machine typical of csv that optionally encloses fields and uses different escape character.
	 * This machine uses tokens, ENC, SEP, EOL, EOF, and ESC 
	 */
	public static StateMachine OPTIONALLY_ENCLOSED_CSV = StateMachine
			.inState(States.LINE_TERMINATED)

			.withTransition(States.FIELD_TERMINATED, Token.isType(ENC.class), States.ENCLOSING_FIELD)
			.withTransition(States.FIELD_TERMINATED, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.FIELD_TERMINATED, Token.isType(EOL.class), States.LINE_TERMINATED)
			.withTransition(States.FIELD_TERMINATED, Token.isType(EOF.class), States.FILE_TERMINATED)
			.withTransition(States.FIELD_TERMINATED, t->true, States.READING_UNENCLOSED)

			.withTransition(States.READING_UNENCLOSED, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.READING_UNENCLOSED, Token.isOneOf(EOL.class, EOF.class), States.LINE_TERMINATED)
			.withTransition(States.READING_UNENCLOSED, t->true, States.READING_UNENCLOSED)

			.withTransition(States.ENCLOSING_FIELD, Token.isType(ESC.class), States.ESCAPING_ENCLOSED)
			.withTransition(States.ENCLOSING_FIELD, Token.isType(ENC.class), States.UNENCLOSING_FIELD)
			.withTransition(States.ENCLOSING_FIELD, t->true, States.READING_ENCLOSED)

			.withTransition(States.READING_ENCLOSED, Token.isType(ESC.class), States.ESCAPING_ENCLOSED)
			.withTransition(States.READING_ENCLOSED, Token.isType(ENC.class), States.UNENCLOSING_FIELD)
			.withTransition(States.READING_ENCLOSED, t->true, States.READING_ENCLOSED)

			.withTransition(States.ESCAPING_ENCLOSED, t->true, States.READING_ENCLOSED)

			.withTransition(States.UNENCLOSING_FIELD, Token.isType(EOL.class), States.LINE_TERMINATED)
			.withTransition(States.UNENCLOSING_FIELD, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.UNENCLOSING_FIELD, t->true, State.error("Enclosed field not immediately followed by seperator or EOL"))

			.withTransition(States.LINE_TERMINATED, Token.isType(ENC.class), States.ENCLOSING_FIELD)
			.withTransition(States.LINE_TERMINATED, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.LINE_TERMINATED, Token.isType(EOL.class), States.LINE_TERMINATED)
			.withTransition(States.LINE_TERMINATED, Token.isType(EOF.class), States.FILE_TERMINATED)
			.withTransition(States.LINE_TERMINATED, t->true, States.READING_UNENCLOSED)
	
			.withTransition(States.FILE_TERMINATED, t->true, State.stopped());
	
	/**
	 * A state machine for files which enclose all their fields and escape the enclosure with a different
	 * character. Mysql can behave like this depending on options. e.g.  "he said, \"This is csv\""
	 */
	public static StateMachine MANDATORY_ENCLOSED_CSV = StateMachine
			.inState(States.LINE_TERMINATED)

			.withTransition(States.FIELD_TERMINATED, Token.isType(ENC.class), States.ENCLOSING_FIELD)
			.withTransition(States.FIELD_TERMINATED, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.FIELD_TERMINATED, Token.isType(EOL.class), States.LINE_TERMINATED)
			.withTransition(States.FIELD_TERMINATED, Token.isType(EOF.class), States.FILE_TERMINATED)
			.withTransition(States.FIELD_TERMINATED, t->true, State.error(""))

			.withTransition(States.ENCLOSING_FIELD, Token.isType(ESC.class), States.ESCAPING_ENCLOSED)
			.withTransition(States.ENCLOSING_FIELD, t->true, States.READING_ENCLOSED)

			.withTransition(States.READING_ENCLOSED, Token.isType(ESC.class), States.ESCAPING_ENCLOSED)
			.withTransition(States.READING_ENCLOSED, Token.isType(ENC.class), States.UNENCLOSING_FIELD)
			.withTransition(States.READING_ENCLOSED, t->true, States.READING_ENCLOSED)

			.withTransition(States.ESCAPING_ENCLOSED, t->true, States.READING_ENCLOSED)

			.withTransition(States.UNENCLOSING_FIELD, Token.isType(EOL.class), States.LINE_TERMINATED)
			.withTransition(States.UNENCLOSING_FIELD, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.UNENCLOSING_FIELD, Token.isType(EOF.class), States.FILE_TERMINATED)
			.withTransition(States.UNENCLOSING_FIELD, t->true, State.error("Unexpected input after enclosed field"))

			.withTransition(States.LINE_TERMINATED, Token.isType(ENC.class), States.ENCLOSING_FIELD)
			.withTransition(States.LINE_TERMINATED, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.LINE_TERMINATED, Token.isType(EOL.class), States.LINE_TERMINATED)
			.withTransition(States.LINE_TERMINATED, Token.isType(EOF.class), States.FILE_TERMINATED)
			.withTransition(States.LINE_TERMINATED, t->true, State.error("Unexpected input at beginning of line"))
			
			.withTransition(States.FILE_TERMINATED, t->true, State.stopped());

	/**
	 * The industry default where csv is optionally enclosed with (usually quotes) and quotes are escaped with more
	 * quotes so that fields can end up like "he said, ""This is csv""" 
	 */
	public static StateMachine EXCEL_CSV = StateMachine
			.inState(States.LINE_TERMINATED)

			.withTransition(States.FIELD_TERMINATED, Token.isType(ENC_ESC.class), States.ENCLOSING_FIELD)
			.withTransition(States.FIELD_TERMINATED, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.FIELD_TERMINATED, Token.isType(EOL.class), States.LINE_TERMINATED)
			.withTransition(States.FIELD_TERMINATED, Token.isType(EOF.class), States.FILE_TERMINATED)
			.withTransition(States.FIELD_TERMINATED, t->true, States.READING_UNENCLOSED)

			.withTransition(States.READING_UNENCLOSED, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.READING_UNENCLOSED, Token.isOneOf(EOL.class, EOF.class), States.LINE_TERMINATED)
			.withTransition(States.READING_UNENCLOSED, t->true, States.READING_UNENCLOSED)

			.withTransition(States.ENCLOSING_FIELD, Token.isType(ENC_ESC.class), States.ESCAPING_ENCLOSED_OR_UNENCLOSING_FIELD)
			.withTransition(States.ENCLOSING_FIELD, t->true, States.READING_ENCLOSED)

			.withTransition(States.READING_ENCLOSED, Token.isType(ENC_ESC.class), States.ESCAPING_ENCLOSED_OR_UNENCLOSING_FIELD)
			.withTransition(States.READING_ENCLOSED, t->true, States.READING_ENCLOSED)

			.withTransition(States.ESCAPING_ENCLOSED_OR_UNENCLOSING_FIELD, Token.isType(ENC_ESC.class), States.READING_ENCLOSED)
			.withTransition(States.ESCAPING_ENCLOSED_OR_UNENCLOSING_FIELD, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.ESCAPING_ENCLOSED_OR_UNENCLOSING_FIELD, Token.isType(EOL.class), States.LINE_TERMINATED)
			.withTransition(States.ESCAPING_ENCLOSED_OR_UNENCLOSING_FIELD, Token.isType(EOF.class), States.FILE_TERMINATED)
			.withTransition(States.ESCAPING_ENCLOSED_OR_UNENCLOSING_FIELD, t->true, State.error("Escape sequence invalid"))

			.withTransition(States.LINE_TERMINATED, Token.isType(ENC_ESC.class), States.ENCLOSING_FIELD)
			.withTransition(States.LINE_TERMINATED, Token.isType(SEP.class), States.FIELD_TERMINATED)
			.withTransition(States.LINE_TERMINATED, Token.isType(EOL.class), States.LINE_TERMINATED)
			.withTransition(States.LINE_TERMINATED, Token.isType(EOF.class), States.FILE_TERMINATED)
			.withTransition(States.LINE_TERMINATED, t->true, States.READING_UNENCLOSED)
	
			.withTransition(States.FILE_TERMINATED, t->true, State.stopped());

	


	
	enum States implements State {
		READING_ENCLOSED,
		ESCAPING_ENCLOSED,
		ENCLOSING_FIELD,
		UNENCLOSING_FIELD,
		READING_UNENCLOSED,
		FIELD_TERMINATED,
		LINE_TERMINATED,
		FILE_TERMINATED, 
		ESCAPING_ENCLOSED_OR_UNENCLOSING_FIELD;
	}

	



}
