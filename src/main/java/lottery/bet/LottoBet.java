package lottery.bet;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

/**
 * Extended class from Bet, representing a Bet placed for a {@link lottery.event.LottoEvent}.
 */
@Entity
public class LottoBet extends Bet{
    private Integer supernumber;
    private Double price;
    private Integer[] lotterynumbers;

	private LocalDateTime eventDate;

    public LottoBet(Integer[] lotterynumbers, Integer supernumber, Double price,
					BetState state, Long user, Long event, Long poolID){
        super(state, user, event, poolID);
        this.price = price;
        this.supernumber = supernumber;
        this.lotterynumbers = lotterynumbers;
    }

	public LottoBet(Integer[] lotterynumbers, Integer supernumber, Double price, BetState state, Long user, Long event,
					Long poolID, LocalDateTime eventDate){
		super(state, user, event, poolID);
		this.price = price;
		this.supernumber = supernumber;
		this.lotterynumbers = lotterynumbers;
		this.eventDate = eventDate;
	}

    public LottoBet() {
        super();
    }

    public Integer[] getLotteryNumbers(){
        return lotterynumbers;
    }
    public Double getPrice(){
        return price;
    }

    public Integer getSupernumber(){
        return supernumber;
    }

	public LocalDateTime getEventDate(){return eventDate;}

    public void setPrice(Double price){
		if(price > 0){
			this.price = price;
		}
	}
    
}
