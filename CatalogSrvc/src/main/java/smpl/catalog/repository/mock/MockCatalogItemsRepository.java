package smpl.catalog.repository.mock;

import smpl.catalog.repository.TestPath;
import smpl.catalog.repository.model.CatalogItem;
import smpl.catalog.model.Catalog;
import smpl.catalog.repository.CatalogItemsRepository;
import smpl.catalog.repository.model.CatalogItem;

import java.util.ArrayList;
import java.util.List;

import io.opentracing.Span;

/**
 * An in-memory repository of catalog items. Used for testing the API surface area.
 */
public class MockCatalogItemsRepository
        implements CatalogItemsRepository, TestPath
{
    public MockCatalogItemsRepository()
    {
        catalog.add(new Catalog("MRP-0001", "MockTestData_Brake Pads", 26.99,  10));
        catalog.add(new Catalog("MRP-0002", "MockTestData_Brake Calipers", 33.99, 10));
        catalog.add(new Catalog("MRP-0003", "MockTestData_Brake Calipers Guide Pin", 2.99, 10));
    }

    /**
     * Retrieves a list of the items in the catalog.
     *
     * @return An catalog item list
     */
    @Override
	public List<Catalog> getCatalogItems(Span span) {
    
        List<Catalog> result = new ArrayList<>();
        for (Catalog catalogobj : catalog)
        {
            result.add(catalogobj);
        }
        return result;
    }

    /**
     * Retrieves information on a specific product
     *
     * @param sku The SKU number
     * @return The catalogItem, null if not found.
     */
    @Override
    public Catalog getCatalogItem(String sku, Span span) 
    {
        for (Catalog catalogobj : catalog)
        {
            if (compareSkuNumbers(sku, catalogobj))
            {
                return catalogobj;
            }
        }
        return null;
    }

    private boolean compareSkuNumbers(String sku, Catalog catalog)
    {
    	//if(sku != null && !sku.trim().equals(""))
    	if(sku != null && !"".equals(sku.trim()))
        {
    		//return catalog.getSkuNumber().toLowerCase().equals(sku.toLowerCase());
    		return catalog.getSkuNumber().equalsIgnoreCase(sku);  
        }
    	else
    	{
    		return false;
    	}
    }

    

    /**
     * Remove an catalog item information record from the catalog.
     *
     * @param sku The SKU number
     * @return true if found, false otherwise.
     */
    @Override
    public boolean removeCatalogItem(String sku, String eTag, Span span) 
    {
        for (int i = 0; i < catalog.size(); ++i)
        {
            Catalog catalogItem = catalog.get(i);
            if (compareSkuNumbers(sku, catalogItem))
            {
                catalog.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
	public boolean updateCatalogItem(String sku, Catalog catalogItem, String eTag, Span span) {
    	boolean bfound = false;
    	for (int i = 0; i < catalog.size(); ++i)
        {
            Catalog ci = catalog.get(i);
            if (compareSkuNumbers(sku, ci))
            {
                catalog.set(i, catalogItem);
                bfound = true;
                break;
            }
        }
    	return bfound;
	}
    
    @Override
    public Catalog createCatalog(Catalog from, Span span) {
    	boolean bfound = false;
    	for (int i = 0; i < catalog.size(); ++i)
        {
            Catalog ci = catalog.get(i);
            if (compareSkuNumbers(from.getSkuNumber(), ci))
            {
            	bfound = true;
            }
        }
    	if(!bfound)
    	{
    		catalog.add(from);
    	}
    	return from;
	}
    
    private final List<Catalog> catalog = new ArrayList<>();

    @Override
    public void reset()
    {
        catalog.clear();
        catalog.add(new Catalog("MRP-0001", "MockTestData_Brake Pads", 26.99,  10));
        catalog.add(new Catalog("MRP-0002", "MockTestData_Brake Calipers", 33.99, 10));
        catalog.add(new Catalog("MRP-0003", "MockTestData_Brake Calipers Guide Pin", 2.99, 10));
    }

	
	@Override
	public Catalog getCatalogItemFB(String sku, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public boolean updateCatalogItemFB(String sku, Catalog catalogItem, String eTag, Span span) {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public boolean removeCatalogItemFB(String sku, String eTag, Span span) {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public Catalog createCatalogFB(Catalog from, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Catalog> getCatalogItemsFB(Span span) {
		// TODO Auto-generated method stub
		return null;
	}
}
