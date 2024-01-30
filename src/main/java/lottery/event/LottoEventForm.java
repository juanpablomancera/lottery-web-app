package lottery.event;

import jakarta.validation.constraints.NotEmpty;
import lottery.lottery.Lottery;

import java.time.format.DateTimeFormatter;

/**
 * This class is used to create a form for the creation of a Lotto event.
 * @author Jorge Andres Picon Colmenares, Jakub Gawroński
 */
public class LottoEventForm {

	@NotEmpty(message = "Draw date has to be picked")
	private final String drawDate;
	// isDateValidFrontendValue is only used in templates.add-lotto-event for frontend purpose.
	private boolean isDateValidFrontendValue = true;
	public LottoEventForm(String drawDate) {
		this.drawDate = drawDate;
	}

	/**
	 * Function to check that a draw date for this event added by the admin is correct, that is, it cannot be a day in
	 * the past or current day
	 * @return returns boolean value depending on whether date is correct or false
	 */
	public boolean isDateValid() {
		if(getDrawDate() == null || getDrawDate().equals("")){
			this.isDateValidFrontendValue = false;
			return false;
		}
		this.isDateValidFrontendValue = Lottery.getBusinessTime().getTime().format(DateTimeFormatter.ISO_LOCAL_DATE).compareTo(getDrawDate()) < 0;
		return this.isDateValidFrontendValue;
	}

	/**
	 *
	 * @return Draw Date for this Event
	 */
	public String getDrawDate() {
		return this.drawDate;
	}

	public boolean getIsDateValidFrontendValue(){
		return this.isDateValidFrontendValue;
	}
}
