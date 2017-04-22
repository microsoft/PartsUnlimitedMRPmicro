package smpl.restclient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import smpl.restclient.model.Quote;
import smpl.restclient.service.QuoteService;

@RestController

@RequestMapping("/api/quotes")
public class QuoteController {

	private final QuoteService quoteService;

	@Autowired
	public QuoteController(QuoteService quoteService) {
		this.quoteService = quoteService;
	}

	@RequestMapping(method = RequestMethod.GET, value = "{quoteId}")
	public ResponseEntity<?> getQuoteById(@PathVariable String quoteId) {
		return quoteService.getQuoteByID(quoteId);
	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getQuotesByCustomerName(@RequestParam(value = "name") String name) {
		return quoteService.getQuotesByCustomerName(name);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "{quoteId}")
	public ResponseEntity<?> removeQuoteItem(@PathVariable String quoteId) {
		return quoteService.deleteQuoteByID(quoteId);
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> createQuote(@RequestBody Quote info) {
		return quoteService.createQuote(info);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "{quoteId}")
	public ResponseEntity<?> updateQuoteItem(@PathVariable String quoteId, @RequestBody Quote info) {
		return quoteService.updateQuoteItem(quoteId, info);
	}
}
