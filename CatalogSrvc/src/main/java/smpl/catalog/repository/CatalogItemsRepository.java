package smpl.catalog.repository;

import java.util.List;

import smpl.catalog.model.Catalog;
import io.opentracing.*;
/**
 * Interface for repositories holding catalog item data.
 */
public interface CatalogItemsRepository
{
    List<Catalog> getCatalogItems(Span span);
    
    List<Catalog> getCatalogItemsFB(Span span);
    
    Catalog getCatalogItem(String sku,Span span);
    
    Catalog getCatalogItemFB(String sku,Span span);

    boolean updateCatalogItem(String sku, Catalog catalogItem, String eTag,Span span);

    boolean updateCatalogItemFB(String sku, Catalog catalogItem, String eTag,Span span);
    
    boolean removeCatalogItem(String sku, String eTag,Span span);
    
    boolean removeCatalogItemFB(String sku, String eTag,Span span);
    
    Catalog createCatalog(Catalog from,Span span) ;
    
    Catalog createCatalogFB(Catalog from,Span span) ;
}
