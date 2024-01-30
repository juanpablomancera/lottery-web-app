package lottery.event;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lottery.bet.BetService;
import lottery.pool.PoolService;
import lottery.user.UserService;

import java.util.Map;

/**
 * Abstract class for all events
 * @see lottery.event.FootballEvent
 * @see lottery.event.LottoEvent
 */
@Entity
public abstract class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	private boolean isActive = true;

	/**
	 * Draws the event
	 * @param userService
	 * @param betService
	 * @param poolService
	 * @return Map of income and expenses
	 */
	abstract public Map<String, Double> drawEvent(UserService userService, BetService betService, PoolService poolService);
	public String adminWinningNumbers = "";

	/**
	 * Returns the id of the event
	 * @return id
	 */
	public Long getId(){
		return this.id;
	}

	/**
	 * Returns the type of the event
	 * @return type
	 */
	abstract public String getType();

	/**
	 * Returns the date of the event
	 * @return date
	 */
	abstract public String getDrawDate();

	/**
	 * Return whether the event is active or not
	 * @return isActive
	 */
	public  boolean isActive(){
		return this.isActive;
	}

	/**
	 * Deactivates the event
	 */
	public  void deactivate(){
		this.isActive = false;
	}

	/**
	 * Returns the manually set winning numbers that can be used to display the winning numbers in the UI
	 * or to override the automatically generated winning numbers
	 * @return manually set winning numbers
	 */
	public int[] getAdminWinningNumbers() {return null;}

	/**
	 * Returns the manually set winning numbers as a string that can be used to display the winning numbers in the UI
	 * @return String manually set winning numbers
	 */
	public String getAdminWinningNumbersString() {return adminWinningNumbers.replace("[", "").replace("]", "");}

	/**
	 * Sets the manually set winning numbers
	 * @param manualWinningNumbers manually set winning numbers
	 */
	public void setAdminWinningNumbers(String manualWinningNumbers){ this.adminWinningNumbers = manualWinningNumbers;}

}
