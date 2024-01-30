package lottery.pool;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import lottery.user.User;
import lottery.user.UserService;
import lottery.bet.Bet;
import lottery.bet.BetService;
import lottery.repositories.PoolRepository;

@Service
@Transactional
public class PoolService {

    private final PoolRepository pools;

	@Autowired
    private UserService userService;
	@Autowired
	private BetService betService;

	/**
	 * Creates a new {@link PoolService} with the given {@link PoolRepository},
	 * {@link UserService} and {@link BetService}.
	 * 
	 * @param pools must not be {@literal null}.
	 * @param userService
	 * @param betService
	 */
    public PoolService(PoolRepository pools, UserService userService, BetService betService) {
        Assert.notNull(pools, "PoolRepository must not be null!");
        this.pools = pools;
        this.userService = userService;
		this.betService = betService;
    }

	/**
	 * Creates a new {@link Pool} using the information given in the {@link PoolForm}.
	 * 
	 * @param form must not be {@literal null}.
	 * @return the new {@link Pool} instance.
	 */
    public Pool createPoolFormForm(PoolForm form) {
        Assert.notNull(form, "Pool form must not be null!");
        var poolChef = userService.getUserByEmail(form.getPoolChef()).orElseThrow();
        return pools.save(new Pool(form.getName(), form.getPassword(), poolChef));
    }

	/**
	 * Creates a new {@link Pool}.
	 * 
	 * @param name
	 * @param password
	 * @param poolChefEmail must associate with existing {@link User}.
	 * @return the new {@link Pool} instance.
	 */
    public Pool createPool(String name, String password, String poolChefEmail) {
        var poolChef = userService.getUserByEmail(poolChefEmail).orElseThrow();
    	return pools.save(new Pool(name, password, poolChef));
    }

	/**
	 * Checks whether the @param name is used on a {@link Pool} as a name already.
	 * 
	 * @param name
	 * @return boolean
	 */
	public boolean poolNameIsUsed(String name) {
		for (var pool : this.pools.findAll()) {
            if (pool.getName().contentEquals(name)) {
				return true;
            }
        }
		return false;
	}

	/**
	 * Devides the @param winnings of a {@link Pool} evenly between its members depending on the betted amount.
	 * 
	 * @param poolId
	 * @param eventId
	 * @param winnings
	 */
	public void drawEventPool(Long poolId, Long eventId, double winnings) {
		double total_price = 0;
		List<Bet> bets = betService.getBetsByEventAndPool(eventId, poolId);
		for (var bet : bets) {
				total_price += bet.getPrice();
		}

		for (var bet : bets) {
			 var user = userService.getUserById(bet.getUser()).orElseThrow();
			 var fac = bet.getPrice()/total_price;
			 double winningsShare = fac*winnings;
			 user.addMoney(winningsShare);
			 bet.setAmountWon(winningsShare);
			 betService.saveBet(bet);
			 System.out.println(user.getUserAccount().getUsername() + " got "+fac*winnings + " from EventId " +eventId);
		}
		Pool pool = this.findById(poolId).orElseThrow();

		pool.addBalance(winnings);
		pool.subBalance(total_price);
	}

    public List<Pool> findAll() {
        return pools.findAll();
    }

    public Optional<Pool> findById(Long id) {
        return pools.findById(id);
    }
    
    public void save(Pool pool) {
        pools.save(pool);
    }

    public void deleteById(Long id) {
        pools.deleteById(id);
    }

	/**
	 * Checks whether a {@link User} is a member or poolChef of any {@link Pool}.
	 * 
	 * @param user
	 * @return boolean
	 */
	public boolean isUserInPool(User user){
		for(var pool : pools.findAll()){
			if(pool.getPoolMembers().contains(user)){
				return true;
			}
			if(pool.getPoolChef().equals(user)){
				return true;
			}
		}
		return false;
	}
}
