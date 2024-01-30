package lottery.bet;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

/**
 * Form to create new {@link LottoBet}
 */
public class LottoBetForm {
    private Integer supernumber;
    private final Long user;
    private final Long event;
    private final BetState state;
    private final Integer[] lotterynumbers;
    private boolean validCheckboxCount;

    public LottoBetForm(@RequestParam("number") String number, @RequestParam("supernumber") String supernumber){
        
        this.lotterynumbers = new Integer[6];
        this.supernumber = null;

        if (number != null && supernumber != null) {
            String[] numberStringsArray = number.split(",");
            String[] sNumberStringsArray = supernumber.split(",");
        
            if (numberStringsArray.length == 1 && sNumberStringsArray.length == 1) {
                this.validCheckboxCount = true;
            } else if (numberStringsArray.length == 7 && sNumberStringsArray.length == 2) {
                this.validCheckboxCount = true;
        
                for (int i = 0; i < numberStringsArray.length - 1; i++) {
                    lotterynumbers[i] = Integer.parseInt(numberStringsArray[i + 1]);
                }
        
                this.supernumber = Integer.parseInt(sNumberStringsArray[1]);
            } else {
                this.validCheckboxCount = false;
            }
        } else {
            this.validCheckboxCount = true;
        }
        
        this.user = Long.valueOf(1);
        this.event = Long.valueOf(1);
        this.state = BetState.OPEN;
    }

	/**
	 * Checks if the amount of numbers selected for the bet is correct
	 * @return
	 */
    public boolean isValidCheckboxCount() {
        return validCheckboxCount;
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

    public Integer getSupernumber(){
        return supernumber;
    }

    public Integer[] getLotteryNumbers(){
        return lotterynumbers;
    }

	/**
	 * Creates new Lotto Bet with the information the form received.
	 * @param userid
	 * @param eventid
	 * @param poolid
	 * @param price
	 * @return {@link LottoBet}
	 */
    LottoBet newLottoBet(Long userid, Long eventid, Long poolid, Integer price){
        return new LottoBet(getLotteryNumbers(), getSupernumber(), 
				   price.doubleValue(), getState(), userid, eventid, poolid);
    }

	/**
	 * Creates new Lotto Bet with the information the form received.
	 * @param userid
	 * @param eventid
	 * @param poolid
	 * @param price
	 * @param eventDate
	 * @return {@link LottoBet}
	 */
	LottoBet newLottoBet(Long userid, Long eventid, Long poolid, Integer price, LocalDateTime eventDate){
		return new LottoBet(getLotteryNumbers(), getSupernumber(), 
				   price.doubleValue(), getState(), userid, eventid, poolid, eventDate);
	}

}
