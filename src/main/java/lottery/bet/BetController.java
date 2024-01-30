package lottery.bet;


import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import lottery.pool.PoolService;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.util.Assert;

import jakarta.validation.Valid;
import lottery.user.UserService;
import lottery.event.LottoEvent;
import lottery.lottery.Lottery;
import lottery.event.Event;
import lottery.event.FootballEvent;
import lottery.pool.Pool;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Webcontroller class for bet related mappings
 */

@Controller
public class BetController {
	private final BetService betService;

	@Autowired
	private UserService userService;

	@Autowired
	private PoolService poolManagement;

	private Lottery lottery;

	BetController(BetService betService, UserService userService, Lottery lottery) {

		Assert.notNull(betService, "Bet service can not be null");
		this.betService = betService;
		this.userService = userService;
		this.lottery = lottery;
	}

	/**
	 * Opens a {@link TotoBetForm} for a {@link lottery.user.User} to place a {@link TotoBet}
	 *
	 * @param model
	 * @param form
	 * @param loggedIn
	 * @param eventid
	 * @param insufficientBalance
	 * @param isTooLate
	 * @return
	 */
	@GetMapping("/{eventid}/totobet")
	public String openTotoBetPage(Model model, TotoBetForm form, @LoggedIn Optional<UserAccount> loggedIn,
								  @PathVariable long eventid, Boolean insufficientBalance, Boolean isTooLate) {
		if (this.lottery.getEventById(eventid) instanceof FootballEvent) {
			FootballEvent footballEvent = (FootballEvent) this.lottery.getEventById(eventid);
			model.addAttribute("matches", footballEvent.getMatches());
		}

		model.addAttribute("form", form);
		model.addAttribute("event", this.lottery.getEventById(eventid));
		model.addAttribute("eventid", eventid);
		model.addAttribute("poolid", Long.valueOf(-1));
		return "toto";
	}

	/**
	 * Opens a {@link TotoBetForm} for a {@link lottery.user.User} to place a {@link TotoBet} for a {@link lottery.pool.Pool}
	 *
	 * @param model
	 * @param form
	 * @param loggedIn
	 * @param eventid
	 * @param poolid
	 * @param insufficientBalance
	 * @param isTooLate
	 * @return
	 */
	@GetMapping("/{eventid}/{poolid}/totobet")
	public String openTotoBetPageFromPool(Model model, TotoBetForm form, @LoggedIn Optional<UserAccount> loggedIn,
										  @PathVariable long eventid, @PathVariable long poolid, Boolean insufficientBalance, Boolean isTooLate) {
		if (this.lottery.getEventById(eventid) instanceof FootballEvent) {
			FootballEvent footballEvent = (FootballEvent) this.lottery.getEventById(eventid);
			model.addAttribute("matches", footballEvent.getMatches());
		}
		int amount = 0;
		Long pid = Long.valueOf(poolid);
		Pool pool = poolManagement.findById(pid).orElse(null);
		if (pool != null) {
			amount = pool.getAmount(eventid);
		}
		if (amount != 0) {
			model.addAttribute("amount", amount);
		}

		model.addAttribute("form", form);
		model.addAttribute("event", this.lottery.getEventById(eventid));
		model.addAttribute("eventid", eventid);
		model.addAttribute("poolid", poolid);
		return "toto";
	}

	/**
	 * Opens a {@link LottoBetForm} for a {@link lottery.user.User} to place a {@link LottoBet} for a {@link lottery.pool.Pool}
	 *
	 * @param model
	 * @param form
	 * @param loggedIn
	 * @param eventid
	 * @param poolid
	 * @param insufficientBalance
	 * @param isTooLate
	 * @return
	 */
	@GetMapping("/{eventid}/{poolid}/lottobet")
	public String openLottoBetPageFromPool(Model model, LottoBetForm form, @LoggedIn Optional<UserAccount> loggedIn,
										   @PathVariable long eventid, @PathVariable long poolid, Boolean insufficientBalance, Boolean isTooLate) {
		if (lottery.getEventById(eventid) instanceof LottoEvent) {
			LottoEvent lottoEvent = (LottoEvent) lottery.getEventById(eventid);
			Integer price = lottoEvent.getPrice();
			model.addAttribute("price", price);
		}

		model.addAttribute("form", form);
		model.addAttribute("eventid", eventid);
		model.addAttribute("poolid", poolid);

		return "lottery-ticket";
	}


