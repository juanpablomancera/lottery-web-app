package lottery.lottery;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


import lottery.bet.BetService;
import lottery.event.LottoEventForm;
import lottery.event.TotoEventForm;
import lottery.pool.PoolService;
import lottery.user.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.salespointframework.time.BusinessTime;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
class LotteryControllerIntegrationTests {

	@Autowired MockMvc mvc;

	@Autowired Lottery lottery;

	@Autowired LotteryController controller;

	@Test
	void showsEvent() throws Exception {
		mvc.perform(get("/lottery")) //
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Event")));
	}

	@Test
	@Disabled
	@WithMockUser(roles = "ADMIN")
	void testDrawEvent(){
		ExtendedModelMap model = new ExtendedModelMap();
		assertThat(controller.drawLotteryManually(1L, model)).isEqualTo("redirect:/adminPanel");
	}

	@Test
	@WithMockUser(roles = "USER")
	void testUnauthorizedDrawEvent(){
		assertThatExceptionOfType(AccessDeniedException.class) //
			.isThrownBy(() -> controller.drawLotteryManually(1L, new ExtendedModelMap()));
	}

	@Test
	@WithMockUser(roles = "USER")
	void testUnauthorizedSkipDay(){
		assertThatExceptionOfType(AccessDeniedException.class) //
			.isThrownBy(() -> controller.skipDay(new ExtendedModelMap()));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanAddTotoEvent(){
		ExtendedModelMap model = new ExtendedModelMap();
		TotoEventForm totoEventForm = mock(TotoEventForm.class);
		when(totoEventForm.getStartDate()).thenReturn(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		when(totoEventForm.getDrawDate()).thenReturn(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
		when(totoEventForm.isDateValid()).thenReturn(true);
		assertThat(controller.addTotoEvent(totoEventForm, mock(Errors.class), model)).isEqualTo("redirect:/adminPanel");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void cantAddTotoWithErrors(){
		ExtendedModelMap model = new ExtendedModelMap();
		TotoEventForm totoEventForm = mock(TotoEventForm.class);
		when(totoEventForm.getDrawDate()).thenReturn(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
		when(totoEventForm.getStartDate()).thenReturn(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		Errors errors = mock(Errors.class);
		when(errors.hasErrors()).thenReturn(true);

		assertThat(controller.addTotoEvent(totoEventForm, errors, model)).isEqualTo("add-toto-event");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminCanAddLottoEvent(){
		ExtendedModelMap model = new ExtendedModelMap();
		LottoEventForm lottoEventForm = mock(LottoEventForm.class);
		when(lottoEventForm.getDrawDate()).thenReturn(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
		when(lottoEventForm.isDateValid()).thenReturn(true);
		Errors errors = mock(Errors.class);
		when(errors.hasErrors()).thenReturn(false);

		assertThat(controller.addLottoEvent(lottoEventForm, 5, errors, model)).isEqualTo("redirect:/adminPanel");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void cantAddLottoWithErrors(){
		ExtendedModelMap model = new ExtendedModelMap();
		LottoEventForm lottoEventForm = mock(LottoEventForm.class);
		when(lottoEventForm.getDrawDate()).thenReturn(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
		Errors errors = mock(Errors.class);
		when(errors.hasErrors()).thenReturn(true);

		assertThat(controller.addLottoEvent(lottoEventForm, 2, errors, model)).isEqualTo("add-lotto-event");
	}

	@Test
	@WithMockUser(roles = "USER")
	void userCannotAddTotoEvent(){
		assertThatExceptionOfType(AccessDeniedException.class) //
			.isThrownBy(() -> controller.addTotoEvent(mock(TotoEventForm.class), mock(Errors.class), new ExtendedModelMap()));
	}

	@Test
	void testOpenLottoBet(){
		ExtendedModelMap model = new ExtendedModelMap();

		UserAccount userAccount = mock(UserAccount.class);
		when(userAccount.getUsername()).thenReturn("user");

		assertThat(controller.openLottoBet(0L , Optional.of(userAccount), model)).isEqualTo("redirect:/{id}/lottobet");
	}

	@Test
	void testOpenTotoBet(){
		ExtendedModelMap model = new ExtendedModelMap();

		UserAccount userAccount = mock(UserAccount.class);
		when(userAccount.getUsername()).thenReturn("user");

		assertThat(controller.openTotoBet(0L , Optional.of(userAccount), model)).isEqualTo("redirect:/{id}/totobet");
	}

	@Test
	void cantInitializeLotteryWithNullLottery(){
		assertThatExceptionOfType(NullPointerException.class) //
			.isThrownBy(() -> new LotteryController(null, mock(BusinessTime.class), mock(UserService.class), mock(PoolService.class), mock(BetService.class)));
	}

	@Test
	void cantInitializeLotteryWithNullBusinessTime(){
		assertThatExceptionOfType(NullPointerException.class) //
			.isThrownBy(() -> new LotteryController(lottery, null, mock(UserService.class), mock(PoolService.class), mock(BetService.class)));
	}

	@Test
	void cantInitializeLotteryWithNullUserService(){
		assertThatExceptionOfType(NullPointerException.class) //
			.isThrownBy(() -> new LotteryController(lottery, mock(BusinessTime.class), null, mock(PoolService.class), mock(BetService.class)));
	}

	@Test
	void cantInitializeLotteryWithNullPoolService(){
		assertThatExceptionOfType(NullPointerException.class) //
			.isThrownBy(() -> new LotteryController(lottery, mock(BusinessTime.class), mock(UserService.class), null, mock(BetService.class)));
	}

	@Test
	void cantInitializeLotteryWithNullBetService(){
		assertThatExceptionOfType(NullPointerException.class) //
			.isThrownBy(() -> new LotteryController(lottery, mock(BusinessTime.class), mock(UserService.class), mock(PoolService.class), null));
	}
}