package smpl.quote.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import smpl.quote.Utility;

/**
 * Represents quote information sent from the service to the client.
 *
 * @see smpl.ordering.models.Order
 */
public class Quote {
	private String quoteId;
	private String validUntil;
	private String customerName;
	private String dealerName;
	// Front end
	private String comments;
	private String terms;
	private String unitDescription;
	private double unitCost;
	private List<QuoteItemInfo> additionalItems;
	// End
	private List<QuoteItemInfo> quoteItems;
	private double totalCost;
	private double discount;
	// Front end
	private double height;
	private double width;
	private double depth;
	private int unit;
	private String purpose;
	private double ambientPeak;
	private double ambientAverage;
	private boolean buildOnSite;
	// End
	private String city;
	private String postalCode;
	private String state;

	public Quote() {
	}

	public Quote(Quote quote) {
		this.quoteId = quote.quoteId;
		this.customerName = quote.getCustomerName();
		this.dealerName = quote.getDealerName();
		this.validUntil = quote.getValidUntil();
		this.totalCost = quote.getTotalCost();
		this.discount = quote.getDiscount();
		this.city = quote.getCity();
		this.postalCode = quote.getPostalCode();
		this.state = quote.getState();
		this.quoteItems = quote.getQuoteItems();
		// Front End
		this.comments = quote.getComments();
		this.terms = quote.getTerms();
		this.unitDescription = quote.getUnitDescription();
		this.unitCost = quote.getUnitCost();
		this.additionalItems = quote.getAdditionalItems();
		this.height = quote.getHeight();
		this.width = quote.getWidth();
		this.depth = quote.getDepth();
		this.unit = quote.getUnit();
		this.purpose = quote.getPurpose();
		this.ambientPeak = quote.getAmbientPeak();
		this.ambientAverage = quote.getAmbientAverage();
		this.buildOnSite = quote.isBuildOnSite();
	}

	public String validate() {
		int count = 0;
		StringBuilder errors = new StringBuilder("{\"errors\": [");
		count = Utility.validateStringField(dealerName, "dealerName", count, errors);
		count = Utility.validateStringField(customerName, "customerName", count, errors);
		errors.append("]}");

		return (count > 0) ? errors.toString() : null;
	}

	public String getQuoteId() {
		return quoteId;
	}

	public void setQuoteId(String quoteId) {
		this.quoteId = quoteId;
	}

	public String getDealerName() {
		return dealerName;
	}

	public void setDealerName(String dealerName) {
		this.dealerName = dealerName;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(String validUntil) {
		this.validUntil = validUntil;
	}

	public List<QuoteItemInfo> getQuoteItems() {
		return quoteItems;
	}

	public void setQuoteItems(List<QuoteItemInfo> quoteItems) {
		this.quoteItems = quoteItems;
	}

	/**
	 * Adds an item to the quote items list.
	 *
	 * @param sku
	 *            The item sku number
	 * @param amount
	 *            The amount or number of units quoted.
	 */
	public void addQuoteItem(String sku, double amount) {
		if (quoteItems == null) {
			this.quoteItems = new ArrayList<QuoteItemInfo>();
		}
		if (quoteItems != null) {
			quoteItems.add(new QuoteItemInfo(sku, amount));
		}

	}

	// Front End
	public void addAdditionalItem(String sku, double amount) {
		if (additionalItems == null) {
			this.additionalItems = new ArrayList<QuoteItemInfo>();
		}

		if (additionalItems != null) {
			additionalItems.add(new QuoteItemInfo(sku, amount));
		}

	}

	/**
	 * Gets the overall cost of the quote, before any discount is applied.
	 */
	public double getTotalCost() {
		return totalCost;
	}

	/**
	 * Sets the overall cost of the quote, before any discount is applied.
	 */
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}

	/**
	 * Gets the overall discount, as an amount (not percentage).
	 */
	public double getDiscount() {
		return discount;
	}

	/**
	 * Sets the overall discount, as an amount (not percentage).
	 */
	public void setDiscount(double discount) {
		this.discount = discount;
	}

	/**
	 * Gets city where the unit is to be delivered. Used to estimate delivery
	 * costs and for capturing local regulatory purposes.
	 */
	public String getCity() {
		return city;
	}

	/**
	 * Sets city where the unit is to be delivered. Used to estimate delivery
	 * costs and for capturing local regulatory purposes.
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * Gets postal code where the unit is to be delivered. Used to estimate
	 * delivery costs and for capturing local regulatory purposes.
	 */
	public String getPostalCode() {
		return postalCode;
	}

	/**
	 * Sets postal code where the unit is to be delivered. Used to estimate
	 * delivery costs and for capturing local regulatory purposes.
	 */
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	/**
	 * Gets state where the unit is to be delivered. Used to estimate delivery
	 * costs and for capturing local regulatory purposes.
	 */
	public String getState() {
		return state;
	}

	/**
	 * Sets state where the unit is to be delivered. Used to estimate delivery
	 * costs and for capturing local regulatory purposes.
	 */
	public void setState(String state) {
		this.state = state;
	}

	// Front End
	public List<QuoteItemInfo> getAdditionalItems() {
		return additionalItems;
	}

