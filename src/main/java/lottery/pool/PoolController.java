package lottery.pool;


import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mysema.commons.lang.Assert;

import jakarta.validation.Valid;
import lottery.lottery.Lottery;
import lottery.event.Event;

import lottery.user.UserService;
import lottery.bet.Bet;
import lottery.bet.BetService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Controller
public class PoolController {

	private final PoolService poolManagement;

	@Autowired
	UserService userService;
	@Autowired
	private BetService betService;

	@Autowired
	Lottery lottery;

	/**
	 * Creates a new {@link PoolController} given a {@link PoolService},
	 * {@link UserService} and {@link Lottery}
	 * 
	 * @param poolManagement must not be {@literal null}
	 * @param userService    must not be {@literal null}
	 * @param lottery        must not be {@literal null}
	 */

	PoolController(PoolService poolManagement, UserService userService, Lottery lottery) {
		Assert.notNull(poolManagement, "PoolManagement must not be null");
		Assert.notNull(userService, "userService must not be null");
		Assert.notNull(lottery, "lottery must not be null");

		this.poolManagement = poolManagement;
		this.userService = userService;
		this.lottery = lottery;

	}

	/**
	 * Displays all pools that a the loggedIn {@link UserAccount} is a member of or is the PoolChef of
	 * @param model will never be {@literal null}.
	 * @param loggedIn {@link UserAccount} that is authenticated
	 * @return template pool-home or redirect to "/"
	 */

	@GetMapping("/poolhome")
	public String home(Model model, @LoggedIn Optional<UserAccount> loggedIn) {
		if (loggedIn.isEmpty()) {
			return "redirect:/";
		}
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
		if (user.getUserAccount().hasRole(Role.of("ADMIN"))) {
			poolManagement.findAll().forEach(pool -> pools.add(pool));
		}
		model.addAttribute("pools", pools);

		return "pool-home";
	}

	/**
	 * Displays a {@link Pool}
	 * @param poolId 
	 * @param model
	 * @return template pool
	 */

	@GetMapping("/pool/{poolId}")
	public String showPool(@PathVariable long poolId, Model model,  @LoggedIn Optional<UserAccount> loggedIn) {
		var pool = this.poolManagement.findById(poolId).get();
		if (loggedIn.isPresent()) {
		var user = userService.getUserByAccountIdentifier(loggedIn.get().getId()).orElseThrow();
		model.addAttribute("isPoolMember", pool.isMember(user));
		}
		model.addAttribute("pool", pool);
		model.addAttribute("activeEvents", lottery.getAllActiveEvents());
		
		return "pool";
	}

	/**
	 * Displays a {@link Pool} for a PoolChef
	 * @param poolId 
	 * @param model
	 * @return template pool-chef-view
	 */

	@GetMapping("/pool/{poolId}/chef")
	public String showPoolChef(@PathVariable long poolId, Model model) {
		var pool = this.poolManagement.findById(poolId).get();
		model.addAttribute("pool", pool);
		model.addAttribute("activeEvents", lottery.getAllActiveEvents());
		return "pool-chef-view";
	}

	/**
	 * Displays a {@link Pool} for a Admin
	 * @param poolId 
	 * @param model
	 * @return template pool-admin-view
	 */

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/pool/{poolId}/admin")
	public String showPoolAdmin(@PathVariable long poolId, Model model) {
		var pool = this.poolManagement.findById(poolId).get();
		model.addAttribute("pool", pool);
		model.addAttribute("activeEvents", lottery.getAllActiveEvents());
		return "pool-admin-view";
	}

	/**
	 * Removes a {@link Pool} from the DataBase
	 * @param id 
	 * @param model
	 * @return redirect to "/poolhome"
	 */

	@GetMapping("/pool/{poolId}/remove")
	public String removePool(@PathVariable long poolId, Model model) {
		List<Bet> bets = betService.getAllBets();
		for (Bet bet : bets) {
			if (bet.getPoolId() == poolId) {
				bet.setToCanceled();
			}
		}
		poolManagement.deleteById(poolId);
		return "redirect:/poolhome";
	}

	@GetMapping("pool/{id}/addevent")
	public String showEvents(@PathVariable long id, Model model, @LoggedIn Optional<UserAccount> loggedIn) {
		Pool pool = poolManagement.findById(id).get();
		model.addAttribute("pool", pool);
		if (loggedIn.isPresent()) {
			var userAccount = loggedIn.get();
			if (userAccount.getId() == pool.getPoolChef().getUserAccount().getId()) {
				List<Event> events = new ArrayList<>();
				events = lottery.getAllActiveEvents();
				events.removeAll(pool.getEvents());
				model.addAttribute("events", events);
				return "event";
			}
		}
		return "redirect:/pool/" + id;
	}

