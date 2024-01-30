package lottery.bet;
import org.springframework.validation.Errors;

import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

/**
 * Form to create new {@link TotoBet}
 */
public class TotoBetForm {
    private final Double amount;
    private final Long user;
    private final Long event;
    private final BetState state;
    private final Integer[] games;
    private boolean validCheckboxCount;

    public TotoBetForm(Double amount, @RequestParam("match") String match){
		games = new Integer[6];
		
	
		if (match != null){
			String[] stringsArray = match.split(",");
			if (stringsArray.length == 7 && amount > 0){
				this.validCheckboxCount = true;

				for(int i = 0; i < stringsArray.length-1; i++){
					games[i] = Integer.parseInt(stringsArray[i+1]);
				}
			}else{
				this.validCheckboxCount = false;
			}
		}else{
			this.validCheckboxCount = true;
		}

		
		this.amount = amount;
		this.user = Long.valueOf(1);
		this.event = Long.valueOf(1);
		this.state = BetState.OPEN;
    }

	/**
	 * Checks if the amount of matches selected for the bet is correct
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

    public Double getAmount(){
        return amount;
    }

    public Integer[] getGames(){
        return games;
    }

	/**
	 *
	 * @param user
	 * @param eventid
	 * @param poolid
	 * @return {@link TotoBet}
	 */
    TotoBet newTotoBet(Long user, Long eventid, Long poolid){
        return new TotoBet(getGames(), getAmount(), getState(), user, eventid, poolid);
    }
}