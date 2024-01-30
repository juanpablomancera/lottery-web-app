package lottery.bet;

import java.util.*;

import org.springframework.stereotype.Service;

import lottery.repositories.BetRepository;
import lottery.event.Event;

/**
 * class for Services with Bets and Bet repository
 */
@Service
public class BetService {
    private final BetRepository repository;

	/**
	 * Creates a new {@link BetService} with the given {@link BetRepository}
	 * @param repository
	 */
    public BetService(BetRepository repository){
        this.repository = repository;
    }

    public void saveBet(Bet bet){
        repository.save(bet);
    }

	public List<Bet> getAllBets(){
		return repository.findAll();
	}

    public boolean deleteBetById(Long betid){
        if(repository.findById(betid).isPresent()){
            repository.deleteById(betid);
            return true;
        }
        return false;
    }

    public List<Bet> getBetsByEvent(Long event){
        return getAllBets().stream().filter(bet -> Objects.equals(bet.getEvent(), event)).toList();
    }

	/**
	 * Searches for Bets with the given {@link lottery.event.Event} and {@link lottery.pool.Pool}
	 * @param event Event ID
	 * @param pool Pool ID
	 * @return List of Bets found
	 */
    public List<Bet> getBetsByEventAndPool(Long event, Long pool){
        List<Bet> betsinevent = getBetsByEvent(event);
        List<Bet> bets = new ArrayList<Bet>();
        for(Bet bet : betsinevent){
            if(bet.getPoolId() == pool){
                    bets.add(bet);
            }
        }
        return bets;
    }

    public Collection<Bet> getAllLottoBets(){
        Collection<Bet> lottobets = new ArrayList<>();
        for(Bet bet : repository.findAll()) {
            if(bet instanceof LottoBet){
                lottobets.add(bet);
            }
        }
        return lottobets;
    }

	/**
	 * Searches for Bets placed by the given {@link lottery.user.User}
	 * @param userId
	 * @return List of Bets placed by the given {@link lottery.user.User}
	 */
	public List<Bet> getAllBetsByUserId(Long userId){
		return repository.findAll().stream().filter(bet -> Objects.equals(bet.getUser(), userId)).toList();
	}

    public Collection<Bet> getAllTotoBets(){
        Collection<Bet> totobets = new ArrayList<>();
        for(Bet bet : repository.findAll()) {
            if(bet instanceof TotoBet){
                totobets.add(bet);
            }
        }
        return totobets;
    }

	public Optional<Bet> getBetById(long id){
		for (Bet currentBet : getAllBets()) {
			if(currentBet.getId() == id){
				return Optional.of(currentBet);
			}
		}
		return Optional.empty();
	}

    public Optional<Long> getEventIdByBetId(long id) {
        return Optional.of(getBetById(id).orElseThrow(null).getEvent());
    }
}
