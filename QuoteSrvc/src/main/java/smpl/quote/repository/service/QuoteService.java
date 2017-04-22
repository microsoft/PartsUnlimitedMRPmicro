package smpl.quote.repository.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import smpl.quote.BadRequestException;
import smpl.quote.model.Quote;
import smpl.quote.repository.QuoteRepository;
import smpl.quote.repository.model.QuoteDetails;
import io.opentracing.*;

@Service
public class QuoteService implements QuoteRepository
{
	private static final Logger log = LoggerFactory.getLogger(QuoteService.class);
	private static final Random s_counter;
	private MongoOperations mongoOperations;

	@Autowired
	public QuoteService(MongoOperations operations) {
		this.mongoOperations = operations;

	}

	static {
		s_counter = new Random();
	}

	@Override
	@HystrixCommand(fallbackMethod = "getQuoteFB", groupKey = "QuoteService", commandKey = "QuoteService", threadPoolKey = "QuoteService")
	public Quote getQuote(String id, Span span) {
		log.info("getQuote called");
		QuoteDetails existing = findExistingQuote(id);
		span.log("GetQuoteFromDB");
		return (existing != null) ? existing.toQuote() : null;

	}

	@Override
	@HystrixCommand(fallbackMethod = "getQuotesByCustomerNameFB", groupKey = "QuoteService", commandKey = "QuoteService", threadPoolKey = "QuoteService")
	public List<Quote> getQuotesByCustomerName(String customerName, Span span) {
		
		log.info("getQuotesByCustomerName called");
		List<QuoteDetails> found = mongoOperations.findAll(QuoteDetails.class);
		
		List<Quote> result = new ArrayList<>();

		if (found != null) {
			for (QuoteDetails q : found) {
				String cName = q.getCustomerName();
				if (cName != null && cName.toLowerCase().contains(customerName.toLowerCase())) {
					result.add(q.toQuote());
				}
			}
		}
		span.log("GetQuotesFromDB");
		return result;
	}

	@Override
	@HystrixCommand(fallbackMethod = "getQuoteIdsByDealerNameFB", groupKey = "QuoteService", commandKey = "QuoteService", threadPoolKey = "QuoteService")
	public List<String> getQuoteIdsByDealerName(String dealerName,Span span) {
		log.info("getQuoteIdsByDealerName called");
		List<QuoteDetails> foundQueries = mongoOperations.find(new Query(Criteria.where("dealerName").is(dealerName)),
				QuoteDetails.class);
		List<String> quotesIds = new ArrayList<>();
		for (QuoteDetails q : foundQueries) {
			quotesIds.add(q.getQuoteId());
		}
		span.log("GetQuotesFromDB");
		return quotesIds;
	}

    @Override
	@HystrixCommand(fallbackMethod = "createQuoteFB", groupKey = "QuoteService", commandKey = "QuoteService", threadPoolKey = "QuoteService")
	public Quote createQuote(Quote from, Span span) throws BadRequestException {
		Quote quote = new Quote(from);
		log.info("createQuote called");
		// TODO

		/*
		 * DealerInfo info = dealers.getDealer(from.getDealerName()); if (info
		 * == null) { dealers.upsertDealer(new
		 * DealerInfo(from.getDealerName()),null); }
		 */

		String id = quote.getQuoteId();

		if (id == null || id.isEmpty()) {
			quote.setQuoteId(String.format("%d", s_counter.nextInt() & 0x7FFFFFFF));
		} else {
			if (getQuote(id,span) != null) {
				throw new BadRequestException(String.format("Duplicate: the quote '%s' already exists", id));
			}
		}

		mongoOperations.insert(new QuoteDetails(quote));
		span.log("CreatedQuote");
		return quote;
	}

	@Override
	@HystrixCommand(fallbackMethod = "updateQuoteFB", groupKey = "QuoteService", commandKey = "QuoteService", threadPoolKey = "QuoteService")
	public boolean updateQuote(String id, Quote from, Span span) {
		QuoteDetails existing = findExistingQuote(id);
		log.info("updateQuote called");
		span.log("GetQuoteFromDB");
		if (existing == null)
			return false;
		
		from.setQuoteId(id); // Just to make sure...

		// TODO

		/*
		 * DealerInfo info = dealers.getDealer(from.getDealerName()); if (info
		 * == null) { dealers.upsertDealer(new DealerInfo(from.getDealerName()),
		 * null); }
		 */

		QuoteDetails details = new QuoteDetails(from);
		details.setId(existing.getId());

		mongoOperations.save(details);
		span.log("UpdatedQuote");
		return true;
	}

	@Override
	@HystrixCommand(fallbackMethod = "removeQuoteFB", groupKey = "QuoteService", commandKey = "QuoteService", threadPoolKey = "QuoteService")
	public boolean removeQuote(String id, Span span) {
		log.info("removeQuote called");
		Query findExisting = new Query(Criteria.where("quoteId").is(id));
		QuoteDetails existing = mongoOperations.findAndRemove(findExisting, QuoteDetails.class);
		span.log("DeletedQuote");
		return existing != null;
	}

	private QuoteDetails findExistingQuote(String id) {
		Query findExisting = new Query(Criteria.where("quoteId").is(id));
		return mongoOperations.findOne(findExisting, QuoteDetails.class);
	}

	@Override
	@HystrixCommand(fallbackMethod = "getAllQuotesFB", groupKey = "QuoteService", commandKey = "QuoteService", threadPoolKey = "QuoteService")
	public List<Quote> getAllQuotes(Span span) {
		log.info("getAllQuotes called");
		List<QuoteDetails> found = mongoOperations.findAll(QuoteDetails.class);
		List<Quote> result = new ArrayList<>();

		if (found != null) {
			for (QuoteDetails q : found) {
				result.add(q.toQuote());
			}
		}
		span.log("GetQuotesFromDB");
		return result;
	}

	/*
	 * FallBack Method implementations starts
	 */
	@Override
	public List<Quote> getAllQuotesFB(Span span) {
		log.info("getAllQuotesFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return null;
	}

	@Override
	public Quote getQuoteFB(String id,Span span) {
		log.info("getQuoteFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return null;
	}

	@Override
	public List<Quote> getQuotesByCustomerNameFB(String customerName, Span span) {
		// throw new RuntimeException("FallBack Simulation");
		// Uncomment the above line and comment the below 2 lines to see
		// fallback in action

		log.info("getQuotesByCustomerNameFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return null;
	}

	@Override
	public List<String> getQuoteIdsByDealerNameFB(String dealerName, Span span) {
		log.info("getQuoteIdsByDealerNameFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return null;
	}

    @Override
	public Quote createQuoteFB(Quote from, Span span) throws BadRequestException {
    	log.info("createQuoteFB fall back method reached...");
    	span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return null;
	}

	@Override
	public boolean updateQuoteFB(String id, Quote quote, Span span) {
		log.info("updateQuoteFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return false;
	}

	@Override
	public boolean removeQuoteFB(String id, Span span) {
		log.info("removeQuoteFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return false;
	}

}
