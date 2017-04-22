package smpl.quote.repository.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.opentracing.Span;
import smpl.quote.BadRequestException;
import smpl.quote.model.Quote;
import smpl.quote.repository.QuoteRepository;
import smpl.quote.repository.TestPath;

public class MockQuoteRepository implements QuoteRepository, TestPath {
	private final List<Quote> quotes;
	private static final Random s_counter;

	static {
		s_counter = new Random();
	}

	public MockQuoteRepository() {
		this.quotes = new ArrayList<>();
	}

	@Override
	public void reset() {
		quotes.clear();
	}

	@Override
	public List<Quote> getAllQuotes(Span span) {
		List<Quote> result = new ArrayList<>();
		for (Quote quote : quotes) {
			result.add(new Quote(quote));
		}
		return result;
	}

	/**
	 * Retrieves a specific quote from the repository.
	 *
	 * @param id
	 *            The quote id.
	 * @return A Quote object, if found.
	 */
	@Override
	public Quote getQuote(String id, Span span) {
		for (Quote q : quotes) {
			if (q.getQuoteId().equals(id)) {
				return q;
			}
		}
		return null;
	}

	/**
	 * Retrieves a list of quotes where the customer name contains the string
	 * passed in.
	 *
	 * @param customerName
	 *            A fragment of the customer name.
	 * @return A list of quotes, possibly empty.
	 */
	@Override
	public List<Quote> getQuotesByCustomerName(String customerName, Span span) {
		List<Quote> lst = new ArrayList<>();
		for (Quote q : quotes) {
			if (q.getCustomerName().toLowerCase().contains(customerName.toLowerCase())) {
				lst.add(q);
			}
		}
		return lst;
	}

	/**
	 * Retrieves a list of quotes where the dealer name is the string passed in.
	 *
	 * @param dealerName
	 * @return A list of quotes
	 */
	@Override
	public List<String> getQuoteIdsByDealerName(String dealerName, Span span) {
		List<String> lst = new ArrayList<>();
		for (Quote q : quotes) {
			if (q.getDealerName().compareToIgnoreCase(dealerName) == 0) {
				lst.add(q.getQuoteId());
			}
		}
		return lst;
	}

	/**
	 * Creates a new quote from information edited by a client.
	 *
	 * @param quote
	 *            The client quote information.
	 * @return A Quote object.
	 */
	@Override
	public Quote createQuote(Quote quote, Span span) throws BadRequestException {
		String id = quote.getQuoteId();

		if (id == null || id.isEmpty()) {
			quote.setQuoteId(String.format("%d", s_counter.nextInt() & 0x7FFFFFFF));
		} else {
			if (getQuote(id, span) != null) {
				throw new BadRequestException(String.format("Duplicate: the quote '%s' already exists", id));
			}
		}

		quotes.add(quote);

		return quote;

	}

	/**
	 * Update an existing quote from client-edited information.
	 *
	 * @param id
	 *            The quote id.
	 * @param from
	 *            New client-edited information.
	 * @return true if the quote exists, false otherwise.
	 */
	@Override
	public boolean updateQuote(String id, Quote from, Span span) {
		Quote quote = getQuote(id, span);
		if (quote == null)
			return false;

		from.setQuoteId(id);

		int idx = quotes.indexOf(quote);
		quotes.set(idx, from);

		return true;
	}

	/**
	 * Remove a quote from the system.
	 *
	 * @param id
	 *            The quote id.
	 * @return true if the quote exists, false otherwise
	 */
	@Override
	public boolean removeQuote(String id, Span span) {
		Quote quote = getQuote(id, span);
		if (quote == null)
			return false;
		quotes.remove(quote);
		return true;
	}

	@Override
	public List<Quote> getAllQuotesFB(Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Quote getQuoteFB(String id, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Quote> getQuotesByCustomerNameFB(String customerName, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getQuoteIdsByDealerNameFB(String dealerName, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Quote createQuoteFB(Quote from, Span span) throws BadRequestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateQuoteFB(String id, Quote quote, Span span) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeQuoteFB(String id, Span span) {
		// TODO Auto-generated method stub
		return false;
	}
}