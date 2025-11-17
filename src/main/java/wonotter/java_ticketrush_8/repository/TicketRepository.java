package wonotter.java_ticketrush_8.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wonotter.java_ticketrush_8.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // 비관적(배타적) 락 적용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    Optional<Ticket> findByIdWithPessimisticLock(@Param("id") Long id);

    // 낙관적 락 적용
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    Optional<Ticket> findByIdWithOptimisticLock(@Param("id") Long id);
}