	public void setAdditionalItems(List<QuoteItemInfo> additionalItems) {
		this.additionalItems = additionalItems;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getTerms() {
		return terms;
	}

	public void setTerms(String terms) {
		this.terms = terms;
	}

	public String getUnitDescription() {
		return unitDescription;
	}

	public void setUnitDescription(String unitDescription) {
		this.unitDescription = unitDescription;
	}

	public double getUnitCost() {
		return unitCost;
	}

	public void setUnitCost(double unitCost) {
		this.unitCost = unitCost;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getDepth() {
		return depth;
	}

	public void setDepth(double depth) {
		this.depth = depth;
	}

	public int getUnit() {
		return unit;
	}

	public void setUnit(int unit) {
		this.unit = unit;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public double getAmbientPeak() {
		return ambientPeak;
	}

	public void setAmbientPeak(double ambientPeak) {
		this.ambientPeak = ambientPeak;
	}

	public double getAmbientAverage() {
		return ambientAverage;
	}

	public void setAmbientAverage(double ambientAverage) {
		this.ambientAverage = ambientAverage;
	}

	public boolean isBuildOnSite() {
		return buildOnSite;
	}

	public void setBuildOnSite(boolean buildOnSite) {
		this.buildOnSite = buildOnSite;
	}

	// End
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Quote quote = (Quote) o;

		if (Double.compare(quote.totalCost, totalCost) != 0)
			return false;
		if (city != null ? !city.equals(quote.city) : quote.city != null)
			return false;
		if (customerName != null ? !customerName.equals(quote.customerName) : quote.customerName != null)
			return false;
		if (dealerName != null ? !dealerName.equals(quote.dealerName) : quote.dealerName != null)
			return false;
		if (postalCode != null ? !postalCode.equals(quote.postalCode) : quote.postalCode != null)
			return false;
		if (quoteId != null ? !quoteId.equals(quote.quoteId) : quote.quoteId != null)
			return false;
		if (state != null ? !state.equals(quote.state) : quote.state != null)
			return false;
		if (validUntil != null ? validUntil.equals(quote.validUntil) : quote.validUntil != null)
			return false;

		// Front end
		if (comments != null ? !comments.equals(quote.comments) : quote.comments != null)
			return false;
		if (terms != null ? !terms.equals(quote.terms) : quote.terms != null)
			return false;
		if (unitDescription != null ? !unitDescription.equals(quote.unitDescription) : quote.unitDescription != null)
			return false;
		if (Double.compare(quote.unitCost, unitCost) != 0)
			return false;
		if (Double.compare(quote.height, height) != 0)
			return false;
		if (Double.compare(quote.width, width) != 0)
			return false;
		if (Double.compare(quote.depth, depth) != 0)
			return false;
		if (Integer.compare(quote.unit, unit) != 0)
			return false;
		if (purpose != null ? !purpose.equals(quote.purpose) : quote.purpose != null)
			return false;
		if (Double.compare(quote.ambientPeak, ambientPeak) != 0)
			return false;
		if (Double.compare(quote.ambientAverage, ambientAverage) != 0)
			return false;
		// end
		if (quoteItems.size() != quote.quoteItems.size())
			return false;

		if (!quoteItems.isEmpty()) {
			QuoteItemInfo arr1[] = new QuoteItemInfo[quoteItems.size()];
			QuoteItemInfo arr2[] = new QuoteItemInfo[quote.quoteItems.size()];

			quoteItems.toArray(arr1);
			quote.quoteItems.toArray(arr2);

			Arrays.sort(arr1);
			Arrays.sort(arr2);

			if (!Arrays.equals(arr1, arr2))
				return false;
		}
		// Front end
		if (additionalItems.size() != quote.additionalItems.size())
			return false;

		if (!additionalItems.isEmpty()) {
			QuoteItemInfo arr1[] = new QuoteItemInfo[additionalItems.size()];
			QuoteItemInfo arr2[] = new QuoteItemInfo[quote.additionalItems.size()];

			additionalItems.toArray(arr1);
			quote.additionalItems.toArray(arr2);

			Arrays.sort(arr1);
			Arrays.sort(arr2);

			if (!Arrays.equals(arr1, arr2))
				return false;
		}

		// end
		// if all conditions are true return true.
		return true;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = quoteId != null ? quoteId.hashCode() : 0;
		result = 31 * result + (validUntil != null ? validUntil.hashCode() : 0);
		result = 31 * result + (customerName != null ? customerName.hashCode() : 0);
		result = 31 * result + (dealerName != null ? dealerName.hashCode() : 0);
		result = 31 * result + (quoteItems != null ? quoteItems.hashCode() : 0);
		// Front end
		result = 31 * result + (additionalItems != null ? additionalItems.hashCode() : 0);
		result = 31 * result + (comments != null ? comments.hashCode() : 0);
		result = 31 * result + (terms != null ? terms.hashCode() : 0);
		result = 31 * result + (unitDescription != null ? unitDescription.hashCode() : 0);
		result = 31 * result + (purpose != null ? purpose.hashCode() : 0);

		temp = Double.doubleToLongBits(unitCost);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(height);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(width);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(depth);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(ambientPeak);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(ambientAverage);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		// end

		temp = Double.doubleToLongBits(totalCost);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(discount);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (city != null ? city.hashCode() : 0);
		result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
		result = 31 * result + (state != null ? state.hashCode() : 0);
		return result;
	}

}
