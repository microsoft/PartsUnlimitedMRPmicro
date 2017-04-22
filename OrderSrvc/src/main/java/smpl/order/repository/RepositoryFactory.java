package smpl.order.repository;

import smpl.order.repository.mock.MockOrderRepository;
import smpl.order.repository.service.OrderService;

public class RepositoryFactory {
	public static OrderRepository getOrderRepository() {
		switch (s_factory.storageKind) {
		case RepositoryFactory.MEMORY:
			return s_factory.mockRepos.orderRepository;
		case RepositoryFactory.MONGODB:
			return s_factory.mongodbRepos.orderRepository;
		default:
			return null;
		}
	}

	private void init(String storage) {
		this.storageKind = storage;
		this.mockRepos = new Repositories();
		this.mongodbRepos = new Repositories();

		this.mockRepos.orderRepository = new MockOrderRepository();
		this.mongodbRepos.orderRepository = mongoDBOrderRepository;
	}

	private RepositoryFactory(String storage) {
		init(storage);
	}

	public static synchronized RepositoryFactory getFactory() {
		return s_factory;
	}

	static public void reset(String storage) {
		s_factory = new RepositoryFactory(storage);
	}

	OrderService mongoDBOrderRepository;

	private class Repositories {
		OrderRepository orderRepository;
	}

	private Repositories mockRepos = new Repositories();
	private Repositories mongodbRepos = new Repositories();
	
	private String storageKind = RepositoryFactory.MONGODB;
	private static RepositoryFactory s_factory;

	public static final String MEMORY = "memory";
	public static final String MONGODB = "mongodb";
}
