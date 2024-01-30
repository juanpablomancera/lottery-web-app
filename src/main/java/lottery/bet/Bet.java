package lottery.bet;

import java.time.format.DateTimeFormatter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lottery.lottery.Lottery;

/**
 * Abstract class for all Bets
 * @see LottoBet
 * @see TotoBet
 */
@Entity
public abstract class Bet{

    private Long user, event;
    private BetState state;
    private String date;
	/**
	 * Amount that the Bet won after the drawing of an event
	 */
    private double amountWon;
    private @Id @GeneratedValue Long id;

    private Long poolId;

    public Bet(BetState state, Long user, Long event, Long poolId){
        this.state = state;
        this.user = user;
        this.event = event;
		this.poolId = poolId;
        this.amountWon = 0;

        this.date = Lottery.getBusinessTime().getTime().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public Bet(){}
    
    public String getDate() {
        return date;
    }

    public BetState getState() {
        return state;
    }

    public Long getUser() {
        return user;
    }

    public Long getEvent() {
        return event;
    }

    public double getAmountWon(){
        return amountWon;
    }

    public void setAmountWon(double amountWon){
        this.amountWon = amountWon;
    }

	public void setToWin(){
		this.state = BetState.WIN;
	}

	public void setToLose(){
		this.state = BetState.LOSE;
	}

	public void setToOpen(){
		this.state = BetState.OPEN;
	}

	public void setToCanceled(){
		this.state = BetState.CANCELED;
	}

    public Long getId() {
        return id;
    }

	public void setEvent(Long event) {
		this.event = event;
	}

	public Long getPoolId() {
        return poolId;
    }

	public abstract Double getPrice();

    public abstract void setPrice(Double price);

}