	@GetMapping("/allbets")
	@PreAuthorize("hasRole('ADMIN')")
	public String openBetsPage(Model model) {
		model.addAttribute("lottobets", betService.getAllLottoBets());
		model.addAttribute("totobets", betService.getAllTotoBets());
		return "all-bets";
	}

	/**
	 * Opens a {@link LottoBetForm} for a user to place a {@link LottoBet}
	 *
	 * @param model
	 * @param form
	 * @param loggedIn
	 * @param eventid
	 * @param insufficientBalance
	 * @param isTooLate
	 * @return
	 */
	@GetMapping("/{eventid}/lottobet")
	public String openLottoBetPage(Model model, LottoBetForm form, @LoggedIn Optional<UserAccount> loggedIn,
								   @PathVariable long eventid, Boolean insufficientBalance, Boolean isTooLate) {
		if (lottery.getEventById(eventid) instanceof LottoEvent) {
			LottoEvent lottoEvent = (LottoEvent) lottery.getEventById(eventid);
			Integer price = lottoEvent.getPrice();
			model.addAttribute("price", price);
		}

		model.addAttribute("form", form);
		model.addAttribute("eventid", eventid);
		model.addAttribute("poolid", Long.valueOf(-1));
		return "lottery-ticket";
	}

	/**
	 * Opens a {@link LottoBetForm} for an admin to select winning numbers for a certain {@link LottoEvent}
	 *
	 * @param model
	 * @param form
	 * @param eventid
	 * @return
	 */
	@GetMapping("/{eventid}/lottoWinningBet")
	public String openLottoWinningBetPage(Model model, LottoBetForm form, @PathVariable long eventid) {
		model.addAttribute("form", form);
		model.addAttribute("eventid", eventid);
		model.addAttribute("poolid", Long.valueOf(-1));
		return "lotto-winning-ticket";
	}

	@GetMapping("betplaced")
	public String BetPlaced(Model model, @LoggedIn Optional<UserAccount> loggedIn) {
		return "bet-placed";
	}

	/**
	 * @param form
	 * @param errors
	 * @param model
	 * @param match    Football Matches from the given {@link FootballEvent}
	 * @param loggedIn
	 * @param eventid
	 * @param poolid
	 * @return Redirect to toto bet page if errors are found in the form, if the balance is insufficient or if is too late to place bet. Otherwise, redirect to bet placed page.
	 */
	@PostMapping(path = "/{eventid}/{poolid}/totobet")
	String addTotoBet(@Valid TotoBetForm form, Errors errors, Model model, @RequestParam("match") String match,
					  @LoggedIn Optional<UserAccount> loggedIn, @PathVariable long eventid, @PathVariable long poolid,
					  RedirectAttributes redirectAttributes) {

		if (poolid != -1) {
			Long pid = Long.valueOf(poolid);
			Pool pool = poolManagement.findById(pid).orElse(null);
			int amount = pool.getAmount(eventid);
			if ((form.getAmount() % amount) > 0.01) {
				redirectAttributes.addFlashAttribute("warningMessage",
					"The amount of the bet has to be a factor of the pool amount for the event");
				return "redirect:/{eventid}/{poolid}/totobet";
			}
		}

		if (errors.hasErrors() || match.split(",").length != 7 || form.getAmount() < 1) {

			System.out.println(match.split(",").length);
			System.out.println(errors);
			redirectAttributes.addFlashAttribute("warningMessage",
				"Please select exactly 6 matches and enter a valid bet amount.");
			return "redirect:/{eventid}/{poolid}/totobet";
		}

		for (Integer game : form.getGames()){
			if (game < 0 || game > 18){
				redirectAttributes.addFlashAttribute("warningMessage",
					"Please provide valid match numbers");
				return "redirect:/{eventid}/{poolid}/totobet";
			}
		}

		Long userid = userService.getUserByAccountIdentifier(loggedIn.orElseThrow().getId()).orElseThrow().getId();


		if (this.lottery.getEventById(eventid) instanceof FootballEvent) {
			FootballEvent footballEvent = (FootballEvent) this.lottery.getEventById(eventid);

			String eventTime = footballEvent.getFirstMatchDate();
			LocalDateTime eventDate = (LocalDate.parse(eventTime)).atStartOfDay();

			if ((Lottery.getBusinessTime().getTime()).until(eventDate, ChronoUnit.HOURS) < 24L) {
				System.out.println("Too late to place bet");
				redirectAttributes.addFlashAttribute("warningMessage",
					"You cannot place a bet less than 24 hours before the first match of an event");
				return "redirect:/{eventid}/{poolid}/totobet";

			}
		}


		betService.saveBet(form.newTotoBet(userid, eventid, poolid));
		userService.saveUser(userService.getUserById(userid).orElseThrow());

		/*if(poolid != -1) {
			Pool pool = poolManagement.findById(poolid).orElseThrow();
			pool.subBalance(form.getAmount());
			poolManagement.save(pool);
		}*/

		return "redirect:/betplaced";
	}

