package smpl.catalog.repository;

import org.springframework.data.mongodb.core.MongoTemplate;

import smpl.catalog.repository.mock.MockCatalogItemsRepository;
import smpl.catalog.repository.service.CatalogService;

public class RepositoryFactory {
	public static CatalogItemsRepository getCatalogItemsRepository() {
		switch (s_factory.storageKind) {
		case RepositoryFactory.MEMORY:
			return s_factory.mockRepos.catalogItems;
		case RepositoryFactory.MONGODB:
			return s_factory.mongodbRepos.catalogItems;
		default:
			return null;
		}
	}

	private void init(String storage) {
		/*
		 * if (mongoTemplate == null) { try { mongoTemplate =
		 * OrderingConfiguration.getApplicationContext().getBean(MongoTemplate.
		 * class); } catch (Exception exc) { } }
		 */

		this.storageKind = storage;
		this.mockRepos = new Repositories();
		this.mongodbRepos = new Repositories();

		this.mockRepos.catalogItems = new MockCatalogItemsRepository();
		this.mongodbRepos.catalogItems = mongoDBCatalogItemsRepository;
	}

	private RepositoryFactory(String storage) {
		init(storage);
	}

	public static synchronized RepositoryFactory getFactory() {
		return s_factory;
	}

	static public void reset(String storage) {
		if (s_factory != null) {
			s_factory.mongoTemplate = null;
		}
		s_factory = new RepositoryFactory(storage);

	}

	private MongoTemplate mongoTemplate;

	CatalogService mongoDBCatalogItemsRepository;

	private class Repositories {
		CatalogItemsRepository catalogItems;
	}

	private Repositories mockRepos = new Repositories();
	private Repositories mongodbRepos = new Repositories();

	private String storageKind = RepositoryFactory.MONGODB;
	private static RepositoryFactory s_factory;

	public static final String MEMORY = "memory";
	public static final String MONGODB = "mongodb";
}
