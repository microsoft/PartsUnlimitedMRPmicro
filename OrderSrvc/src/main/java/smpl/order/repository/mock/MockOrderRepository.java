package smpl.order.repository.mock;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.opentracing.Span;
import smpl.order.BadRequestException;
import smpl.order.ConflictingRequestException;
import smpl.order.model.Order;
import smpl.order.model.OrderStatus;
import smpl.order.model.OrderUpdateInfo;
import smpl.order.repository.OrderRepository;
import smpl.order.repository.TestPath;

public class MockOrderRepository implements OrderRepository, TestPath {
	private final List<Order> orders;

	public MockOrderRepository() {
		this.orders = new ArrayList<>();
	}

	@Override
	public void reset() {
		orders.clear();
	}

	@Override
	public Order createOrder(String quoteId, Span span) throws BadRequestException {
		Order assocOrder = getOrderByQuoteId(quoteId, span);

		if (assocOrder != null) {
			throw new ConflictingRequestException(
					String.format("The quote has already been used to create an order: %s", assocOrder.getOrderId()));
		}

		Order result = new Order();
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

		result.setOrderDate(df.format(new Date()));
		result.setOrderId(String.format("order-%s", quoteId));
		result.setQuoteId(quoteId);
		result.setStatus(OrderStatus.Created);

		orders.add(result);

		return result;
	}

	@Override
	public boolean updateOrder(Order order, String id, Span span) {
		Order old = getOrder(id, span);
		if (old == null)
			return false;
		int idx = orders.indexOf(old);
		orders.set(idx, order);
		return true;
	}

	@Override
	public boolean removeOrder(String id, Span span) {
		Order old = getOrder(id, span);
		if (old == null)
			return false;
		int idx = orders.indexOf(old);
		orders.remove(idx);
		return true;
	}

	@Override
	public List<Order> getOrdersByDealerName(String dealer, OrderStatus status, Span span) {
		List<Order> lst = new ArrayList<>();
		return lst;
	}

	@Override
	public Order getOrder(String id, Span span) {
		for (Order order : orders) {
			if (order.getOrderId().equals(id)) {
				return order;
			}
		}
		return null;
	}

	@Override
	public Order getOrderByQuoteId(String id, Span span) {
		for (Order order : orders) {
			if (order.getQuoteId().equals(id)) {
				return order;
			}
		}
		return null;
	}

	@Override
	public List<Order> getOrdersByStatus(OrderStatus status, Span span) {
		List<Order> lst = new ArrayList<>();

		if (status == OrderStatus.None) {
			lst = orders;
		} else {
			for (Order order : orders) {
				if (order.getStatus() == status) {
					lst.add(order);
				}
			}
		}

		return lst;
	}

	@Override
	public boolean hasOrder(String id) {
		for (Order order : orders) {
			if (order.getOrderId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean updateOrder(String id, OrderUpdateInfo info, Span span) throws BadRequestException {
		Order old = getOrder(id, span);
		if (old == null)
			throw new BadRequestException("No such order");
		old.addEvent(info.getEventInfo());
		old.setStatus(info.getStatus());
		return true;
	}

	@Override
	public Order createOrderFB(String quoteId, Span span) throws BadRequestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateOrderFB(Order order, String id, Span span) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeOrderFB(String id, Span span) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Order> getOrdersByDealerNameFB(String dealer, OrderStatus status, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order getOrderFB(String id, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Order getOrderByQuoteIdFB(String id, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Order> getOrdersByStatusFB(OrderStatus status, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasOrderFB(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateOrderFB(String id, OrderUpdateInfo info, Span span) throws BadRequestException {
		// TODO Auto-generated method stub
		return false;
	}
}