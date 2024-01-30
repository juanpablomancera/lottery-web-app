package lottery.lottery;

import lottery.bet.BetService;
import lottery.event.*;
import lottery.pool.PoolService;
import lottery.repositories.EventRepository;
import lottery.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class LotteryUnitTests {
	@Autowired
	Lottery lottery;

	@Autowired
	UserService userService;

	@Autowired
	BetService betService;

	@Autowired
	PoolService poolService;

	@Test
	void areDefaultActiveEvents() {
		assertEquals(lottery.getAllActiveEvents().size(), 3);
	}

	@Test
	void areDefaultTotoEvents() {
		assertEquals(lottery.getAllActiveEvents().
			stream().filter(event -> Objects.equals(event.getType(), "FootballEvent"))
			.toList().size(), 1);
	}

	@Test
	void areDefaultLottoEvents() {
		assertEquals(lottery.getAllActiveEvents().
			stream().filter(event -> Objects.equals(event.getType(), "LottoEvent"))
			.toList().size(), 2);
	}

	@Test
	void noInactiveEvents() {
		assertEquals(lottery.getAllInactiveEvents().size(), 0);
	}

	@Test
	void existsElementWithId() {
		assertEquals(lottery.getEventById(1L).getType(), "LottoEvent");
	}
	@Test
	void notExistsElementWithId(){
		assertThat(lottery.getEventById(0L)).isNull();
	}
	@Test
	void getEventsShouldReturnInstanceOfEventRepository() {
		assertThat(lottery.getEvents()).isInstanceOf(EventRepository.class);
	}

	@Test
	void removeEventShouldReturnFalseIfNotFound() {
		assertThat(lottery.removeEvent(0L)).isFalse();
	}
	@Test
	void getIncomeAfterSetIncome(){
		lottery.setIncome(10);
		assertEquals(lottery.getIncome() , 10);
	}
	@Test
	void getExpensesAfterSetExpenses(){
		lottery.setExpenses(10);
		assertEquals(lottery.getExpenses() , 10);
	}

	@Test
	void getEventInfoFromLottoForm() {
		LottoEventForm lottoEventForm = new LottoEventForm(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
		assertEquals(lottoEventForm.getDrawDate(), LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
	}

	@Test
	void getEventInfoFromTotoForm(){
		TotoEventForm totoEventForm = new TotoEventForm(
				LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
				LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		assertEquals(totoEventForm.getDrawDate(), LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
		assertEquals(totoEventForm.getStartDate(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
	}

	@Test
	void lottoFormNullDateInvalid(){
		LottoEventForm lottoEventForm = new LottoEventForm(null);
		assertThat(lottoEventForm.isDateValid()).isFalse();
	}

	@Test
	void lottoFormEmptyDateInvalid(){
		LottoEventForm lottoEventForm = new LottoEventForm("");
		assertThat(lottoEventForm.isDateValid()).isFalse();
	}

	@Test
	void lottoFormValidDate(){
		LottoEventForm lottoEventForm = new LottoEventForm(LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE));
		assertThat(lottoEventForm.isDateValid()).isTrue();
	}

	@Test
	void lottoFormInvalidDate(){
		LottoEventForm lottoEventForm = new LottoEventForm(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		assertThat(lottoEventForm.isDateValid()).isFalse();
	}

	@Test
	void lottoEventNullDrawArgument(){
		LottoEvent lottoEvent = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);
		assertThrows(NullPointerException.class, () -> lottoEvent.drawEvent(null, null, null));
	}

	@Test
	void testMatchWithNullArguments(){
		assertThrows(IllegalArgumentException.class, () -> new Match(null, null, 0, 0, 0, null));
	}

	@Test
	void testMatchWithEmptyArguments(){
		assertThrows(IllegalArgumentException.class, () -> new Match("", "", 0, 0, 0, ""));
	}

	@Test
	void testMatchWithNegativeArguments(){
		assertThrows(IllegalArgumentException.class, () -> new Match("team1", "team2", -1, -1, -1, "-11.-22.-1"));
	}

	@Test
	void testMatchWithNegativeScoreArguments(){
		assertThrows(IllegalArgumentException.class, () -> new Match("team1", "team2", -1, -1, 0, "2022.01.21"));
	}

	@Test
	void testMatchWithNegativeMatchNumberArgument(){
		assertThrows(IllegalArgumentException.class, () -> new Match("team1", "team2", 0, 0, -1, "2022.01.21"));
	}

	@Test
	void testMatchWithValidArguments(){
		Match match = new Match("team1", "team2", 0, 0, 0, "2022.01.212");
		assertThat(match).isNotNull();
	}

	@Test
	void testThatMatchIsADraw(){
		Match match = new Match("team1", "team2", 0, 0, 0, "2022.01.21");
		assertThat(match.wasATie()).isTrue();
	}

	@Test
	void testThatMatchIsNotADraw(){
		Match match = new Match("team1", "team2", 1, 0, 0,"2022.01.21");
		assertThat(match.wasATie()).isFalse();
	}

	@Test
	void testThatMatchTieResultTotalIsCorrect(){
		Match match = new Match("team1", "team2", 1, 0, 0,"2022.01.21");
		assertThat(match.tieResultTotal()).isEqualTo(1);
	}

	@Test
	void testThatMatchToStringIsCorrect(){
		Match match = new Match("team1", "team2", 1, 0, 0,"2022.01.21");
		assertThat(match.toString()).isEqualTo("First team: team1 scored 1. Second team: team2 scored 0. Match number : 0. Total: 1");
	}

	@Test
	void eventDeactivation(){
		Event event = new LottoEvent(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 2);
		assertThat(event.isActive()).isTrue();
		event.deactivate();
		assertThat(event.isActive()).isFalse();
	}

	@Test
	void cantUpdateEventWhenEventIsNull(){
		assertThrows(NullPointerException.class, () -> lottery.updateEvent(null));
	}

	@Test
	void cantAddEventWhenEventIsNull(){
		assertThrows(NullPointerException.class, () -> lottery.addEvent(null));
	}
}
