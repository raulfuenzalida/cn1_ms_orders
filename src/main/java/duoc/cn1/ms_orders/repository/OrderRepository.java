package duoc.cn1.ms_orders.repository;

import duoc.cn1.ms_orders.model.Order;
import duoc.cn1.ms_orders.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	Optional<Order> findById(Long id);

	List<Order> findByStatus(OrderStatus status);

	List<Order> findByCustomerNameContainingIgnoreCase(String customerName);

	List<Order> findByCustomerEmailContainingIgnoreCase(String customerEmail);
}
