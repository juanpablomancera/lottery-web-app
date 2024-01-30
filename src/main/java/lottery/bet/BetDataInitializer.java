package lottery.bet;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Data initializer to create demo bets
 *
 */
@Component
@Order(3)
public class BetDataInitializer implements DataInitializer{
    private final BetService betservice;
    
    public BetDataInitializer(BetService betservice){
        this.betservice = betservice;
    }

	/**
	 * Initializes the database with some bets
	 */
    @Override
    public void initialize(){
		BetState state = BetState.OPEN;
		Long user = Long.valueOf(1);
		Long event =  Long.valueOf(0);
		Long pool = Long.valueOf(-1);

        Integer[] games = {1, 2, 3, 4, 5, 6};
        Double amount = 10.0;
		
        TotoBet toto = new TotoBet(games, amount, state, user, event, pool);
		//betservice.saveBet(toto);

        Integer[] games2 = {3, 4, 18, 2, 15, 16};
        Double amount2 = 120.0;
        TotoBet toto2 = new TotoBet(games2, amount2, state, user, event, pool);
		//betservice.saveBet(toto2);

        Integer[] numbers = {11, 22, 13, 14, 5, 6};
        Integer supernumber = 19;
        Double price = 15.0;
        LottoBet lotto = new LottoBet(numbers, supernumber, price, state, user, event, pool);
		//betservice.saveBet(lotto);
    }
}