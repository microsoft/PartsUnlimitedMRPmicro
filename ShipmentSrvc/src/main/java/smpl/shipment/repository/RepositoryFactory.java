package smpl.shipment.repository;

import smpl.shipment.repository.mock.MockShipmentRepository;
import smpl.shipment.repository.service.ShipmentService;

public class RepositoryFactory {

	ShipmentService mongoDBShipmentRepository;

	private RepositoryFactory(String storage) {
		init(storage);
	}

	public static ShipmentRepository getShipmentRepository() {
		switch (s_factory.storageKind) {
		case RepositoryFactory.MEMORY:
			return s_factory.mockRepos.shipmentRepository;
		case RepositoryFactory.MONGODB:
			return s_factory.mongodbRepos.shipmentRepository;
		default:
			return null;
		}
	}

	private void init(String storage) {
		this.storageKind = storage;
		this.mockRepos = new Repositories();
		this.mongodbRepos = new Repositories();

		this.mockRepos.shipmentRepository = new MockShipmentRepository();
		this.mongodbRepos.shipmentRepository = mongoDBShipmentRepository;
	}

	public static synchronized RepositoryFactory getFactory() {
		return s_factory;
	}

	public static void reset(String storage) {
		s_factory = new RepositoryFactory(storage);
	}

	private class Repositories {
		ShipmentRepository shipmentRepository;
	}

	private Repositories mockRepos = new Repositories();
	private Repositories mongodbRepos = new Repositories();

	private String storageKind = RepositoryFactory.MONGODB;
	private static RepositoryFactory s_factory;

	public static final String MEMORY = "memory";
	public static final String MONGODB = "mongodb";
}
