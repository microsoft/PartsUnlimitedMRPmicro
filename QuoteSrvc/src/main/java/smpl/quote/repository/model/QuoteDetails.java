package smpl.quote.repository.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import smpl.quote.model.Quote;
import smpl.quote.model.QuoteItemInfo;

@Document(collection = "quotes")
public class QuoteDetails
{
    @Id
    private String id;
    @Indexed
    private String quoteId;
    private String validUntil;    
    @Indexed
    private String customerName;
    @Indexed
    private String dealerName;
    private QuoteItemInfo[] quoteItems;    
//    Front end
    private QuoteItemInfo[] additionalItems;    
    private String comments;
    private String terms;
    private String unitDescription;
    private double unitCost;
    private double height;
    private double width;
    private double depth;
    private int unit;
    private String purpose;
    private double ambientPeak;
    private double ambientAverage;
    private boolean buildOnSite;
  //End    
    private double totalCost;
    private double discount;
    private String city;
    private String postalCode;
    private String state;

    public QuoteDetails()
    {
    }

    public QuoteDetails(Quote from)
    {
        this.quoteId = from.getQuoteId();
        this.validUntil = from.getValidUntil();
        this.customerName = from.getCustomerName();
        this.dealerName = from.getDealerName();
        this.totalCost = from.getTotalCost();
        this.discount = from.getDiscount();
        this.city = from.getCity();
        this.postalCode = from.getPostalCode();
        this.state = from.getState();

        List<QuoteItemInfo> qi = from.getQuoteItems();
        this.quoteItems = (qi != null && !qi.isEmpty()) ?
        qi.toArray(new QuoteItemInfo[qi.size()]) : new QuoteItemInfo[0];
      
//        		Front end
        List<QuoteItemInfo> ai = from.getAdditionalItems();
        this.additionalItems = (ai != null && !ai.isEmpty()) ?
        ai.toArray(new QuoteItemInfo[ai.size()]) : new QuoteItemInfo[0];

        this.comments = from.getComments();
        this.terms = from.getTerms();
        this.unitDescription = from.getUnitDescription();
        this.unitCost = from.getUnitCost();
        this.height = from.getHeight();
        this.width = from.getWidth();
        this.depth = from.getDepth();
        this.unit = from.getUnit();
        this.purpose = from.getPurpose();
        this.ambientPeak = from.getAmbientPeak();
        this.ambientAverage = from.getAmbientAverage();
        this.buildOnSite=from.isBuildOnSite();
//        end      
        
    }

    public Quote toQuote()
    {
        Quote result = new Quote();
        result.setQuoteId(quoteId);
        result.setValidUntil(validUntil);
        result.setCustomerName(customerName);
        result.setDealerName(dealerName);
        result.setTotalCost(totalCost);
        result.setDiscount(discount);
        result.setCity(city);
        result.setPostalCode(postalCode);
        result.setState(state);
        if (quoteItems != null)
        {
            for (QuoteItemInfo item : quoteItems)
            {
                result.addQuoteItem(item.getSkuNumber(), item.getAmount());
            }
        }
//        Front End
        if (additionalItems != null)
        {
            for (QuoteItemInfo item : additionalItems)
            {
                result.addAdditionalItem(item.getSkuNumber(), item.getAmount());
            }
        }
        result.setComments(comments);
        result.setTerms(terms);
        result.setUnitDescription(unitDescription);
        result.setUnitCost(unitCost);
        result.setWidth(width);
        result.setHeight(height);
        result.setDepth(depth);
        result.setUnit(unit);
        result.setPurpose(purpose);
        result.setAmbientPeak(ambientPeak);
        result.setAmbientAverage(ambientAverage);
        result.setBuildOnSite(buildOnSite); 
//        End
        
        return result;
    }


    public String getQuoteId()
    {
        return quoteId;
    }

    public String getDealerName()
    {
        return dealerName;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

	public String getValidUntil() {
		return validUntil;
	}
	public double getTotalCost() {
		return totalCost;
	}

	public double getDiscount() {
		return discount;
	}

	public String getCity() {
		return city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public String getState() {
		return state;
	}

	public String getComments() {
		return comments;
	}

	public String getTerms() {
		return terms;
	}

	public String getUnitDescription() {
		return unitDescription;
	}

	public double getUnitCost() {
		return unitCost;
	}

	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}

	public double getDepth() {
		return depth;
	}

	public int getUnit() {
		return unit;
	}

	public String getPurpose() {
		return purpose;
	}

	public double getAmbientPeak() {
		return ambientPeak;
	}

	public double getAmbientAverage() {
		return ambientAverage;
	}

	public boolean isBuildOnSite() {
		return buildOnSite;
	}


}
