package lottery.bet;

import lottery.event.Event;
import lottery.event.FootballEvent;
import lottery.event.LottoEvent;
import lottery.lottery.Lottery;
import lottery.user.User;
import lottery.user.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;
import org.springframework.ui.Model;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Optional;


@SpringBootTest
@AutoConfigureMockMvc
public class BetControllerWebIntegrationTests {
	@Autowired
	MockMvc mvc;
	@Autowired
	BetController betController;
	@Autowired
	UserAccountManagement userAccountManagement;
	@Autowired
	UserService userService;
	@Autowired
	BetService betService;

	@Test
	@WithMockUser(roles = "USER")
	void allowsUnauthenticatedAccessToSeeAllBets() throws Exception {
		mvc.perform(get("/allbets")) //
			.andExpect(status().isForbidden());//
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void allowsAuthenticatedAccessToSeeAllBets() throws Exception {
		mvc.perform(get("/allbets")) //
			.andExpect(status().isOk());//
	}

	@Test
	@WithMockUser(roles = "USER")
	void allowsAuthenticatedAccessToSeeTotobetFormFromPoolView() throws Exception {
		mvc.perform(get("/1/1/totobet")) //
			.andExpect(status().isOk()) //
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("eventid"))
			.andExpect(model().attributeExists("poolid"));
	}

