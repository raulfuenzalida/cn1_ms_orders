package duoc.cn1.ms_orders.repository;

import duoc.cn1.ms_orders.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	@Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :idOrder AND oi.idProduct = :idProduct")
	Optional<OrderItem> findByIdOrderAndIdProduct(@Param("idOrder") Long idOrder, @Param("idProduct") Long idProduct);

	@Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.order.id = :idOrder AND oi.idProduct = :idProduct")
	boolean existsByIdOrderAndIdProduct(@Param("idOrder") Long idOrder, @Param("idProduct") Long idProduct);
}
