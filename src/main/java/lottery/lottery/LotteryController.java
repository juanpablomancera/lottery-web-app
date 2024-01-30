package lottery.lottery;

import jakarta.validation.Valid;
import lottery.bet.*;
import lottery.event.*;
import lottery.pool.PoolService;
import lottery.user.UserService;

import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Webcontroller class for Lottery related mappings
 * @author Jorge Andres Picon Colmenares, Jakub Gawroński
 */
@Controller
public class LotteryController {

	private final Lottery lottery;
	private final BusinessTime businessTime;

	@Autowired
	private UserService userService;

	@Autowired
	private PoolService poolService;

	@Autowired
	private BetService betService;


	public LotteryController(Lottery lottery, BusinessTime businessTime, UserService userService, PoolService poolService, BetService betService) {
		if (lottery == null || businessTime == null || userService == null || poolService == null || betService == null){
			throw new NullPointerException("Arguments must not be <<null>> in LotteryController.LotteryController()");
		}
		this.lottery = lottery;
		this.businessTime = businessTime;
		this.userService = userService;
		this.poolService = poolService;
		this.betService = betService;
	}

	/**
	 *
	 * @param model
	 * @return Lottery home page with addition of active event list
	 */
	@GetMapping("/lottery")
	public String getLottery(Model model){
		// Add all active events to the list
		model.addAttribute("events", lottery.getAllActiveEvents());

		return "lottery";
	}

	/**
	 *
	 * @param model
	 * @param form
	 * @return show admin page with form to create new Lotto type event
	 */
	@GetMapping("/lotto")
	@PreAuthorize("hasRole('ADMIN')")
	public String getLottoPage(Model model, @ModelAttribute(binding = false) LottoEventForm form){
		// A controller for the add-lotto-event template
		model.addAttribute("form", form);
		return "add-lotto-event";
	}

	/**
	 *
	 * @param model
	 * @param form
	 * @return show admin page with form to create new Football type event
	 */
	@GetMapping("/toto")
	@PreAuthorize("hasRole('ADMIN')")
	public String getTotoPage(Model model, @ModelAttribute(binding = false) TotoEventForm form){
		// A controller for the add-toto-event
		model.addAttribute("form", form);
		return "add-toto-event";
	}

	/**
	 * New Football event creation and Form controller method, add Football event to the lottery
	 * @param form
	 * @param errors
	 * @param model
	 * @return Redirect to admin panel if event Registration was successful, otherwise redirect
	 * to back to the event creation form page
	 */
	@PostMapping(path="/toto")
	@PreAuthorize("hasRole('ADMIN')")
	String addTotoEvent(@Valid @ModelAttribute("form") TotoEventForm form, Errors errors, Model model){
		if(errors.hasErrors() || !form.isDateValid()){
			System.out.println(errors);
			return getTotoPage(model, form);
		}

		FootballEvent event = new FootballEvent(form.getDrawDate(), form.getStartDate());

		lottery.addEvent(event);

		return "redirect:/adminPanel";
	}

	/**
	 * New Lotto event creation and Form controller method, add Lotto event to the lottery
	 * @param form
	 * @param price
	 * @param errors
	 * @param model
	 * @return Redirect to admin panel if event Registration was successful, otherwise redirect
	 * 	 * to back to the event creation form page
	 */
	@PostMapping(path="/lotto")
	@PreAuthorize("hasRole('ADMIN')")
	String addLottoEvent(@Valid @ModelAttribute("form") LottoEventForm form, @RequestParam("price") Integer price, Errors errors, Model model){
		// A function for adding new lotto events on form submission
		if(errors.hasErrors() || !form.isDateValid() || price < 1){
			System.out.println(errors);
			return getLottoPage(model, form);
		}

		LottoEvent event = new LottoEvent(form.getDrawDate(), price);

		lottery.addEvent(event);

		List<Bet> bets = betService.getBetsByEvent(-1L);

		// If there are long term bets bought on this date, change their ID to the ID of the new event
		for (Bet bet : bets) {
			LottoBet lottoBet = (LottoBet) bet;
			if(Objects.equals(lottoBet.getEventDate().format(DateTimeFormatter.ISO_LOCAL_DATE), form.getDrawDate())){
				if(userService.getUserById(lottoBet.getUser()).isPresent()
					&& userService.getUserById(lottoBet.getUser()).get().getAccountState() >= event.getPrice()){
					userService.getUserById(lottoBet.getUser()).get().substractMoney(Double.valueOf(event.getPrice()));
					lottoBet.setPrice(event.getPrice().doubleValue());
					lottoBet.setEvent(event.getId());
					betService.saveBet(bet);
				} else {
					lottoBet.setPrice(event.getPrice().doubleValue());
					lottoBet.setToCanceled();
					lottoBet.setEvent(event.getId());
					betService.saveBet(bet);
				}
			}
		}

		return "redirect:/adminPanel";
	}

