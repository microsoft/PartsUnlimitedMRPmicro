package smpl.quote.repository;

import smpl.quote.repository.mock.MockQuoteRepository;
import smpl.quote.repository.service.QuoteService;

public class RepositoryFactory {
	
	
	private RepositoryFactory(String storage) {
		init(storage);
	}

	public static QuoteRepository getQuoteRepository() {
		switch (s_factory.storageKind) {
		case RepositoryFactory.MEMORY:
			return s_factory.mockRepos.quoteRepository;
		case RepositoryFactory.MONGODB:
			return s_factory.mongodbRepos.quoteRepository;
		default:
			return null;
		}
	}

	private void init(String storage) {
		this.storageKind = storage;
		this.mockRepos = new Repositories();
		this.mongodbRepos = new Repositories();

		this.mockRepos.quoteRepository = new MockQuoteRepository();
		this.mongodbRepos.quoteRepository = mongoDBQuoteRepository;
	}

	public static synchronized RepositoryFactory getFactory() {
		return s_factory;
	}

	public static void reset(String storage) {
		s_factory = new RepositoryFactory(storage);
	}

	QuoteService mongoDBQuoteRepository;

	private class Repositories {
		QuoteRepository quoteRepository;
	}

	private Repositories mockRepos = new Repositories();
	private Repositories mongodbRepos = new Repositories();

	private String storageKind = RepositoryFactory.MONGODB;
	private static RepositoryFactory s_factory;

	public static final String MEMORY = "memory";
	public static final String MONGODB = "mongodb";
}
