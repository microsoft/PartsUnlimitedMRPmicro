package smpl.catalog.repository.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import smpl.catalog.model.Catalog;
import smpl.catalog.repository.CatalogItemsRepository;
import smpl.catalog.repository.model.CatalogItem;
import io.opentracing.*;

@Service
public class CatalogService implements CatalogItemsRepository {

	private static final Logger log = LoggerFactory.getLogger(CatalogService.class);
	private MongoOperations mongoOperations;	

	@Autowired
	public CatalogService(MongoOperations operations) {
		this.mongoOperations = operations;

	}

	@Override
	@HystrixCommand(fallbackMethod = "getCatalogItemsFB", groupKey = "CatalogService", commandKey = "CatalogService")

	public List<Catalog> getCatalogItems(Span span) {
		log.info("getCatalogItems called");
		List<CatalogItem> found = mongoOperations.findAll(CatalogItem.class);

		List<Catalog> result = new ArrayList<>();

		for (CatalogItem catalogItem : found) {
			result.add(catalogItem.toCatalogItem());
		}
		span.log("GetCatalogFromDB");
		return result;
	}

	@Override
	@HystrixCommand(fallbackMethod = "getCatalogItemFB", groupKey = "CatalogService", commandKey = "CatalogService", threadPoolKey = "CatalogService")
	public Catalog getCatalogItem(String sku,Span span) {
		log.info("getCatalogItem by sku called");
		CatalogItem existing = findExistingCatalogItem(sku);

		if (existing != null) {	
			span.log("GetCatalogFromDB");
			return existing.toCatalogItem();
		}
		span.log("GetCatalogFromDB");
		return null;
	}

	private CatalogItem findExistingCatalogItem(String sku) {
		Query findExisting = new Query(Criteria.where("skuNumber").is(sku));
		return mongoOperations.findOne(findExisting, CatalogItem.class);
	}

	@Override
	@HystrixCommand(fallbackMethod = "updateCatalogItemFB", groupKey = "CatalogService", commandKey = "CatalogService", threadPoolKey = "CatalogService")
	public boolean updateCatalogItem(String sku, Catalog catalogItem, String eTag,Span span) {
		log.info("updateCatalogItem called");
		CatalogItem existing = findExistingCatalogItem(sku);
		if (existing == null)
			return false;

		CatalogItem mongoCatalogItem = new CatalogItem(catalogItem);
		mongoCatalogItem.setId(existing.getId());
		mongoOperations.save(mongoCatalogItem);
		span.log("CatalogUpdated");
		return true;

	}

	@Override
	@HystrixCommand(fallbackMethod = "removeCatalogItemFB", groupKey = "CatalogService", commandKey = "CatalogService", threadPoolKey = "CatalogService")
	public boolean removeCatalogItem(String sku, String eTag,Span span) {
		log.info("removeCatalogItem called");
		Query findExisting = new Query(Criteria.where("skuNumber").is(sku));
		CatalogItem existing = mongoOperations.findAndRemove(findExisting, CatalogItem.class);
		span.log("DeleteCatalogFromDB");
		return existing != null;
	}

	@Override
	@HystrixCommand(fallbackMethod = "createCatalogFB", groupKey = "CatalogService", commandKey = "CatalogService", threadPoolKey = "CatalogService")
	public Catalog createCatalog(Catalog from,Span span) {
		log.info("createCatalog called");
		Catalog catalog = new Catalog(from);
		mongoOperations.insert(new CatalogItem(from));
		span.log("CatalogCreated");
		return catalog;
	}

	@Override
	public List<Catalog> getCatalogItemsFB(Span span) {
		log.info("getCatalogItems fall back method reached...");		
		// TODO Auto-generated method stub
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return null;
	}

	@Override
	public Catalog getCatalogItemFB(String sku,Span span) {
		log.info("getCatalogItem fall back method reached...");
		// TODO Auto-generated method stub
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return null;
	}

	@Override
	public boolean updateCatalogItemFB(String sku, Catalog catalogItem, String eTag,Span span) {
		// TODO Auto-generated method stub
		log.info("updateCatalogItemFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		return false;
	}

	@Override
	public boolean removeCatalogItemFB(String sku, String eTag,Span span) {
		log.info("removeCatalogItemFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Catalog createCatalogFB(Catalog from,Span span) {
		log.info("createCatalogFB fall back method reached...");
		span.setTag("error", "Unable to connect to MongoDB, Fallback method reached.");
		// TODO Auto-generated method stub
		return null;
	}

}
