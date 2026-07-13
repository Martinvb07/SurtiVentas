package com.surtiventas.backend.geo;

import com.surtiventas.backend.customer.Customer;
import com.surtiventas.backend.customer.CustomerRepository;
import com.surtiventas.backend.geo.dto.DeliveryPoint;
import com.surtiventas.backend.geo.dto.StorePoint;
import com.surtiventas.backend.order.Order;
import com.surtiventas.backend.order.OrderRepository;
import com.surtiventas.backend.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read models for the route maps: stores geolocated for the seller, and the
 * driver's assigned deliveries geolocated by their store.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GeoService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public List<StorePoint> stores() {
        return customerRepository.findWithCoordinates().stream()
                .map(this::toStorePoint)
                .toList();
    }

    public List<DeliveryPoint> deliveries(Long driverId) {
        return orderRepository
                .findByDriverAndStatus(driverId, OrderStatus.ASIGNADO_RUTA, PageRequest.of(0, 200)).stream()
                .filter(order -> order.getCustomer().getLatitude() != null
                        && order.getCustomer().getLongitude() != null)
                .map(this::toDeliveryPoint)
                .toList();
    }

    private StorePoint toStorePoint(Customer c) {
        return new StorePoint(
                c.getId(),
                c.getStoreName(),
                c.getOwnerName(),
                c.getAddress(),
                c.getLatitude().doubleValue(),
                c.getLongitude().doubleValue(),
                c.getClassification().name());
    }

    private DeliveryPoint toDeliveryPoint(Order order) {
        Customer c = order.getCustomer();
        return new DeliveryPoint(
                order.getId(),
                order.getOrderNumber(),
                c.getStoreName(),
                c.getAddress(),
                c.getLatitude().doubleValue(),
                c.getLongitude().doubleValue(),
                order.getTotalAmount());
    }
}
