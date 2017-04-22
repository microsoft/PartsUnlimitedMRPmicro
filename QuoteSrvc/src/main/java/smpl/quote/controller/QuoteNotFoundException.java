package smpl.quote.controller;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Order not found	")  //404
public class QuoteNotFoundException extends RuntimeException {

	public QuoteNotFoundException(String id) {
		// TODO Auto-generated constructor stub
	}

}
