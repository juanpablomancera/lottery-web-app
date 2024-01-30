package lottery.bet;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

/**
 * Extended class from Bet, representing a Bet placed for a {@link lottery.event.FootballEvent}
 */
@Entity
public class TotoBet extends Bet{
    private Double amount;
    private Integer[] games;

    public TotoBet(Integer[] games, Double amount, BetState state, Long user, Long event, Long poolID){
        super(state, user, event, poolID);
        this.amount = amount;
        this.games = games;
    }

    public TotoBet() {
        super();
    }

    public Integer[] getGames(){
        return games;
    }

    public Double getPrice(){
        return amount;
    }

	public void setPrice(Double price){
        if(price > 0){
        this.amount = price;
        }    
    }
}
