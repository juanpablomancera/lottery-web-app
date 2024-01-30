package lottery.bet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import lottery.repositories.BetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BetServiceUnitTests {

	@Autowired BetService betService;

	@Test
	void saveBetShouldCallRepositorySave(){
		BetRepository repository = mock(BetRepository.class);
		BetService betService = new BetService(repository);
		LottoBet lottoBet = new LottoBet();
		betService.saveBet(lottoBet);

		verify(repository, times(1)).save(eq(lottoBet));
	}

	@Test
	void getAllBetsShouldCallRepositoryFindAll(){
		BetRepository repository = mock(BetRepository.class);
		BetService betService = new BetService(repository);

		betService.getAllBets();
		verify(repository, times(1)).findAll();
	}


	@Test
	void getAllBetsByUserIdShouldReturnAllBetsWhenFound(){
		Integer[] lottoNumbers = {1,2,3,4,5,6};
		Integer[] games = {1,2,3,4,5,8};
		LottoBet lottoBet1 = new LottoBet(lottoNumbers, 7, 25.0, BetState.OPEN, 3L, 0L, 0L);
		TotoBet totoBet = new TotoBet(games, 10.0, BetState.OPEN, 3L, 0L, 0L);
		LottoBet lottoBet2 = new LottoBet(lottoNumbers, 11, 50.0, BetState.OPEN, 2L, 0L, 0L);
		betService.saveBet(lottoBet1);
		betService.saveBet(lottoBet2);
		betService.saveBet(totoBet);
		assertThat(betService.getAllBetsByUserId(3L)).hasSize(2);
	}

	@Test
	void getAllBetsByEventIdShouldReturnAllBetsWhenFound(){
		Integer[] lottoNumbers = {1,2,3,4,5,6};
		Integer[] games = {1,2,3,4,5,8};
		LottoBet lottoBet1 = new LottoBet(lottoNumbers, 7, 25.0, BetState.OPEN, 3L, 11L, 0L);
		TotoBet totoBet = new TotoBet(games, 10.0, BetState.OPEN, 3L, 13L, 0L);
		LottoBet lottoBet2 = new LottoBet(lottoNumbers, 11, 50.0, BetState.OPEN, 2L, 12L, 0L);
		LottoBet lottoBet3 = new LottoBet(lottoNumbers, 11, 50.0, BetState.OPEN, 2L, 12L, 0L);
		betService.saveBet(lottoBet1);
		betService.saveBet(lottoBet2);
		betService.saveBet(lottoBet3);
		betService.saveBet(totoBet);
		assertThat(betService.getBetsByEvent(12L)).hasSize(2);
	}

	@Test
	void getAllBetsByEventAndPoolShouldReturnAllBetsWhenFound(){
		Integer[] lottoNumbers = {1,2,3,4,5,6};
		Integer[] games = {1,2,3,4,5,8};
		LottoBet lottoBet1 = new LottoBet(lottoNumbers, 7, 25.0, BetState.OPEN, 3L, 5L, 0L);
		TotoBet totoBet = new TotoBet(games, 10.0, BetState.OPEN, 3L, 1L, 0L);
		LottoBet lottoBet2 = new LottoBet(lottoNumbers, 11, 50.0, BetState.OPEN, 2L, 0L, 8L);
		LottoBet lottoBet3 = new LottoBet(lottoNumbers, 7, 25.0, BetState.OPEN, 3L, 0L, 0L);
		TotoBet totoBet2 = new TotoBet(games, 10.0, BetState.OPEN, 3L, 7L, 0L);
		LottoBet lottoBet4 = new LottoBet(lottoNumbers, 11, 50.0, BetState.OPEN, 2L, 0L, 8L);
		betService.saveBet(lottoBet1);
		betService.saveBet(lottoBet2);
		betService.saveBet(totoBet);
		betService.saveBet(lottoBet3);
		betService.saveBet(lottoBet4);
		betService.saveBet(totoBet2);
		assertThat(betService.getBetsByEventAndPool(0L, 8L)).hasSize(2);
	}

	@Test
	void getAllLottoBetsShouldOnlyReturnLottoBets(){
		Integer[] lottoNumbers = {1,2,3,4,5,6};
		Integer[] games = {1,2,3,4,5,8};
		LottoBet lottoBet1 = new LottoBet(lottoNumbers, 7, 25.0, BetState.OPEN, 0L, 0L, 0L);
		TotoBet totoBet = new TotoBet(games, 10.0, BetState.OPEN, 0L, 0L, 0L);
		LottoBet lottoBet2 = new LottoBet(lottoNumbers, 11, 50.0, BetState.OPEN, 0L, 0L, 0L);
		betService.saveBet(lottoBet1);
		betService.saveBet(lottoBet2);
		betService.saveBet(totoBet);
		for(Bet bet : betService.getAllLottoBets()){
			assertThat(bet).isInstanceOf(LottoBet.class);
		}
	}

	@Test
	void getAllTotoBetsShouldOnlyReturnTotoBets(){
		Integer[] lottoNumbers = {1,2,3,4,5,6};
		Integer[] games = {1,2,3,4,5,8};
		LottoBet lottoBet1 = new LottoBet(lottoNumbers, 7, 25.0, BetState.OPEN, 0L, 0L, 0L);
		TotoBet totoBet = new TotoBet(games, 10.0, BetState.OPEN, 0L, 0L, 0L);
		LottoBet lottoBet2 = new LottoBet(lottoNumbers, 11, 50.0, BetState.OPEN, 0L, 0L, 0L);
		betService.saveBet(lottoBet1);
		betService.saveBet(lottoBet2);
		betService.saveBet(totoBet);
		for(Bet bet : betService.getAllTotoBets()){
			assertThat(bet).isInstanceOf(TotoBet.class);
		}
	}

	@Test
	void getBetByIdShouldReturnOptionalBet(){
		Integer[] lottoNumbers = {1,2,3,4,5,6,};
		Integer[] games = {1,2,3,4,5,6,};
		LottoBet lottoBet1 = new LottoBet(lottoNumbers, 7, 25.0, BetState.OPEN, 0L, 0L, 0L);
		TotoBet totoBet = new TotoBet(games, 10.0, BetState.OPEN, 0L, 0L, 0L);
		betService.saveBet(lottoBet1);
		betService.saveBet(totoBet);
		assertThat(betService.getBetById(lottoBet1.getId()).get().getId()).isEqualTo(lottoBet1.getId());
		assertThat(betService.getBetById(totoBet.getId()).get().getId()).isEqualTo(totoBet.getId());
	}

	@Test
	void getByIdShouldReturnEmptyOptionalForNonExistingBet(){
		assertThat(betService.getBetById(100L)).isEmpty();
	}

	@Test
	void deleteBetByIdShouldReturnTrueIfBetExists(){
		Integer[] games = {1,2,3,4,5,6,};
		TotoBet totoBet = new TotoBet(games, 10.0, BetState.OPEN, 0L, 0L, 0L);
		betService.saveBet(totoBet);
		assertThat(betService.deleteBetById(totoBet.getId())).isTrue();
	}

	@Test
	void deleteBetByIdShouldReturnFalseIfBetDoesNotExists(){
		assertThat(betService.deleteBetById(41L)).isFalse();
	}

}
