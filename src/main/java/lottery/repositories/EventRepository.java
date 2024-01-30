package lottery.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import lottery.event.Event;

/**
 * Interface in charge of representing and storing all the events created by the Lottery
 */
public interface EventRepository extends JpaRepository<Event, Long>{
}
