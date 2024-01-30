package lottery.bet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.assertj.core.api.StringAssert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Arrays;

@SpringBootTest
@AutoConfigureMockMvc
public class LottoBetFormUnitTests {
    @Test
	void testCreateReadValues() throws Exception {
        var lottoForm = new LottoBetForm("-1,10,1,3,11,20,4", "-1,2");
		assertArrayEquals(lottoForm.getLotteryNumbers(),new Integer[]{10,1,3,11,20,4});
		assertThat(lottoForm.getSupernumber()).isEqualTo(2);
        assertTrue(lottoForm.isValidCheckboxCount());
    }

    @Test
	void testNotEnughNumbers() throws Exception {
        var lottoForm = new LottoBetForm("10,1,20","-1,10");
		assertTrue(Arrays.stream(lottoForm.getLotteryNumbers()).allMatch(element -> element == null));
		assertNull(lottoForm.getSupernumber());
        assertFalse(lottoForm.isValidCheckboxCount());
    }

    @Test
	void testTooManyEnughNumbers() throws Exception {
        var lottoForm = new LottoBetForm("-1,10,1,20,12,4,6,9,11","-1,10");
		assertTrue(Arrays.stream(lottoForm.getLotteryNumbers()).allMatch(element -> element == null));
		assertNull(lottoForm.getSupernumber());
        assertFalse(lottoForm.isValidCheckboxCount());
    }

    @Test
	void testNoSupernumber() throws Exception {
        var lottoForm = new LottoBetForm("-1,10,1,20,12,4,6","-1");
		assertTrue(Arrays.stream(lottoForm.getLotteryNumbers()).allMatch(element -> element == null));
		assertNull(lottoForm.getSupernumber());
        assertFalse(lottoForm.isValidCheckboxCount());
    }

    @Test
	void testMoreThanOneSupernumber() throws Exception {
        var lottoForm = new LottoBetForm("-1,10,1,20,12,4,6","-1,4,2");
		assertTrue(Arrays.stream(lottoForm.getLotteryNumbers()).allMatch(element -> element == null));
		assertNull(lottoForm.getSupernumber());
        assertFalse(lottoForm.isValidCheckboxCount());
    }

	@Test
	void testCreateLottoBetFromForm() throws Exception {
		var lottoForm = new LottoBetForm("-1,10,1,3,11,20,4", "-1,2");
		var lottoBet = lottoForm.newLottoBet(100L, 100L, 100L, 10);
		Integer[] numbers = {10, 1, 3, 11, 20, 4};
		assertThat(lottoBet.getLotteryNumbers()).isEqualTo(numbers);
		assertThat(lottoBet.getSupernumber()).isEqualTo(2);
		assertThat(lottoBet.getPrice()).isEqualTo(10);
		assertThat(lottoBet.getState()).isEqualTo(BetState.OPEN);
		lottoBet.setToCanceled();
		assertThat(lottoBet.getState()).isEqualTo(BetState.CANCELED);
		lottoBet.setToLose();
		assertThat(lottoBet.getState()).isEqualTo(BetState.LOSE);
		lottoBet.setToWin();
		assertThat(lottoBet.getState()).isEqualTo(BetState.WIN);
		lottoBet.setToOpen();
		assertThat(lottoBet.getState()).isEqualTo(BetState.OPEN);
		assertThat(lottoBet.getDate()).isInstanceOf(String.class);
	}
}
