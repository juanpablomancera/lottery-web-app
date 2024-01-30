package lottery.event;

import lottery.lottery.Lottery;
import org.salespointframework.core.DataInitializer;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A {@link DataInitializer} implementation that creates default events for the lottery.
 * @author Jakub Gawroński, Jorge Andres Picon
 */
@Component
@Order(2)
class EventDataInitializer implements DataInitializer {

	@Autowired
	private final Lottery lottery;

	EventDataInitializer(Lottery lottery) {

		Assert.notNull(lottery, "Lottery must not be null");
		this.lottery = lottery;
		
	}

	/**
	 * Creates default events for the lottery.
	 */
	@Override
	public void initialize() {
		List.of(
			new LottoEvent(LocalDateTime.now().plusDays(4).format(DateTimeFormatter.ISO_LOCAL_DATE), 2),
			new LottoEvent(LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE), 2),
			new FootballEvent(LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE), LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE))
		).forEach(event -> lottery.addEvent(event));
	}
}