	/**
	 * @param form
	 * @param errors
	 * @param model
	 * @param number
	 * @param longTermDuration
	 * @param supernumber
	 * @param loggedIn
	 * @param eventid
	 * @param poolid
	 * @return Redirect to Lotto bet page if errors are found in the form, if the balance is insufficient or if is too late to place bet. Otherwise, redirect to bet placed page.
	 */
	@PostMapping(path = "/{eventid}/{poolid}/lottobet")
	String addLottoBet(@Valid LottoBetForm form, Errors errors, Model model, @RequestParam("number") String number,
					   @RequestParam(name = "longTermDuration", required = false) Integer longTermDuration, @RequestParam("supernumber") String supernumber,
					   @LoggedIn Optional<UserAccount> loggedIn, @PathVariable long eventid, @PathVariable long poolid,
					   RedirectAttributes redirectAttributes) {
		Integer price = 0;

		if (lottery.getEventById(eventid) instanceof LottoEvent lottoEvent) {
			price = lottoEvent.getPrice();
		}

		if (errors.hasErrors() || number.split(",").length != 7 || supernumber.split(",").length != 2) {
			System.out.println(errors);
			redirectAttributes.addFlashAttribute("warningMessage",
				"Please select exactly 6 numbers and 1 super number.");
			return "redirect:/{eventid}/{poolid}/lottobet";
		}

		for (Integer lottoNumber : form.getLotteryNumbers()){
			if (lottoNumber<1 || lottoNumber>49 || form.getSupernumber() < 0 || form.getSupernumber() > 9){
				redirectAttributes.addFlashAttribute("warningMessage",
					"Please provide valid lottery numbers and supernumber");
				return "redirect:/{eventid}/{poolid}/lottobet";
			}
		}

		if (!(longTermDuration == null ||
			longTermDuration.equals(0)||
			longTermDuration.equals(4) ||
			longTermDuration.equals(8) ||
			longTermDuration.equals(12)
			)) {

			redirectAttributes.addFlashAttribute("warningMessage",
				"Invalid long term bet duration");
			return "redirect:/{eventid}/{poolid}/lottobet";
		}

		Long userid = userService.getUserByAccountIdentifier(loggedIn.orElseThrow().getId()).orElseThrow().getId();

		if (userService.getUserById(userid).orElseThrow().getAccountState() < price || price == 0) {
			System.out.println(errors);
			redirectAttributes.addFlashAttribute("warningMessage",
				"Insufficient Account Balance.");
			return "redirect:/{eventid}/{poolid}/lottobet";
		}

		String eventTime = lottery.getEventById(eventid).getDrawDate();
		LocalDateTime eventDate = (LocalDate.parse(eventTime)).atStartOfDay();

		if ((Lottery.getBusinessTime().getTime()).until(eventDate, ChronoUnit.HOURS) < 24L) {
			System.out.println("Too late to place bet");
			redirectAttributes.addFlashAttribute("warningMessage",
				"You cannot place a bet less than 24 hours before the draw date of an event");
			return "redirect:/{eventid}/{poolid}/lottobet";
		}

		double toBePaid = price;

		betService.saveBet(form.newLottoBet(userid, eventid, poolid, price));

		if (longTermDuration != null) {
			for (int i = 0; i < longTermDuration; i++) {
				int finalI = i;
				Optional<Event> event = lottery.getAllActiveEvents().stream() //
					.filter(currentEvent -> currentEvent instanceof LottoEvent
						&& currentEvent.getDrawDate().equals(eventDate.plusWeeks(finalI + 1L) //
						.format(DateTimeFormatter.ISO_LOCAL_DATE))).findFirst();
				if (event.isPresent()) {
					LottoEvent lottoEvent = (LottoEvent) event.get();

					if ((userService.getUserById(userid).orElseThrow().getAccountState()
						- lottoEvent.getPrice()) < lottoEvent.getPrice()) {
						LottoBet lottoBet = form.newLottoBet(userid, event.get().getId(), poolid,
							lottoEvent.getPrice(), eventDate.plusWeeks(finalI + 1L));
						lottoBet.setToCanceled();
						betService.saveBet(lottoBet);
					} else {
						userService.getUserById(userid).orElseThrow().substractMoney(Double.valueOf(lottoEvent.getPrice()));
						betService.saveBet(form.newLottoBet(userid, event.get().getId(), poolid,
							lottoEvent.getPrice(), eventDate.plusWeeks(finalI + 1L)));
					}
					continue;
				}
				betService.saveBet(form.newLottoBet(userid, -1L, poolid, 0, eventDate.plusWeeks(finalI + 1L)));
			}
		}

		userService.getUserById(userid).orElseThrow().substractMoney(toBePaid);
		userService.saveUser(userService.getUserById(userid).orElseThrow());

		if(poolid != -1) {
			Pool pool = poolManagement.findById(poolid).orElseThrow();
			pool.subBalance(price.doubleValue());
			poolManagement.save(pool);
		}

		return "redirect:/betplaced";
	}

