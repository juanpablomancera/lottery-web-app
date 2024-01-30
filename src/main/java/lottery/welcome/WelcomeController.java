package lottery.welcome;

import java.util.Optional;

import lottery.lottery.Lottery;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.web.LoggedIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import lottery.user.UserService;


@Controller
public class WelcomeController {

	@Autowired
	private UserService userService;
	@Autowired
	private Lottery lottery;

	WelcomeController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/")
	public String welcome(Model model) {
		model.addAttribute("events", lottery.getAllActiveEvents());
		return "welcome";
	}

	@ModelAttribute("currentUserName")
	String currentUserName(@LoggedIn Optional<UserAccount> logedIn) {
		if (logedIn.isPresent()) {
			return logedIn.get().getUsername();
		}
		return "Anonym";
	}

	@ModelAttribute("currentUserId")
	Long currentUserId(@LoggedIn Optional<UserAccount> logedIn) {
		if (logedIn.isPresent()) {
			return userService.getUserByAccountIdentifier(logedIn.get().getId()).orElseThrow().getId();
		}
		return 0L;
	}
}