	@GetMapping("pool/{id}/remove/{member}")
	public String removeMember(@PathVariable long id, @PathVariable long member, Model model,
			@LoggedIn Optional<UserAccount> loggedIn) {
		Pool pool = poolManagement.findById(id).get();
		for (var bet : betService.getAllBetsByUserId(member)) {
			if (bet.getPoolId() == id) {
				bet.setToCanceled();
			}
		}
		pool.removePoolMember(userService.getUserById(member).orElseThrow());
		poolManagement.save(pool);
		if (loggedIn.get().hasRole(Role.of("ADMIN"))) {
			return "redirect:/pool/" + id + "/admin";
		}
		if (loggedIn.get() == userService.getUserById(member).orElse(null).getUserAccount() &&
			loggedIn.get() != pool.getPoolChef().getUserAccount()) {
			return "redirect:/account";
		}
		return "redirect:/pool/" + id + "/chef";
	}

	@GetMapping("pool/{id}/event/{eid}")
	public String addEvent(Model model, TotoForm form, Errors errors) {
		model.addAttribute("TotoForm", form);
		return "pool/{id}/event/{eid}";
	}

	@PostMapping("pool/{id}/event/{eid}")
	public String addEvent(@Valid TotoForm form, @PathVariable long id, @PathVariable Long eid, Model model) {
		Pool pool = this.poolManagement.findById(id).get();
		pool.addEvent(lottery.getEventById(eid), Integer.valueOf(form.getAmount()));
		poolManagement.save(pool);
		
		return "redirect:/pool/"+ id +"/chef";
	}

	@GetMapping("pool/{id}/event/{eid}/lotto")
	public String addLottoEvent(@PathVariable long id, @PathVariable Long eid, Model model) {
		Pool pool = this.poolManagement.findById(id).get();
		pool.addEvent(lottery.getEventById(eid), 1);
		poolManagement.save(pool);
		
		return "redirect:/pool/"+ id +"/chef";
	}


	@PostMapping(path = "/pool/{poolid}/FootballEvent/{eventid}")
	String openTotoBetfrompool(@PathVariable Long poolid, @PathVariable Long eventid,
			@LoggedIn Optional<UserAccount> loggedIn, Model model) {
		return "redirect:/{eventid}/{poolid}/totobet";
	}

	@PostMapping(path = "/pool/{poolid}/LottoEvent/{eventid}")
	String openLottoBetfrompool(@PathVariable Long poolid, @PathVariable Long eventid,
			@LoggedIn Optional<UserAccount> loggedIn, Model model) {
		return "redirect:/{eventid}/{poolid}/lottobet";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/create")
	public String create(Model model, PoolForm form, Errors errors) {
		model.addAttribute("PoolForm", form);
		return "create";
	}

	@PostMapping("/createnew")
	public String createPool(@Valid PoolForm form, RedirectAttributes redirectAttributes, Errors errors) {
		if (errors.hasErrors() || poolManagement.poolNameIsUsed(form.getName())) {
			redirectAttributes.addFlashAttribute("message", "Pool already exists");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/create";
		}
		if (userService.getUserByEmail(form.getPoolChef()).isEmpty()) {
			redirectAttributes.addFlashAttribute("message", "Pool chef is not registerd");
			redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
			return "redirect:/create";
		}

		this.poolManagement.createPoolFormForm(form);
		return "redirect:/poolhome";
	}
	

	@GetMapping("/join")
	public String join(Model model, JoinForm form, Errors errors, @LoggedIn Optional<UserAccount> loggedIn) {
		if (loggedIn.isEmpty()) {
			return "redirect:/";
		}
		return "join";
	}

	@PostMapping("/joinpool")
	public String joinPool(@Valid JoinForm form, Model model, RedirectAttributes redirectAttributes, Errors errors,
			@LoggedIn Optional<UserAccount> loggedIn) {
		if (errors.hasErrors()) {
			return "details";
		}
		for (var pool : poolManagement.findAll()) {
			if (pool.getName().contentEquals(form.getName()) && pool.isValidPassword(form.getPassword())) {
				var user = userService.getUserByAccountIdentifier(loggedIn.get().getId()).orElseThrow();
				if (pool.isMember(user)) {
					redirectAttributes.addFlashAttribute("message", "You are already part of this pool!");
					redirectAttributes.addFlashAttribute("alertClass", "alert-danger");							return "redirect:/join";
				}				
				pool.addPoolMember(user);
				poolManagement.save(pool);
				return "redirect:/pool/" + pool.getId();

			}
		}

		redirectAttributes.addFlashAttribute("message", "Pool Name or Password wrong");
		redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
		return "redirect:/join";
	}

	@ModelAttribute("currentUserName")
	String currentUserName(@LoggedIn Optional<UserAccount> loggedIn) {
		if (loggedIn.isPresent()) {
			var user = loggedIn.get().getUsername();
			return user;
		}
		return "Anonym";
	}

	@ModelAttribute("currentUserId")
	Long currentUserId(@LoggedIn Optional<UserAccount> loggedIn) {
		if (loggedIn.isPresent()) {
			return userService.getUserByAccountIdentifier(loggedIn.get().getId()).orElseThrow().getId();
		}
		return 0L;
	}

	@ModelAttribute("currentUserRole")
	String currentUserRole(@LoggedIn Optional<UserAccount> logedIn) {
		if (logedIn.isPresent()) {
			var user = userService.getUserByAccountIdentifier(
					logedIn.get().getId()).orElseThrow().getUserAccount().getRoles().toList().get(0);
			return user.toString();
		}
		return "UNREGISTERED";
	}
}