package uk.co.terminological.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import uk.co.terminological.datatypes.Deferred;

public class SimpleIANAParser implements Iterator<Deferred<List<String>,ParserException>> {

	BufferedReader input;
	String line = null;
	IOException error = null;
	String seperator;

	public SimpleIANAParser(Reader input, char[] seperator) {
		this.input = new BufferedReader(input);
		this.seperator = new String(seperator);
	}

	private boolean cacheLine() {
		if (line == null) {
			try {
				String tmp = input.readLine();
				if (tmp !=null) {
					line = tmp;
					return true;
				}
				return false;		
			} catch (IOException e) {
				error = e;
				return false;
			}

		} else {
			return true;
		}
	}

	@Override
	public boolean hasNext() {
		return cacheLine();
	}

	@Override
	public Deferred<List<String>, ParserException> next() {
		if (cacheLine()) {
			List<String> value = split(line);
			line = null;
			return Deferred.value(value);
		} else {
			if (error != null) {
				throw new NoSuchElementException(error.getLocalizedMessage());
			}
			throw new NoSuchElementException();
		}
	}
	
	/**
	 * https://stackoverflow.com/questions/6374050/string-split-not-on-regular-expression
	 * @param input
	 * @param delim
	 * @return
	 */
	private List<String> split(String input) {
		List<String> l = new ArrayList<String>();
	    int offset = 0;
	    while (true) {
	        int index = input.indexOf(seperator, offset);
	        if (index == -1) {
	            l.add(input.substring(offset));
	            return l;
	        } else {
	            l.add(input.substring(offset, index));
	            offset = (index + seperator.length());
	        }
	    }
	}


}
