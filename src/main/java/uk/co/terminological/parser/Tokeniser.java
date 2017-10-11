package uk.co.terminological.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.parser.Matcher.Match;
import uk.co.terminological.parser.Tokens.EOF;

/**
 * Tokeniser converts a Reader into a Iterator of Tokens
 * based on the set of supplied tokens. If an input cannot
 * be matched to a token it will return a very general Matcher.Default token.
 * 
 * 
 * @author rc538
 *
 */
class Tokeniser {

	Logger log = LoggerFactory.getLogger(Tokeniser.class);

	List<Matcher> matchers = new ArrayList<>();

	public Tokeniser(Token... tokens) {
		for (Token token: tokens) 
			matchers.add(Matcher.from(token));
		matchers.add(new Matcher.Default());
	}

	Stream<Token> stream(final Reader reader) {
		return StreamSupport.stream(iterable(reader).spliterator(), false);
	}
	
	Iterable<Token> iterable(final Reader reader) {
		return new Iterable<Token>() {
			@Override
			public Iterator<Token> iterator() {
				return Tokeniser.this.iterator(reader);
			}
		};
	}
	
	Iterator<Token> iterator(final Reader reader) {
		return new Iterator<Token>() {

			char[] buff = new char[1];
			int pos = 0;
			Queue<Token> queue = new LinkedList<>();
			boolean eofFlag = false;

			@Override
			public boolean hasNext() {
				if (queue.isEmpty() && eofFlag == false) {
					try {
						read();
					} catch (IOException e) {
						//we are not ready and never will be
					}
				}
				return !queue.isEmpty();
			}

			@Override
			public Token next() {
				// read next token if not ready
				if (hasNext()) {
					return queue.poll();
				} else {
					//This closes the iterator after an EOF token
					throw new NoSuchElementException();
				}
			}

			//if the reader is closed this will return an EOF however many times it is read.

			void read() throws IOException {

				MatchOptions possibilities = new MatchOptions();

				while( possibilities.notDetermined().orElse(true) ) {

					// if the whole options has not yet been used or it is not yet determined then feed it a character.
					if(reader.read(buff, 0, 1) == -1) {
						// add end of file token if file closed
						// if more reads are attempted on a closed file there will be a run-off of EOF tokens
						possibilities.consume(new EOF());
						eofFlag = true;
					} else {
						log.trace(pos+": "+String.valueOf(buff));
						possibilities.consume(buff[0]);
						pos += 1;
					}
				}

				queue.addAll(possibilities.selectOneMatch());
			}
		};


	}

	class MatchOptions {

		List<MatchSequence> options = null;

		//decides whether all the match options from this point down the tree have reached a conclusion 
		//at the same time. Will return true if everything has completely matched, it will return false if anything
		//is still in progress and it will return empty is nothing has yet been fed to matchers down the tree.
		//
		//alternative options are only determined if all option sequences are determined
		//if this is the case and pruning has worked they should all be of the same fixed length
		public Optional<Boolean> notDetermined() {
			boolean noneUsed = true;
			for (MatchSequence ms: options) {
				if (ms.notDetermined().isPresent()) {
					if (ms.notDetermined().get()) {
						return Optional.of(true);
					} 
					if (ms.length().get() != shortest().get()) throw new RuntimeException();
					noneUsed = false;
				} else {
					//This match sequence has not been tested yet
					//noneUsed remains true;
					//difficult to imagine situation where one matcher is used but others aren't
				}
			}
			if (noneUsed) {
				return Optional.empty();
			} else {
				return Optional.of(false);
			}
		}

		//find the shortest determined path so far, ignoring matchers that have not been used or are still IN_PROGRESS 
		public Optional<Integer> shortest() {
			Optional<Integer> shortest = Optional.empty();
			for (MatchSequence ms: options) {
				if (
						ms.notDetermined().isPresent() && //matcher has been used  
						!ms.notDetermined().get() && //matcher result is determined
						ms.length().isPresent() && //matcher has a length (will this always be true given the previous?)
						ms.length().get() < shortest.orElse(Integer.MAX_VALUE) //matchers length is shorter than the previous shortest
						) {
					shortest = ms.length();
				}
			}
			return shortest;
		}

		public boolean consume(Object o) {
			boolean oneConsumed = false;
			//what to do if options is empty list?
			Iterator<MatchSequence> optIt = options.iterator();
			while (optIt.hasNext()) {
				MatchSequence seq = optIt.next();
				// test every current matcher that is not in a fail state as an option
				oneConsumed = seq.consume(o);
				// seq.matcher.state() will now be non null
				// prune failed branches
				if (seq.matcher.state().equals(Match.FAIL)) {
					seq.alternatives = null;
					optIt.remove();
				} else {
					// Branch could be SUCCEEDED or IN_PROGRESS - in which case we just need to wait and see what happens in 
					// the next iteration - there is nothing to do for these branches apart from wait for next round
				}
			}
			
			this.pruneOptions();
			return oneConsumed;
		}