	@ModelAttribute("currentUserName")
	String currentUserName(@LoggedIn Optional<UserAccount> logedIn) {
		if (logedIn.isPresent()) {
			var user = logedIn.get().getUsername();
			return user;
		}
		return "Anonym";

	}

	@ModelAttribute("currentUserId")
	Long currentUserId(@LoggedIn Optional<UserAccount> logedIn) {
		if (logedIn.isPresent()) {
			return userService.getUserByAccountIdentifier(logedIn.orElseThrow().getId()).orElseThrow().getId();
		}
		return 0L;
	}

	@ModelAttribute("currentUserRole")
	String currentUserRole(@LoggedIn Optional<UserAccount> logedIn) {
		if (logedIn.isPresent()) {
			var user = userService.getUserByAccountIdentifier(
					logedIn.orElseThrow().getId())
				.orElseThrow().getUserAccount().getRoles().toList().get(0);
			return user.toString();
		}
		return "UNREGISTERED";
	}

	/**
	 * @param model
	 * @param betId
	 * @return Shows an admin the details of the selected {@link Bet}: draw date, event type,
	 * pool ID, price, status, selected numbers/matches, winning numbers/matches for the event
	 */
	@GetMapping(path = "/betDetailsAdmin/{betId}")
	@PreAuthorize("hasRole('ADMIN')")
	String betDetailsForAdmin(Model model, @PathVariable long betId) {
		model.addAttribute("bettedEvents", bettedEvents(betService.getBetById(betId).orElseThrow().getUser()));
		if (poolManagement.findById(betService.getBetById(betId).orElseThrow().getPoolId()).isPresent()) {
			model.addAttribute("pool", poolManagement.findById(
				betService.getBetById(betId).orElseThrow().getPoolId()).orElseThrow());
		} else {
			model.addAttribute("pool", "No Pool");
		}
		poolManagement.findById(betService.getBetById(betId).orElseThrow().getPoolId());
		Long eventid = betService.getBetById(betId).orElseThrow().getEvent();

		if (lottery.getEventById(eventid) != null) {
			if (lottery.getEventById(eventid).isActive()) {
				if (lottery.getEventById(eventid).getType().equals("LottoEvent")) {
					model.addAttribute("winningNumbers", "No winning numbers yet");
				} else {
					model.addAttribute("winningNumbers", "No matches played yet");
				}
			} else {
				model.addAttribute("winningNumbers",
					lottery.getEventById(eventid).adminWinningNumbers.replace("[", "").replace("]", "").split(", "));
				if (lottery.getEventById(eventid) instanceof LottoEvent) {
					LottoEvent lottoEvent = (LottoEvent) lottery.getEventById(eventid);
					model.addAttribute("winningSupernumber", lottoEvent.getSupernumber());

				}
			}
		}

		if (betService.getBetById(betId).orElseThrow() instanceof LottoBet lottoBet) {
			model.addAttribute("bet", lottoBet);
			return "bet-details-lotto";
		}

		if (lottery.getEventById(eventid) != null) {
			if (this.lottery.getEventById(eventid) instanceof FootballEvent) {
				FootballEvent footballEvent = (FootballEvent) this.lottery.getEventById(eventid);
				model.addAttribute("selectedMatches", footballEvent.getMatchesByBet(
					(TotoBet) betService.getBetById(betId).orElseThrow()));
				model.addAttribute("matches", footballEvent.getMatches());
			}
		}
		//TotoBet
		model.addAttribute("bet", (betService.getBetById(betId).orElseThrow()));
		return "bet-details-toto";

	}