	@Test
	@WithMockUser(roles = "USER")
	void allowsAuthenticatedAccessToSeeLottobetFormFromPoolView() throws Exception {
		mvc.perform(get("/1/1/lottobet")) //
			.andExpect(status().isOk()) //
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("eventid"))
			.andExpect(model().attributeExists("poolid"));
	}

	@Test
	@WithMockUser(roles = "USER")
	void allowsAuthenticatedAccessToSeeTotobetFormFromEventView() throws Exception {
		mvc.perform(get("/1/totobet")) //
			.andExpect(status().isOk()) //
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("eventid"))
			.andExpect(model().attributeExists("poolid"));

	}

	@Test
	@WithMockUser(roles = "USER")
	void allowsAuthenticatedAccessToSeeLottobetFormFromEventView() throws Exception {
		mvc.perform(get("/1/lottobet")) //
			.andExpect(status().isOk()) //
			.andExpect(model().attributeExists("form"))
			.andExpect(model().attributeExists("eventid"))
			.andExpect(model().attributeExists("poolid"));
	}

	@Test
	@WithMockUser(roles = "USER")
	void placeTotoBet(){
		var totoForm = new TotoBetForm(1.0, "-1,10,1,3,11,2,4");
		Errors errors = mock(Errors.class);
		Model model = mock(Model.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		String match = "-1,10,1,3,11,2,4";
		User user = new User(userAccountManagement.create(
			"loggedIn123", Password.UnencryptedPassword.of("test"), "log@log123", Role.of("USER")));
		user.addMoney(40.0);
		userService.saveUser(user);
		assertThat(betController.addTotoBet(totoForm, errors, model, match, Optional.ofNullable(user.getUserAccount()), 1L, 1L, redirectAttributes))
			.isEqualTo("redirect:/betplaced");
	}

	@Test
	@WithMockUser(roles = "USER")
	void placeTotoBetWithInsufficientBalance(){
		var totoForm = new TotoBetForm(100.0, "-1,10,1,3,11,2,4");
		Errors errors = mock(Errors.class);
		Model model = mock(Model.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		String match = "-1,10,1,3,11,2,4";
		User user = new User(userAccountManagement.create(
			"loggedIn1234", Password.UnencryptedPassword.of("test"), "log@log1234", Role.of("USER")));
		user.addMoney(40.0);
		userService.saveUser(user);
		assertThat(betController.addTotoBet(totoForm, errors, model, match,Optional.ofNullable(user.getUserAccount()), 1L, 1L, redirectAttributes))
			.isEqualTo("redirect:/betplaced");
	}

	@Test
	@WithMockUser(roles = "USER")
	void placeTotoBetWithInvalidMatches(){
		var totoForm = new TotoBetForm(100.0, "-1,10,1,3,11,2,4,5");
		Errors errors = mock(Errors.class);
		Model model = mock(Model.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		String match = "-1,10,1,3,11,2,4,5";
		User user = new User(userAccountManagement.create(
			"loggedIn12345", Password.UnencryptedPassword.of("test"), "log@log12345", Role.of("USER")));
		user.addMoney(40.0);
		userService.saveUser(user);
		assertThat(betController.addTotoBet(totoForm, errors, model, match,Optional.ofNullable(user.getUserAccount()), 1L, 1L, redirectAttributes))
			.isEqualTo("redirect:/{eventid}/{poolid}/totobet");
	}

	@Test
	@WithMockUser(roles = "USER")
	void placeLottoBet(){
		var lottoForm = new LottoBetForm("-1,10,1,3,11,20,4", "-1,2");
		Errors errors = mock(Errors.class);
		Model model = mock(Model.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		String number = "-1,10,1,3,11,20,4";
		String superNumber = "-1,2";
		User user = new User(userAccountManagement.create(
			"loggedIn321", Password.UnencryptedPassword.of("test"), "log@log321", Role.of("USER")));
		user.addMoney(40.0);
		userService.saveUser(user);
		assertThat(betController.addLottoBet(lottoForm, errors, model, number,null, superNumber, Optional.ofNullable(user.getUserAccount()), 1, 1, redirectAttributes))
			.isEqualTo("redirect:/betplaced");
	}

	@Test
	@WithMockUser(roles = "USER")
	void placeLottoBetWithLongTermTicket(){
		var lottoForm = new LottoBetForm("-1,10,1,3,11,20,4", "-1,2");
		Errors errors = mock(Errors.class);
		Model model = mock(Model.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		String number = "-1,10,1,3,11,20,4";
		String superNumber = "-1,2";
		User user = new User(userAccountManagement.create(
				"loggedIn321786", Password.UnencryptedPassword.of("test"), "log@log321457", Role.of("USER")));
		user.addMoney(40.0);
		userService.saveUser(user);

		int quantityOfBets = betService.getAllLottoBets().size();
		betController.addLottoBet(lottoForm, errors, model, number,4, superNumber, Optional.ofNullable(user.getUserAccount()), 1, 1, redirectAttributes);
		assertThat(betService.getAllLottoBets().size()).isEqualTo(quantityOfBets + 5);
	}

	@Test
	@WithMockUser(roles = "USER")
	void placeLottoBetWithInvalidNumbers(){
		var lottoForm = new LottoBetForm("-1,10,1,3,11,20,4,5", "-1,2");
		Errors errors = mock(Errors.class);
		Model model = mock(Model.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		String number = "-1,10,1,3,11,20,4,5";
		String superNumber = "-1,2";
		User user = new User(userAccountManagement.create(
			"loggedIn4321", Password.UnencryptedPassword.of("test"), "log@log4321", Role.of("USER")));
		user.addMoney(40.0);
		userService.saveUser(user);
		assertThat(betController.addLottoBet(lottoForm, errors, model, number,0, superNumber, Optional.ofNullable(user.getUserAccount()), 1, 1, redirectAttributes))
			.isEqualTo("redirect:/{eventid}/{poolid}/lottobet");
	}

	@Test
	@WithMockUser(roles = "USER")
	void placeLottoBetWithInvalidSuperNumber(){
		var lottoForm = new LottoBetForm("-1,10,1,3,11,20,4", "-1,2");
		Errors errors = mock(Errors.class);
		Model model = mock(Model.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		String number = "-1,10,1,3,11,20,4";
		String superNumber = "-1,2,3";
		User user = new User(userAccountManagement.create(
			"loggedIn54321", Password.UnencryptedPassword.of("test"), "log@log54321", Role.of("USER")));
		user.addMoney(40.0);
		userService.saveUser(user);
		assertThat(betController.addLottoBet(lottoForm, errors, model, number,0, superNumber, Optional.ofNullable(user.getUserAccount()), 1, 1, redirectAttributes))
			.isEqualTo("redirect:/{eventid}/{poolid}/lottobet");
	}

	@Test
	@WithMockUser(roles = "USER")
	void placeLottoBetWithInvalidBalance(){
		var lottoForm = new LottoBetForm("-1,10,1,3,11,20,4", "-1,2");
		Errors errors = mock(Errors.class);
		Model model = mock(Model.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		String number = "-1,10,1,3,11,20,4";
		String superNumber = "-1,2";
		User user = new User(userAccountManagement.create(
			"loggedIn654321", Password.UnencryptedPassword.of("test"), "log@log654321", Role.of("USER")));
		user.addMoney(1.0);
		userService.saveUser(user);
		assertThat(betController.addLottoBet(lottoForm, errors, model, number,0, superNumber, Optional.ofNullable(user.getUserAccount()), 1, 1, redirectAttributes))
			.isEqualTo("redirect:/{eventid}/{poolid}/lottobet");
	}

	@Test
	@WithMockUser(roles = "USER")
	void placeLottoBetWithInvalidForm(){
		var lottoForm = new LottoBetForm("-1,10,1,3,11,20,50", "-1,2");
		Errors errors = mock(Errors.class);
		Model model = mock(Model.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		String number = "-1,10,1,3,11,20,50";
		String superNumber = "-1,2";
		User user = new User(userAccountManagement.create(
			"loggedIn6543211", Password.UnencryptedPassword.of("test"), "log@log6543211", Role.of("USER")));
		user.addMoney(50.0);
		userService.saveUser(user);
		assertThat(betController.addLottoBet(lottoForm, errors, model, number,0, superNumber, Optional.ofNullable(user.getUserAccount()), 1, 1, redirectAttributes))
			.isEqualTo("redirect:/{eventid}/{poolid}/lottobet");
	}

	@Test
	@WithMockUser(roles = "USER")
	void placeTotoBetWithInvalidForm(){
		var totoForm = new TotoBetForm(100.0, "-1,10,1,3,11,20,4");
		Errors errors = mock(Errors.class);
		Model model = mock(Model.class);
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		String match = "-1,10,1,3,11,20,4";
		User user = new User(userAccountManagement.create(
			"loggedIn123455", Password.UnencryptedPassword.of("test"), "log@log123455", Role.of("USER")));
		user.addMoney(40.0);
		userService.saveUser(user);
		assertThat(betController.addTotoBet(totoForm, errors, model, match,Optional.ofNullable(user.getUserAccount()), 1L, 1L, redirectAttributes))
			.isEqualTo("redirect:/{eventid}/{poolid}/totobet");
	}

	@Test
	@WithMockUser(roles = "USER")
	void testBetPlaced() throws Exception {
		mvc.perform(get("/betplaced")) //
			.andExpect(status().isOk());//
	}

	@Test
	@WithMockUser(roles = "USER")
	void testModelAttributesMethods(){
		User user = new User(userAccountManagement.create(
			"loggedIn234", Password.UnencryptedPassword.of("test"), "log@log432", Role.of("USER")));
		userService.saveUser(user);
		assertThat(betController.currentUserName(Optional.ofNullable(user.getUserAccount()))).isEqualTo(user.getUserAccount().getUsername());
		assertThat(betController.currentUserId(Optional.ofNullable(user.getUserAccount()))).isEqualTo(user.getId());
		assertThat(betController.currentUserRole(Optional.ofNullable(user.getUserAccount()))).isEqualTo("USER");
	}

	@Test
	@Disabled
	@WithMockUser(roles = "USER")
	void betDetailsWithInvalidPoolID(){
		User user = new User(userAccountManagement.create(
			"loggedIn2345", Password.UnencryptedPassword.of("test"), "log@log5432", Role.of("USER")));
		userService.saveUser(user);
		Model model = mock(Model.class);
		Integer[] games = {1, 2, 3, 4, 5, 6};
		TotoBet toto = new TotoBet(games, 10.0, BetState.OPEN, 1L, 0L,-1L);
		betService.saveBet(toto);
		Integer[] numbers = {11, 22, 13, 14, 5, 6};
		LottoBet lotto = new LottoBet(numbers, 19, 15.0, BetState.OPEN, 1L, 0L, -1L);
		betService.saveBet(lotto);

		Lottery lottery = mock(Lottery.class);
		Event event = mock(Event.class);
		when(lottery.getEventById(-1L)).thenReturn(event);

		when(event.getAdminWinningNumbersString()).thenReturn("[2, 3]");

		assertThat(betController.betDetails(model, Optional.ofNullable(user.getUserAccount()), toto.getId())).isEqualTo("bet-details-toto");
		assertThat(betController.betDetails(model, Optional.ofNullable(user.getUserAccount()), lotto.getId())).isEqualTo("bet-details-lotto");
	}


}