		private void pruneOptions() {
			// the alternatives can be pruned if there are determined branches that are longer than the
			// shortest determined branch.
			Iterator<MatchSequence> optIt = options.iterator();  
			while (optIt.hasNext()) {
				MatchSequence ms = optIt.next();
				// determined branches have a length, nonDetermined do not
				if (ms.length().orElse(Integer.MIN_VALUE) > shortest().orElse(Integer.MAX_VALUE)) {
					optIt.remove(); 
					// prune ms from options if its length can be determined and is longer than the shortest alternative.
				}
			}
		}

		MatchOptions() {
			options = new ArrayList<>();
			for (Matcher matcher: matchers) options.add(new MatchSequence((Matcher) matcher.clone()));
		}

		public List<Token> selectOneMatch() {
			if (options.isEmpty()) throw new RuntimeException();
			return options.get(0).selectOneMatch();
		}
	}

	class MatchSequence {

		Matcher matcher;
		MatchSequence next;
		MatchOptions alternatives;

		public List<Token> selectOneMatch() {
			return FluentList
					.with(matcher.token())
					.append(next != null ? next.selectOneMatch() : null)
					.append(alternatives != null ? alternatives.selectOneMatch() : null);
		}

		// the sequence is determined if the current matcher is in any state apart from 
		// IN_PROGRESS, and if it SUCEEDED that if subsequent parts of the chain are present
		// then they are also determined.
		public Optional<Boolean> notDetermined() {
			if (matcher.state() == null) return Optional.empty();
			if (matcher.state().equals(Match.SUCCEEDED)) {
				if (next != null) return next.notDetermined();
				if (alternatives != null) return alternatives.notDetermined();
				return Optional.of(false);
			}
			if (matcher.state().equals(Match.IN_PROGRESS)) return Optional.of(true);
			// If matcher is SLEEPING then it has never been used 
			// This condition probably does not happen
			// If the matcher has FAILED it is determined, however it
			// should have been pruned already by MatchOptions.consume()
			throw new RuntimeException();
			// return false;
		}

		public Optional<Integer> length() {
			if (notDetermined().orElse(true)) return Optional.empty();
			if (next == null && alternatives == null) return Optional.of(1);
			if (next != null) return next.length().map(i -> i+1);
			if (alternatives != null) return alternatives.shortest().map(i-> i+1);
			throw new RuntimeException();
		}

		private void prune() {
			// only do anything if there are alternatives that exist
			if (next != null) return;
			if (alternatives == null) return;
			// where there is exactly one option we can update the MatchSequence
			// so that there are no alternatives in the sequence (by converting tree
			// of alternatives to chain.)
			if (alternatives.options.size() == 1) {
				next = alternatives.options.get(0);
				alternatives = null;
			}
		}

		// the match sequence will consume characters or other tokens. If the matcher has SUCCEEDED it will delegate to
		// the next matcher in the chain
		public boolean consume(Object o) {
			boolean out = false;
			if (matcher.state() == null) {
				out = matcher.consume(o);
			} else if (matcher.state().equals(Match.SUCCEEDED)) {
				if (next != null) {
					out = next.consume(o);
				} else {
					if (alternatives == null) alternatives = new MatchOptions();
					out = alternatives.consume(o);
				} 
			} else if (matcher.state().equals(Match.IN_PROGRESS)) {
				if (matcher.consume(o)) {
					// matcher may have SUCCEEDED or still be IN_PROGRESS
					out = true;
				} else {
					// the matcher has refused the current input this represents either a complete fail
					// or it was deferring SUCCESS until it met an unwanted character. In this second case it
					// will now have SUCCEEDED and we want to delegate the input onto the next level. If that in turn
					// succeeds this chain can continue to grow. If not we will have a set of FAILS at the 
					// next level.
					if (matcher.state().equals(Match.SUCCEEDED)) {
						if (alternatives == null) alternatives = new MatchOptions();
						out = alternatives.consume(o);
					} else {
						// this state is a real inability to match the input
						// the matcher should be in a FAIL state if implemented correctly
						if (!matcher.state().equals(Match.FAIL)) throw new RuntimeException();
						// we need to delete this chain from the MatchOptions tree. 
						// this will be done by MatchOptions.consume()
						out = false;
					}
				}
			} else {
				// matcher has failed already
				// this maybe should have already been pruned unless it was the first in the chain
				out = false;
			}
			this.prune();
			return out;
		}

		public MatchSequence(Matcher matcher) {
			this.matcher = matcher;
			this.alternatives = null;
		}
	}

}