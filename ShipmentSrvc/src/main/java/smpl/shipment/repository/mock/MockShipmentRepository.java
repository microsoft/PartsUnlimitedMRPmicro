package smpl.shipment.repository.mock;

import java.util.ArrayList;
import java.util.List;

import io.opentracing.Span;
import smpl.shipment.BadRequestException;
import smpl.shipment.model.OrderStatus;
import smpl.shipment.model.ShipmentEventInfo;
import smpl.shipment.model.ShipmentRecord;
import smpl.shipment.repository.ShipmentRepository;
import smpl.shipment.repository.TestPath;

public class MockShipmentRepository implements ShipmentRepository, TestPath {
	private final List<ShipmentRecord> records = new ArrayList<>();

	public MockShipmentRepository() {
	}

	@Override
	public void reset() {
		records.clear();
	}

	@Override
	public List<ShipmentRecord> getShipments(OrderStatus status, Span span) {
		List<ShipmentRecord> result = new ArrayList<>();
		for (ShipmentRecord record : records) {
			result.add(new ShipmentRecord(record));
		}
		return result;
	}

	@Override
	public ShipmentRecord getShipmentById(String id, Span span) {
		for (ShipmentRecord record : records) {
			if (record.getOrderId().equals(id)) {
				return new ShipmentRecord(record);
			}
		}
		return null;
	}

	@Override
	public ShipmentRecord createShipment(ShipmentRecord info, Span span) throws BadRequestException {
		ShipmentRecord existing = getShipmentById(info.getOrderId(), null);
		if (existing != null) {
			throw new BadRequestException(
					String.format("A shipment record for order '%s' already exists", info.getOrderId()));
		}

		ShipmentRecord result = new ShipmentRecord(info);
		records.add(result);
		return result;
	}

	@Override
	public boolean addEvent(String id, ShipmentEventInfo event, Span span) {
		ShipmentRecord existing = null;

		for (ShipmentRecord record : records) {
			if (record.getOrderId().equals(id)) {
				existing = record;
				break;
			}
		}

		if (existing == null)
			return false;

		existing.addEvent(new ShipmentEventInfo(event.getDate(), event.getComments()));

		return true;
	}

	@Override
	public boolean updateShipment(ShipmentRecord info, Span span) {
		int idx = -1;
		String id = info.getOrderId();

		for (int i = 0; i < records.size(); ++i) {
			ShipmentRecord record = records.get(i);
			if (record.getOrderId().equals(id)) {
				idx = i;
				break;
			}
		}

		if (idx == -1)
			return false;

		// Replace shipment in the same location

		records.set(idx, new ShipmentRecord(info));

		return true;
	}

	@Override
	public boolean removeShipment(String id, String eTag, Span span) {
		int idx = -1;
		for (int i = 0; i < records.size(); ++i) {
			ShipmentRecord record = records.get(i);
			if (record.getOrderId().equals(id)) {
				idx = i;
				break;
			}
		}
		if (idx == -1)
			return false;
		else {
			records.remove(idx);
			return true;
		}
	}

	@Override
	public List<ShipmentRecord> getShipmentsFB(OrderStatus status, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ShipmentRecord getShipmentByIdFB(String id, Span span) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ShipmentRecord createShipmentFB(ShipmentRecord info, Span span) throws BadRequestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addEventFB(String id, ShipmentEventInfo event, Span span) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateShipmentFB(ShipmentRecord info, Span span) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeShipmentFB(String id, String eTag, Span span) {
		// TODO Auto-generated method stub
		return false;
	}
}