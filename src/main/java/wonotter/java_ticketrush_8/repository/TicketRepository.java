package wonotter.java_ticketrush_8.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wonotter.java_ticketrush_8.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
