package smpl.quote.repository;

import java.util.List;

import org.springframework.scheduling.annotation.EnableAsync;

import smpl.quote.BadRequestException;
import smpl.quote.model.Quote;
import io.opentracing.*;
@EnableAsync
public interface QuoteRepository {
	
	   
	    public List<Quote> getAllQuotes(Span span);

	    Quote getQuote(String id, Span span);

	    List<Quote> getQuotesByCustomerName(String customerName, Span span);

	    List<String> getQuoteIdsByDealerName(String dealerName, Span span);

	    Quote createQuote(Quote from, Span span) throws BadRequestException;

	    boolean updateQuote(String id, Quote quote, Span span);

	    boolean removeQuote(String id, Span span);
	    
	    
	    //FallBack Methods
	    
	    public List<Quote> getAllQuotesFB(Span span);

	    Quote getQuoteFB(String id, Span span);

	    List<Quote> getQuotesByCustomerNameFB(String customerName,Span span);

	    List<String> getQuoteIdsByDealerNameFB(String dealerName, Span span);

	    Quote createQuoteFB(Quote from ,Span span) throws BadRequestException;

	    boolean updateQuoteFB(String id, Quote quote, Span span);

	    boolean removeQuoteFB(String id, Span span);

}