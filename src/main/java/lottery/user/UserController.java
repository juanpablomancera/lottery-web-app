package lottery.user;

import jakarta.validation.Valid;

import java.text.DecimalFormat;
import java.util.*;

import lottery.bet.Bet;
import lottery.bet.BetService;
import lottery.bet.BetState;
import lottery.event.Event;
import lottery.lottery.Lottery;
import lottery.pool.Pool;
import lottery.pool.PoolService;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Webcontroller class for user related mappings
 *  @author Konrad Löhr, Juan Pablo Mancera
 */


@Controller
public class UserController {

	private final UserService userService;

	private final BetService betService;

	@Autowired
	Lottery lottery;

	@Autowired
	PoolService poolManagement;

	UserController(UserService userService, Lottery lottery, BetService betService) {
		Assert.notNull(userService, "User service can not be null");
		this.betService = betService;
		this.lottery = lottery;
		this.userService = userService;
	}

	/**
	 *
	 * @param form
	 * @param result
	 * @param redirectAttributes
	 * @return Redirect to login page if registration was successful, otherwise redirect to registration page
	 */
	@PostMapping("/register")
	String registerUser(@Valid RegistrationForm form, Errors result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors() || userService.isUserRegistered(form.getEmail(), form.getName())) {
			redirectAttributes.addFlashAttribute("message", "User already exists");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/register";
		}
		userService.registerUser(form, Role.of("USER"));
		return "redirect:/login";

	}

	/**
	 *
	 * @param registrationForm
	 * @param result
	 * @param redirectAttributes
	 * @return Redirect to admin panel if registration was successful, otherwise redirect to admin panel and
	 * display error message
	 */
	@PostMapping("/createAdmin")
	@PreAuthorize("hasRole('ADMIN')")
	String registerAdmin(@Valid RegistrationForm registrationForm, Errors result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors() || userService.isUserRegistered(registrationForm.getEmail(), registrationForm.getName())) {
			redirectAttributes.addFlashAttribute("message", "Admin already exists");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/adminPanel";
		}
		userService.registerUser(registrationForm, Role.of("ADMIN"));
		return "redirect:/adminPanel";
	}

	@GetMapping("/register")
	public String register(Model model, RegistrationForm form) {
		return "register";
	}

	@GetMapping("/login")
	public String login(Model model) {
		return "login";
	}

	@GetMapping("/adminPanel")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminPanel(Model model, RegistrationForm form) {
		model.addAttribute("activeEvents", lottery.getAllActiveEvents());
		model.addAttribute("inactiveEvents", lottery.getAllInactiveEvents());
		model.addAttribute("income", lottery.getIncome());
		model.addAttribute("expenses", lottery.getExpenses());
		model.addAttribute("time", lottery.getTime());
		model.addAttribute("userList", userService.getAllUsers());
		model.addAttribute("lottobets", betService.getAllLottoBets());
		model.addAttribute("totobets", betService.getAllTotoBets());
		model.addAttribute("numberOfUsers", userService.getAllUsers().size());
		return "admin-panel";
	}

	/**
	 *
	 * @param model
	 * @param userId
	 * @return shows the user: all the bets he has done, all the pools where the user is member of,
	 * the account balance and the user
	 */
	@GetMapping("/userDetails/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public String userDetails(Model model, @PathVariable long userId){
		model.addAttribute("bets", betService.getAllBetsByUserId(userId));
		model.addAttribute("pools", getPoolPerUser(userId));
		model.addAttribute("accountBalance", userService.getUserById(userId).orElseThrow().getAccountState());
		model.addAttribute("user", userService.getUserById(userId).orElseThrow());

		return "user-detail";
	}

	/**
	 *
	 * @param userId
	 * @return all the pools where the user is member of
	 */
	private List<Pool>  getPoolPerUser(long userId){
			List<Pool> list = new ArrayList<>();
			for(Pool pool : poolManagement.findAll()){
				for (User member : pool.getPoolMembers()){
					if(userId == member.getId()){
						list.add(pool);
					}
				}
				if (userId == pool.getPoolChef().getId()) {
					list.add(pool);
				}
			}
		return list;
		}

	@PostMapping("/deleteUser/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public String deleteUser(@LoggedIn Optional<UserAccount> userAccount, @PathVariable long id, Model model, RedirectAttributes redirectAttributes) {
		if (poolManagement.isUserInPool(userService.getUserById(id).orElseThrow())) {
			redirectAttributes.addFlashAttribute("message", "User is in a pool. Please remove him from the pool first");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/adminPanel";
		}else if (this.currentUserId(userAccount) == id) {
			redirectAttributes.addFlashAttribute("message", "Not possible to delete yourself");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/adminPanel";
		}
		else if (userService.isLastAdmin(id)) {
			redirectAttributes.addFlashAttribute("message", "Not possible to delete last admin");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/adminPanel";
		}
		userService.deleteUserByID(id);
		return "redirect:/adminPanel";
	}

	@PostMapping("/depositMoney")
	@PreAuthorize("hasRole('USER')")
	public String depositMoney(@LoggedIn Optional<UserAccount> userAccount, @RequestParam("deposit") Double amount) {
		User user = userService.getUserById(this.currentUserId(userAccount)).orElse(null);
		if(user != null) {
			user.addMoney(amount);
			userService.saveUser(user);
		}
		return "redirect:/account";
	}

	@PostMapping("/withdrawMoney")
	@PreAuthorize("hasRole('USER')")
	public String withdrawMoney(@LoggedIn Optional<UserAccount> userAccount, @RequestParam("withdraw") Double amount,
	RedirectAttributes redirectAttributes) {
		User user = userService.getUserById(this.currentUserId(userAccount)).orElse(null);
		if(user != null) {
			if(user.getAccountState() < amount){
				redirectAttributes.addFlashAttribute("message", "Not enough money in account");
				redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
				return "redirect:/account";
			}
			user.substractMoney(amount);
			userService.saveUser(user);
		}
		return "redirect:/account";
	}

	@PostMapping("/withdrawMoneyFromUser/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public String withdrawMoneyFromUser(@PathVariable long id, @RequestParam("withdraw") Double amount,
								RedirectAttributes redirectAttributes) {
		User user = userService.getUserById(id).orElse(null);
		if(user != null) {
			if(user.getUserAccount().hasRole(Role.of("ADMIN"))){
				redirectAttributes.addFlashAttribute("message", "Not possible to withdraw money from admin");
				redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
				return "redirect:/userDetails/" + id;
			}
			if(user.getAccountState() < amount){
				redirectAttributes.addFlashAttribute("message", "Not enough money in account");
				redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
				return "redirect:/userDetails/" + id;
			}
			user.substractMoney(amount);
			userService.saveUser(user);
		}
		return "redirect:/userDetails/" + id;
	}

	@PostMapping("/depositMoneyToUser/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public String depositMoneyToUser(@PathVariable long id, @RequestParam("deposit") Double amount,
								RedirectAttributes redirectAttributes) {
		User user = userService.getUserById(id).orElse(null);
		if(user != null) {
			if(user.getUserAccount().hasRole(Role.of("ADMIN"))){
				redirectAttributes.addFlashAttribute("message", "Not possible to deposit money to admin");
				redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
				return "redirect:/userDetails/" + id;
			}
			user.addMoney(amount);
			userService.saveUser(user);
		}
		return "redirect:/userDetails/" + id;
	}

	@GetMapping("/account")
	String accountOfCurrentUser(Model model, @LoggedIn Optional<UserAccount> loggedIn) {
		if (loggedIn.isPresent()) {
			double accountBalance = userService.getUserByAccountIdentifier(
				loggedIn.get().getId()).orElseThrow().getAccountState();
			DecimalFormat df = new DecimalFormat("0.00");
			accountBalance = Double.parseDouble(df.format(accountBalance).replaceAll(",", "."));

			var user = userService.getUserByAccountIdentifier(loggedIn.get().getId()).orElseThrow();
			List<Pool> pools = new ArrayList<>();
			for (var pool : poolManagement.findAll()) {
				for (var member : pool.getPoolMembers()) {
					if (user == member) {
						pools.add(pool);
					}
				}
				if (pool.getPoolChef() == user) {
					pools.add(pool);
				}
			}

			model.addAttribute("accountBalance", accountBalance);
			model.addAttribute("currentUserName", loggedIn.get().getUsername());
			model.addAttribute("pools", pools);
			model.addAttribute("activeBets", betService.getAllBetsByUserId(
				userService.getUserByAccountIdentifier(loggedIn.get().getId()).orElseThrow().getId()).stream().filter(bet -> bet.getState() == BetState.OPEN));
			model.addAttribute("archivedBets", betService.getAllBetsByUserId(
				userService.getUserByAccountIdentifier(loggedIn.get().getId()).orElseThrow().getId()).stream().filter(bet -> bet.getState() != BetState.OPEN));
			model.addAttribute("bettedPools", bettedPools(loggedIn));
			model.addAttribute("bettedEvents",bettedEvents(
				userService.getUserByAccountIdentifier(loggedIn.orElseThrow().getId()).orElseThrow().getId()));
			model.addAttribute("notices",
				userService.getUserByAccountIdentifier(loggedIn.orElseThrow().getId()).orElseThrow().getGivenNotices());
		}
		return "account";
	}

	private Map<Bet, Event> bettedEvents(long userId){
		Map<Bet, Event> map = new HashMap<>();
		for(Bet bet : betService.getAllBetsByUserId(userId)){
			map.put(bet, lottery.getEventById(bet.getEvent()));
		}
		return map;
	}

	/**
	 *
	 * @param loggedIn
	 * @return List of Pools that the logged in User has betted in.
	 */
	private List<Pool> bettedPools(@LoggedIn Optional<UserAccount> loggedIn){
		List<Pool> currentPools = new ArrayList<>();
		for (Pool pool : poolManagement.findAll()) {
			for(Bet bet : betService.getAllBetsByUserId(userService.getUserByAccountIdentifier(
				loggedIn.orElseThrow().getId()).orElseThrow().getId())){
				if(bet.getPoolId() == pool.getId()){
					currentPools.add(pool);
				}
			}
		}
		return currentPools;
	}

	@ModelAttribute("currentUserName")
	String currentUserName(@LoggedIn Optional<UserAccount> logedIn) {
		if (logedIn.isPresent()) {
			return logedIn.get().getUsername();
		}
		return "Anonym";


	}

	@ModelAttribute("currentUserId")
	Long currentUserId(@LoggedIn Optional<UserAccount> loggedIn) {
		if (loggedIn.isPresent()) {
			return userService.getUserByAccountIdentifier(
				loggedIn.get().getId()).orElseThrow().getId();
		}
		return 0L;
	}

	@ModelAttribute("currentUserRole")
	String currentUserRole(@LoggedIn Optional<UserAccount> loggedIn) {
		if (loggedIn.isPresent()) {
			var user = userService.getUserByAccountIdentifier(
				loggedIn.get().getId()).orElseThrow().getUserAccount().getRoles().toList().get(0);
			return user.toString();
		}
		return "UNREGISTERED";
	}
}
