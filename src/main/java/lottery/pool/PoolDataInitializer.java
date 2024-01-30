package lottery.pool;

import lottery.lottery.Lottery;
import org.salespointframework.core.DataInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import lottery.user.UserService;

@Component
@Order(4)
class PoolDataInitializer implements DataInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(PoolDataInitializer.class);

	private final PoolService poolManagement;
	private UserService userService;


	@Autowired
	private Lottery lottery;

	/**
	 * Creates new {@link PoolDataInitializer} with the given {@link PoolService} and {@link UserService}.
	 * 
	 * @param poolManagement must not be {@literal null}.
	 * @param lottery
	 * @param userService
	 */
	PoolDataInitializer(PoolService poolManagement, Lottery lottery, UserService userService) {

		Assert.notNull(poolManagement, "poolRepository must not be null!");
	
		this.poolManagement = poolManagement;
		this.userService = userService;
		this.lottery = lottery;
		
	}

	@Override
	public void initialize() {
        if (poolManagement.findAll().size() > 0) {
            return;
        }

		LOG.info("Creating default pools.");

		Pool pool = poolManagement.createPool("TestPool1", "123", "carlos@carlos");
		pool.addPoolMember(userService.getUserByEmail("pablo@pablo").orElse(null));
		pool.addPoolMember(userService.getUserByEmail("jakub@jakub").orElse(null));
		pool.addPoolMember(userService.getUserByEmail("peter@peter").orElse(null));
		pool.addEvent(lottery.getEventById(1L), Integer.valueOf(-1));
		pool.addEvent(lottery.getEventById(2L), Integer.valueOf(-1));
		this.poolManagement.save(pool);

		Pool pool2 = poolManagement.createPool("TestPool2", "123", "peter@peter");
		pool2.addPoolMember(userService.getUserByEmail("carlos@carlos").orElse(null));
		pool2.addPoolMember(userService.getUserByEmail("pablo@pablo").orElse(null));
		pool2.addEvent(lottery.getEventById(2L), Integer.valueOf(-1));
		pool2.addEvent(lottery.getEventById(3L), Integer.valueOf(5));
		this.poolManagement.save(pool2);

		Pool pool3 = poolManagement.createPool("TestPool3 mit einem sehr langen Namen", "123", "mareike@mareike");
		pool3.addPoolMember(userService.getUserByEmail("konrad@konrad").orElse(null));
		pool3.addPoolMember(userService.getUserByEmail("jorge@jorge").orElse(null));
		pool3.addEvent(lottery.getEventById(1L), Integer.valueOf(-1));
		pool3.addEvent(lottery.getEventById(2L), Integer.valueOf(-1));
		pool3.addEvent(lottery.getEventById(3L), Integer.valueOf(2));
		this.poolManagement.save(pool3);

	}
}
