package lottery.event;

import jakarta.validation.constraints.NotEmpty;
import lottery.lottery.Lottery;

import java.time.format.DateTimeFormatter;

/**
 * This class is used to create a form for the creation of a toto event.
 * @author Jakub Gawroński, Jorge Andres Picon
 */
public class TotoEventForm {
	
	@NotEmpty(message = "Draw date has to be picked")
	private final String drawDate;

	@NotEmpty(message = "Start date has to be picked")
	private final String startDate;

	// isDateValidFrontendValue is only used in templates.add-toto-event for frontend purpose.
	private boolean isDateValidFrontendValue = true;

	public TotoEventForm(String drawDate, String startDate) {
		this.drawDate = drawDate;
		this.startDate = startDate;
	}

	/**
	 * Check if the date is valid, meaning that the draw date is after the start date and that the draw date is after today.
	 * @return true if the date is valid, false otherwise.
	 */
	public boolean isDateValid() {
		String todayDate = Lottery.getBusinessTime().getTime().format(DateTimeFormatter.ISO_LOCAL_DATE);
		if (todayDate.compareTo(getStartDate()) > 0 || todayDate.compareTo(getDrawDate()) > 0){
			this.isDateValidFrontendValue = false;
			return false;
		}else {
			this.isDateValidFrontendValue = getStartDate().compareTo(getDrawDate()) < 0;
			return this.isDateValidFrontendValue;
		}
	}

	/**
	 * Get the draw date.
	 * @return draw date
	 */
	public String getDrawDate() {
		return drawDate;
	}

	/**
	 * Get the start date.
	 * @return start date
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * Get the isDateValidFrontendValue.
	 * @return isDateValidFrontendValue
	 */
	public boolean getIsDateValidFrontendValue(){
		return this.isDateValidFrontendValue;
	}
}