	/**
	 * Testing Function to the automatic drawing of events. Fast-forward the days to simulate the transition of time
	 * @param model
	 * @return redirect to adminpanel
	 */
	@PostMapping(path="/lottery/skipday")
	@PreAuthorize("hasRole('ADMIN')")
	String skipDay(Model model){
		// A function for testing the automatic drawing of events
		// Passes the time but 24 hours triggering the onDayPassed event listener of Lottery class
		Duration duration = Duration.ofHours(24);
		businessTime.forward(duration);
		return "redirect:/adminPanel";
	}

	/**
	 * Controller function for a form filled in by the Admin to generate a manual draw of a Lotto type Event.
	 * @param form
	 * @param errors
	 * @param number Winning numbers manually added by Admin (6 numbers from 1-49 of 49) for this event
	 * @param supernumber Winning Super number manually added by Admin (0 to 9) for this event
	 * @param eventid
	 * @return Redirect to admin panel if draw was successful, otherwise redirect back to the Draw Lotto Event Form.
	 */
	@PostMapping(path="/lottery/draw/{eventid}/lottoWinningBet")
	@PreAuthorize("hasRole('ADMIN')")
	String drawLotteryEventManually(@Valid LottoBetForm form, Errors errors, @RequestParam("number") String number, @RequestParam("supernumber") String supernumber, @PathVariable long eventid){

		int numberLength = number.split(",").length;
		int superNumberLength = supernumber.split(",").length;
		if (errors.hasErrors() ||
				(numberLength == 7 && superNumberLength != 2) ||
				(numberLength == 1 && superNumberLength != 1) ||
				(numberLength != 7 && numberLength != 1) ||
				(superNumberLength != 2 && superNumberLength != 1)
		){
			System.out.println(errors);
			return "redirect:/{eventid}/lottoWinningBet";
		}
		// Draw the event manually
		Event event = lottery.getEventById(eventid);

		// Adding to the Event the Winning Numbers selected by Admin, if they were selected.
		if (numberLength == 7 && superNumberLength == 2){
			// string range from 3 to skip form-generated prefix "-1,".
			event.setAdminWinningNumbers(number.substring(3)+supernumber.substring(2));
		}

		// Update the income and expenses of the lottery
		lottery.updateFinancialSituation(event.drawEvent(userService, betService, poolService));
		event.deactivate();
		lottery.addEvent(event);

		return "redirect:/adminPanel";
	}

	/**
	 * Function to generate a manual draw for any type of event.
	 * @param id
	 * @param model
	 * @return Redirect to admin panel
	 */
	@PostMapping(path="/lottery/draw/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	String drawLotteryManually(@PathVariable Long id, Model model){

		// Draw the event manually
		Event event = lottery.getEventById(id);

		// Update the income and expenses of the lottery
		lottery.updateFinancialSituation(event.drawEvent(userService, betService, poolService));
		event.deactivate();
		lottery.updateEvent(event);

		return "redirect:/adminPanel";
	}

	/**
	 *
	 * @param id
	 * @param loggedIn
	 * @param model
	 * @return Redirect to Football Bet purchase form
	 */
	@PostMapping(path="/lottery/FootballEvent/{id}")
	String openTotoBet(@PathVariable Long id, @LoggedIn Optional<UserAccount> loggedIn, Model model){

		return "redirect:/{id}/totobet";
	}

	/**
	 *
	 * @param id
	 * @param loggedIn
	 * @param model
	 * @return Redirect to Lotto Bet purchase form
	 */
	@PostMapping(path="/lottery/LottoEvent/{id}")
	String openLottoBet(@PathVariable Long id, @LoggedIn Optional<UserAccount> loggedIn, Model model){

		return "redirect:/{id}/lottobet";
	}

	/**
	 *
	 * @param id
	 * @param model
	 * @return Redirect to Lotto Bet draw form
	 */
	@PostMapping(path="/adminPanel/LottoEvent/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	String openWinningLottoBet(@PathVariable Long id, Model model){

		return "redirect:/{id}/lottoWinningBet";
	}

	/**
	 *
	 * @param loggedIn
	 * @return Current User Id if was found, otherwise if not found 0
	 */
	@ModelAttribute("currentUserId")
	Long currentUserId(@LoggedIn Optional<UserAccount> loggedIn) {
		if (loggedIn.isPresent()) {
			return userService.getUserByAccountIdentifier(loggedIn.get().getId()).orElseThrow().getId();
		}
		return 0L;
	}

	@ModelAttribute("currentUserName")
	String currentUserName(@LoggedIn Optional<UserAccount> logedIn) {
		if (logedIn.isPresent()) {
			return logedIn.get().getUsername();
		}
		return "Anonym";
	}
}