	/**
	 * @param model
	 * @param loggedIn
	 * @param betId
	 * @return Shows the user the details of the selected {@link Bet}: draw date, event type,
	 * pool ID, price, status, selected numbers/matches, winning numbers/matches for the event
	 */
	@GetMapping(path = "/betDetails/{betId}")
	@PreAuthorize("hasRole('USER')")
	String betDetails(Model model, @LoggedIn Optional<UserAccount> loggedIn, @PathVariable long betId) {
		model.addAttribute("bettedEvents", bettedEvents(userService.getUserByAccountIdentifier(
			loggedIn.orElseThrow().getId()).orElseThrow().getId()));

		int amount = 0;
		Long eventid = betService.getEventIdByBetId(betId).orElse(null);
		Pool pool = poolManagement.findById(betService.getBetById(betId).orElseThrow().getPoolId()).orElse(null);
		if (pool != null) {
			amount = pool.getAmount(eventid);
			model.addAttribute("pool", poolManagement.findById(
				betService.getBetById(betId).orElseThrow().getPoolId()).orElseThrow());
		} else {
			model.addAttribute("pool", "No Pool");
		}
		if (amount != 0) {
			model.addAttribute("amount", amount);
		}

		if (lottery.getEventById(eventid) != null) {
			if (lottery.getEventById(eventid).isActive()) {
				if (lottery.getEventById(eventid).getType().equals("LottoEvent")) {
					model.addAttribute("winningNumbers", "No winning numbers yet");
				} else {
					model.addAttribute("winningNumbers", "No matches played yet");
				}
			} else {
				model.addAttribute("winningNumbers",
					lottery.getEventById(eventid).adminWinningNumbers.replace("[", "").replace("]", "").split(", "));
				if (lottery.getEventById(eventid) instanceof LottoEvent) {
					LottoEvent lottoEvent = (LottoEvent) lottery.getEventById(eventid);
					model.addAttribute("winningSupernumber", lottoEvent.getSupernumber());

				}
			}
		}

		if (betService.getBetById(betId).orElseThrow() instanceof LottoBet lottoBet) {
			model.addAttribute("bet", lottoBet);
			return "bet-details-lotto";
		}

		if (lottery.getEventById(eventid) != null) {
			if (this.lottery.getEventById(eventid) instanceof FootballEvent) {
				FootballEvent footballEvent = (FootballEvent) this.lottery.getEventById(eventid);
				model.addAttribute("selectedMatches", footballEvent.getMatchesByBet(
					(TotoBet) betService.getBetById(betId).orElseThrow()));
				model.addAttribute("matches", footballEvent.getMatches());
			}
		}
		//TotoBet
		model.addAttribute("bet", (betService.getBetById(betId).orElseThrow()));
		return "bet-details-toto";

	}

	Map<Bet, Event> bettedEvents(long userId) {
		Map<Bet, Event> map = new HashMap<>();
		for (Bet bet : betService.getAllBetsByUserId(userId)) {
			map.put(bet, lottery.getEventById(bet.getEvent()));
		}
		return map;
	}

	/**
	 * Allows user to change the amount that was bet for a Toto Event
	 *
	 * @param model
	 * @param loggedIn
	 * @param newAmount
	 * @param betId
	 * @return Redirects to bet details page
	 */
	@PostMapping(path = "/betDetails/{betId}")
	String changeBetAmount(Model model, @LoggedIn Optional<UserAccount> loggedIn,
						   @RequestParam("new-amount") Double newAmount, @PathVariable long betId) {
		Bet bet = betService.getBetById(betId).orElseThrow();

		if (bet.getState() == BetState.OPEN) {
			bet.setPrice(newAmount);
			betService.saveBet(bet);
		}

		return "redirect:/betDetails/" + betId;
	}
}
