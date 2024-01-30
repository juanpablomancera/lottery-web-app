package lottery.lottery;

import lottery.bet.BetService;
import lottery.event.Event;
import lottery.pool.PoolService;
import lottery.repositories.EventRepository;
import lottery.user.UserService;
import org.salespointframework.time.BusinessTime;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.text.DecimalFormat;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This class represents the Lottery itself. It is responsible for managing the events, drawing them and updating the
 * financial situation of the lottery.
 * @author Jakub Gawroński, Jorge Andres Picon Colmenares
 */
@Service
public class Lottery {

	private double income;
	private double expenses;
	private final EventRepository events;
	private static BusinessTime businessTime = null;

	private final UserService userService;

	private final BetService betService;
	private final PoolService poolManagement;

	public EventRepository getEvents() {
		return events;
	}

	public static BusinessTime getBusinessTime() {
		return businessTime;
	}


	public Lottery(BusinessTime businessTime, EventRepository eventRepository , UserService userService, BetService betService, PoolService poolManagement){
		if (businessTime == null || eventRepository == null || userService == null || betService == null || poolManagement == null){
			throw new NullPointerException("Arguments must not be <<null>> in Lottery.Lottery()");
		}
		Lottery.businessTime = businessTime;
		this.events = eventRepository;
		this.userService = userService;
		this.betService = betService;
		this.income = 0;
		this.expenses = 0;
		this.poolManagement = poolManagement;
	}


	/**
	 * This method adds a new event to the lottery. It checks if the event is not null and if the draw date is in the
	 * future. If the event is valid, it is added to the events collection.
	 * @param event The event to be added to the lottery
	 * @return true if the event was added successfully, false otherwise
	 */
	public boolean addEvent(Event event){
		if (event == null){
			throw new NullPointerException("Event must not be <<null>> in Lottery.addEvent()");
		}

		if(businessTime.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE).compareTo(event.getDrawDate()) < 0){
			events.save(event);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method updates an event in the event repository
	 * @param event The event to be updated in the lottery
	 */
	public void updateEvent(Event event){
		if (event == null){
			throw new NullPointerException("Event must not be <<null>> in Lottery.updateEvent()");
		}
		events.save(event);
	}

	/**
	 * This method removes an event from the lottery. It checks if the event is present in the events repository and
	 * if it is, it removes it.
	 * @param eventId The id of the event to be removed
	 * @return true if the event was removed successfully, false otherwise
	 */
	public boolean removeEvent(Long eventId){
		if(events.findById(eventId).isPresent()){
			events.deleteById(eventId);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method returns all the events in the lottery that are active
	 * @return A list of all the active events in the lottery
	 */
	public List<Event> getAllActiveEvents(){
		List<Event> active_events = new ArrayList<>();

		for(Event e : events.findAll()){
			if(e.isActive()){
				active_events.add(e);
			}
		}

		return active_events;
	}

	/**
	 * This method returns all the events in the lottery that are inactive
	 * @return A list of all the inactive events in the lottery
	 */
	public List<Event> getAllInactiveEvents(){
		List<Event> inactive_events = new ArrayList<>();

		for(Event e : events.findAll()){
			if(!e.isActive()){
				inactive_events.add(e);
			}
		}

		return inactive_events;
	}

	/**
	 * This method returns a specific event in the lottery by its id
	 * @param id The id of the event to be returned
	 * @return The event with the specified id
	 * @throws IllegalArgumentException if the event with the specified id is not present in the events repository
	 */
	public Event getEventById(Long id) throws IllegalArgumentException{
		if(events.findById(id).isPresent()){
			return events.findById(id).get();
		} else {
			return null;
		}
	}

	/**
	 * This function updates the financial situation of the lottery after an event is drawn
	 * @param eventDrawFinancialResults A map containing the income and expenses of the lottery after the event is drawn
	 */
	public void updateFinancialSituation(Map<String, Double> eventDrawFinancialResults){
		// Calculate the new financial situation after the lottery is drawn
		this.income += eventDrawFinancialResults.get("income");
		this.expenses += eventDrawFinancialResults.get("expenses");
	}

	/**
	 * This function is an event listener that fires automatically when the date changes (after each day). It checks
	 * which events have the draw date set for today and draws them.
	 * @param time The time that triggers the function
	 */
	@EventListener
	public void onDayChange(BusinessTime.DayHasPassed time){
		// An event listener that fires automatically when the date changes (after each day)

		// Get the current time in the "YYYY-MM-DD" format and compare it with the Draw Date
		String current_time = businessTime.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE);

		// Iterate over all events and check which ones have the draw date set for today, then draw those events and
		// remove them from the events collection
		for(Event event : this.getAllActiveEvents()){
			if(current_time.equals(event.getDrawDate())){
				this.updateFinancialSituation(event.drawEvent(userService, betService, poolManagement));
				event.deactivate();
				this.updateEvent(event);
			}
		}
	}

	/**
	 * This function returns the current date of the lottery
	 * @return The current date of the lottery
	 */
	public String getTime(){
		return businessTime.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	/**
	 * This function sets the current income of the lottery
	 * @param income The new income of the lottery
	 */
	public void setIncome(double income) {
		this.income = income;
	}

	/**
	 * This function sets the current expenses of the lottery
	 * @param expenses The new expenses of the lottery
	 */
	public void setExpenses(double expenses) {
		this.expenses = expenses;
	}

	/**
	 * This function returns the current income of the lottery
	 * @return The current income of the lottery
	 */
	public double getIncome() {
		DecimalFormat df = new DecimalFormat("0.00");
		return Double.valueOf(df.format(income).replaceAll(",", "."));
	}

	/**
	 * This function returns the current expenses of the lottery
	 * @return The current expenses of the lottery
	 */
	public double getExpenses() {
		DecimalFormat df = new DecimalFormat("0.00");
		return Double.valueOf(df.format(expenses).replaceAll(",", "."));
	}
}
