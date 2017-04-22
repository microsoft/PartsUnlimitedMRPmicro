package smpl.restclient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import smpl.restclient.model.Catalog;
import smpl.restclient.service.CatalogService;

@RestController

@RequestMapping("/api/catalog")
public class CatalogController {

	private CatalogService catalogService;

	@Autowired
	public CatalogController(CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getCatalogItems() {
		return catalogService.getCatalogs();
	}

	@RequestMapping(method = RequestMethod.GET, value = "{sku}")
	public ResponseEntity<?> getCatalogItem(@PathVariable String sku) {
		ResponseEntity<String> result = catalogService.getCatalogByID(sku);
		return result;

	}

	@RequestMapping(method = RequestMethod.DELETE, value = "{sku}")
	public ResponseEntity<?> removeCatalogItem(@PathVariable String sku) {
		return catalogService.deleteCatalogByID(sku);
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> addCatalogItem(@RequestBody Catalog info) {
		return catalogService.addCatalogItem(info);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "{sku}")
	public ResponseEntity<?> updateCatalogItem(@PathVariable String sku, @RequestBody Catalog info) {
		return catalogService.updateCatalogItem(sku, info);
	}
}
