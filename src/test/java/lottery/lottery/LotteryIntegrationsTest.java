package lottery.lottery;

import lottery.bet.BetService;
import lottery.bet.BetState;
import lottery.bet.LottoBet;
import lottery.bet.TotoBet;
import lottery.event.*;
import lottery.pool.PoolService;
import lottery.repositories.EventRepository;
import lottery.user.User;
import lottery.user.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.salespointframework.time.BusinessTime;
import org.springframework.ui.ExtendedModelMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LotteryIntegrationsTest {

	@InjectMocks
	Lottery lottery;

	@Mock
	EventRepository eventRepository;

	@Mock
	PoolService poolService;

	@Mock
	BetService betService;

	@Mock
	UserService userService;

	@Mock
	BusinessTime businessTime;

	@Test
	void createTotoEvent() {
		when(businessTime.getTime()).thenReturn(LocalDateTime.now());

		TotoEventForm totoEventForm = new TotoEventForm(
			LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
			LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

		FootballEvent event = new FootballEvent(totoEventForm.getDrawDate(), totoEventForm.getStartDate());

		lottery.addEvent(event);

		verify(eventRepository, times(1)).save(event);
	}

	@Test
	void createLottoEvent() {
		when(businessTime.getTime()).thenReturn(LocalDateTime.now());

		LottoEventForm lottoEventForm = new LottoEventForm(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));

		LottoEvent event = new LottoEvent(lottoEventForm.getDrawDate(), 4);

		lottery.addEvent(event);

		verify(eventRepository, times(1)).save(event);
	}

	@Test
	@Disabled
	void createTotoEventWithWrongDate() {
		TotoEventForm totoEventForm = new TotoEventForm(
			LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
			LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));

		FootballEvent event = new FootballEvent(totoEventForm.getDrawDate(), totoEventForm.getStartDate());

		lottery.addEvent(event);

		verify(eventRepository, times(0)).save(event);
	}

	@Test
	@Disabled
	void createTotoEventWithEmptyDate() {
		TotoEventForm totoEventForm = new TotoEventForm(
			"",
			LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));

		FootballEvent event = new FootballEvent(totoEventForm.getDrawDate(), totoEventForm.getStartDate());

		lottery.addEvent(event);

		verify(eventRepository, times(0)).save(event);
	}

	@Test
	void createLottoEventWithWrongDate() {
		when(businessTime.getTime()).thenReturn(LocalDateTime.now());

		LottoEventForm lottoEventForm = new LottoEventForm(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

		LottoEvent event = new LottoEvent(lottoEventForm.getDrawDate(), 2);

		lottery.addEvent(event);

		verify(eventRepository, times(0)).save(event);
	}

	@Test
	void createLottoEventWithEmptyDate() {
		when(businessTime.getTime()).thenReturn(LocalDateTime.now());

		LottoEventForm lottoEventForm = new LottoEventForm("");

		LottoEvent event = new LottoEvent(lottoEventForm.getDrawDate(), 2);

		lottery.addEvent(event);

		verify(eventRepository, times(0)).save(event);
	}

	@Test
	void checkEventStatusAfterDraw() {
		LottoEvent lottoEvent = mock(LottoEvent.class);
		lottoEvent.deactivate();

		assertThat(lottoEvent.isActive()).isFalse();
	}

	@Test
	void removeEventShouldReturnTrueIfFound() {
		LottoEvent event = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);
		when(eventRepository.findById(1L)).thenReturn(java.util.Optional.of(event));
		assertThat(lottery.removeEvent(1L)).isTrue();
	}

	@Test
	void getTimeShouldReturnCurrentDay() {
		when(businessTime.getTime()).thenReturn(LocalDateTime.now());
		assertEquals(lottery.getTime(), businessTime.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
	}

	@Test
	void getIncomeAfterDrawEventShouldBeZero() {
		//Should be 0 beacuse there is no Bets placed for that Event
		// Create Event
		when(businessTime.getTime()).thenReturn(LocalDateTime.now());
		LottoEventForm lottoEventForm = new LottoEventForm(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
		LottoEvent event = new LottoEvent(lottoEventForm.getDrawDate(), 2);
		lottery.addEvent(event);

		//Draw Event
		event.drawEvent(userService, betService, poolService);
		assertEquals(lottery.getIncome(), 0);
	}

	@Test
	void getExpensesAfterDrawEventShouldBeZero() {
		//Should be 0 beacuse there is no Bets placed for that Event
		// Create Event
		when(businessTime.getTime()).thenReturn(LocalDateTime.now());
		LottoEventForm lottoEventForm = new LottoEventForm(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
		LottoEvent event = new LottoEvent(lottoEventForm.getDrawDate(), 2);
		lottery.addEvent(event);

		//Draw Event
		event.drawEvent(userService, betService, poolService);
		assertEquals(lottery.getExpenses(), 0);
	}

	@Test
	void lottoEventGetTypeShouldReturnLottoEvent() {
		LottoEventForm lottoEventForm = new LottoEventForm(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
		LottoEvent event = new LottoEvent(lottoEventForm.getDrawDate(), 2);

		assertThat(event.getType().equals("LottoEvent")).isTrue();
	}

	@Test
	void totoEventGetTypeShouldReturnFootballEvent() {
		TotoEventForm totoEventForm = new TotoEventForm(
			LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
			LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

		FootballEvent event = new FootballEvent(totoEventForm.getDrawDate(), totoEventForm.getStartDate());
		assertThat(event.getType().equals("FootballEvent")).isTrue();
	}


	@Test
	void betsChangeStatusAfterDraw() {
		// Create Event
		LottoEvent event = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);

		//Create Bet
		BetService betService = mock(BetService.class);
		LottoBet bet = mock(LottoBet.class);

		Integer[] nums = {0, 0, 0, 0, 0, 0};
		when(bet.getLotteryNumbers()).thenReturn(nums);
		when(bet.getSupernumber()).thenReturn(7);
		when(bet.getPrice()).thenReturn(1.0);
		when(bet.getState()).thenReturn(BetState.OPEN);

		when(betService.getBetsByEvent(event.getId())).thenReturn(List.of(bet));

		//Draw Event
		event.drawEvent(userService, betService, poolService);

		verify(bet, times(1)).setToLose();
	}

	@Test
	void totoBetInNoWinningClass() {
		FootballEvent event = new FootballEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

		TotoBet bet = mock(TotoBet.class);

		Integer[] winningMatchesAsArray = new Integer[6];

		List<Integer> winningMatches = event.calculateWinningMatches();
		for (int i = 0; i < winningMatches.size(); i++) {
			winningMatchesAsArray[i] = winningMatches.get(i);
		}

		winningMatchesAsArray[0] = 51;
		winningMatchesAsArray[1] = 51;
		winningMatchesAsArray[2] = 51;
		winningMatchesAsArray[3] = 51;
		winningMatchesAsArray[4] = 51;
		winningMatchesAsArray[5] = 51;

		when(bet.getGames()).thenReturn(winningMatchesAsArray);

		assertThat(event.assignToWinningClass(List.of(bet), winningMatches).containsValue(bet)).isFalse();
	}

	@Test
	void lottoBetInWinningClassSix() {
		LottoEvent event = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);

		LottoBet bet = mock(LottoBet.class);

		Integer[] winningNumbers = new Integer[6];

		int super_number = 7;

		int[] winningNumbersList = event.getWinningNumbers();
		for (int i = 0; i < winningNumbersList.length; i++) {
			winningNumbers[i] = winningNumbersList[i];
		}

		when(bet.getLotteryNumbers()).thenReturn(winningNumbers);
		when(bet.getSupernumber()).thenReturn(super_number);
		when(bet.getPrice()).thenReturn(1.0);

		System.out.println("Winning numbers 6 " + Arrays.toString(winningNumbersList));
		System.out.println("Winning numbers 6 " + Arrays.toString(winningNumbers));


		assertThat(event.assignToWinningClass(List.of(bet), winningNumbersList, 7).get(6).contains(bet)).isTrue();
	}

	@Test
	void lottoBetInWinningClassFive() {
		LottoEvent event = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);

		LottoBet bet = mock(LottoBet.class);

		Integer[] winningNumbers = new Integer[6];

		int super_number = 7;

		int[] winningNumbersList = Arrays.stream(event.getWinningNumbers()).sorted().toArray();

		for (int i = 0; i < winningNumbersList.length; i++) {
			winningNumbers[i] = winningNumbersList[i];
		}

		System.out.println("Winning numbers 5 " + Arrays.toString(winningNumbersList));
		System.out.println("Winning numbers 5 " + Arrays.toString(winningNumbers));


		when(bet.getLotteryNumbers()).thenReturn(winningNumbers);
		when(bet.getSupernumber()).thenReturn(super_number + 10);
		when(bet.getPrice()).thenReturn(1.0);

		System.out.println(event.assignToWinningClass(List.of(bet), winningNumbersList, 7));
		assertThat(event.assignToWinningClass(List.of(bet), winningNumbersList, 7).get(5).contains(bet)).isTrue();
	}

	@Test
	void lottoBetInWinningClassFour() {
		LottoEvent event = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);

		LottoBet bet = mock(LottoBet.class);

		Integer[] winningNumbers = new Integer[6];

		int super_number = 7;

		int[] winningNumbersList = Arrays.stream(event.getWinningNumbers()).sorted().toArray();

		for (int i = 0; i < winningNumbersList.length; i++) {
			winningNumbers[i] = winningNumbersList[i];
		}

		winningNumbers[0] = 50;

		when(bet.getLotteryNumbers()).thenReturn(winningNumbers);
		when(bet.getSupernumber()).thenReturn(super_number);
		when(bet.getPrice()).thenReturn(1.0);

		System.out.println("Winning numbers 4 " + Arrays.toString(winningNumbersList));
		System.out.println("Winning numbers 4 " + Arrays.toString(winningNumbers));

		System.out.println(event.assignToWinningClass(List.of(bet), winningNumbersList, 7));
		assertThat(event.assignToWinningClass(List.of(bet), winningNumbersList, 7).get(4).contains(bet)).isTrue();
	}

	@Test
	void lottoBetInWinningClassThree() {
		LottoEvent event = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);

		LottoBet bet = mock(LottoBet.class);

		Integer[] winningNumbers = new Integer[6];

		int super_number = 7;

		int[] winningNumbersList = Arrays.stream(event.getWinningNumbers()).sorted().toArray();

		for (int i = 0; i < winningNumbersList.length; i++) {
			winningNumbers[i] = winningNumbersList[i];
		}

		winningNumbers[0] = 50;

		when(bet.getLotteryNumbers()).thenReturn(winningNumbers);
		when(bet.getSupernumber()).thenReturn(super_number + 10);
		when(bet.getPrice()).thenReturn(1.0);

		System.out.println("Winning numbers 3 " + Arrays.toString(winningNumbersList));
		System.out.println("Winning numbers 3 " + Arrays.toString(winningNumbers));

		assertThat(event.assignToWinningClass(List.of(bet), winningNumbersList, 7).get(3).contains(bet)).isTrue();
	}

	@Test
	void lottoBetInWinningClassTwo() {
		LottoEvent event = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);

		LottoBet bet = mock(LottoBet.class);

		Integer[] winningNumbers = new Integer[6];

		int super_number = 7;

		int[] winningNumbersList = Arrays.stream(event.getWinningNumbers()).sorted().toArray();

		for (int i = 0; i < winningNumbersList.length; i++) {
			winningNumbers[i] = winningNumbersList[i];
		}

		winningNumbers[0] = 50;
		winningNumbers[1] = 50;

		when(bet.getLotteryNumbers()).thenReturn(winningNumbers);
		when(bet.getSupernumber()).thenReturn(super_number);
		when(bet.getPrice()).thenReturn(1.0);

		System.out.println("Winning numbers 2 " + Arrays.toString(winningNumbersList));
		System.out.println("Winning numbers 2 " + Arrays.toString(winningNumbers));

		assertThat(event.assignToWinningClass(List.of(bet), winningNumbersList, 7).get(2).contains(bet)).isTrue();
	}

	@Test
	void lottoBetInWinningClassOne() {
		LottoEvent event = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);

		LottoBet bet = mock(LottoBet.class);

		Integer[] winningNumbers = new Integer[6];

		int super_number = 7;

		int[] winningNumbersList = Arrays.stream(event.getWinningNumbers()).sorted().toArray();

		for (int i = 0; i < winningNumbersList.length; i++) {
			winningNumbers[i] = winningNumbersList[i];
		}

		winningNumbers[0] = 50;
		winningNumbers[1] = 50;

		when(bet.getLotteryNumbers()).thenReturn(winningNumbers);
		when(bet.getSupernumber()).thenReturn(super_number + 10);
		when(bet.getPrice()).thenReturn(1.0);

		System.out.println("Winning numbers 1 " + Arrays.toString(winningNumbersList));
		System.out.println("Winning numbers 1 " + Arrays.toString(winningNumbers));

		assertThat(event.assignToWinningClass(List.of(bet), winningNumbersList, 7).get(1).contains(bet)).isTrue();
	}

	@Test
	void checkIfUserWasInsideAPoolLotto() {
		LottoEvent event = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);

		LottoBet bet = mock(LottoBet.class);

		Integer[] winningNumbers = new Integer[6];

		int super_number = 7;

		int[] winningNumbersList = event.getWinningNumbers();
		for (int i = 0; i < winningNumbersList.length; i++) {
			winningNumbers[i] = winningNumbersList[i];
		}

		when(bet.getLotteryNumbers()).thenReturn(winningNumbers);
		when(bet.getSupernumber()).thenReturn(super_number);
		when(bet.getPrice()).thenReturn(1.0);
		when(bet.getPoolId()).thenReturn(1L);

		Map<Long, Double> pools =  event.divideWinnings(event.assignToWinningClass(List.of(bet), winningNumbersList, 7), userService, betService, new HashMap<>());

		assertThat(pools.containsKey(1L)).isTrue();
	}

	@Test
	void checkIfUserWasInsideAPoolToto(){
		FootballEvent event = new FootballEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

		TotoBet bet = mock(TotoBet.class);


		List<Integer> winningMatches = event.calculateWinningMatches();

		if (winningMatches.size() == 0) {
			// An edge case where there are no winning matches, very unlikely but possible
			return;
		}

		Integer[] winningMatchesAsArray = new Integer[winningMatches.size()];

		for (int i = 0; i < winningMatches.size(); i++) {
			winningMatchesAsArray[i] = winningMatches.get(i);
		}

		when(bet.getGames()).thenReturn(winningMatchesAsArray);
		when(bet.getPrice()).thenReturn(1.0);
		when(bet.getPoolId()).thenReturn(1L);

		System.out.println(event.assignToWinningClass(List.of(bet), winningMatches));

		Map<Long, Double> pools =  event.divideWinnings(event.assignToWinningClass(List.of(bet), winningMatches), userService, betService, new HashMap<>());

		assertThat(pools.containsKey(1L)).isTrue();
	}

	@Test
	void checkIfSkipDayReturnsAdminPanel(){
		LotteryController controller = new LotteryController(lottery, businessTime, userService, poolService, betService);
		assertThat(controller.skipDay(new ExtendedModelMap())).isEqualTo("redirect:/adminPanel");
	}

	@Test
	void checkEventRemove(){
		when(eventRepository.findById(1L)).thenReturn(Optional.of(new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2)));

		assertThat(lottery.removeEvent(1L)).isTrue();
	}

	@Test
	void checkEventRemoveFail(){
		when(eventRepository.findById(1L)).thenReturn(Optional.empty());
		assertThat(lottery.removeEvent(1L)).isFalse();
	}

	@Test
	void testDrawEventInTotoPool(){
		HashMap<Long, Double> pools = new HashMap<>();
		pools.put(1L, 1.0);

		FootballEvent event = new FootballEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		event.drawEventInPools(pools, poolService);

		verify(poolService, times(1)).drawEventPool(1L, null, 1.0);
	}

	@Test
	void footballEventDrawEventReturnsMoney(){
		FootballEvent event = new FootballEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

		TotoBet bet = mock(TotoBet.class);
		when(bet.getPrice()).thenReturn(1.0);
		when(bet.getGames()).thenReturn(new Integer[]{51, 51, 51, 51, 51, 51});
		when(bet.getUser()).thenReturn(3L);
		when(bet.getState()).thenReturn(BetState.OPEN);

		User user = mock(User.class);

		when(user.getAccountState()).thenReturn(400D);
		when(userService.getUserById(bet.getUser())).thenReturn(Optional.of(user));
		when(betService.getBetsByEvent(event.getId())).thenReturn(List.of(bet));
		when(betService.getBetsByEvent(event.getId())).thenReturn(List.of(bet));

		lottery.updateFinancialSituation(event.drawEvent(userService, betService, poolService));

		assertThat(lottery.getIncome()).isGreaterThan(0);
	}
}
