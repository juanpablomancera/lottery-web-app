package lottery.bet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Arrays;
import java.util.Objects;

@SpringBootTest
@AutoConfigureMockMvc
public class TotoBetFormUnitTests {
    @Test
	void testCreateReadValues(){
        var totoForm = new TotoBetForm(10.0, "-1,10,1,3,11,20,4");
        assertThat(totoForm.getAmount()).isEqualTo(10);
		assertArrayEquals(totoForm.getGames(),new Integer[]{10,1,3,11,20,4});
        assertTrue(totoForm.isValidCheckboxCount());
    }

    @Test
	void testNotEnughMatches(){
        var totoForm = new TotoBetForm(10.0, "10,1,20");
        assertThat(totoForm.getAmount()).isEqualTo(10);
        assertTrue(Arrays.stream(totoForm.getGames()).allMatch(Objects::isNull));
        assertFalse(totoForm.isValidCheckboxCount());
    }

    @Test
	void testTooManyMatches(){
        var totoForm = new TotoBetForm(10.0, "-1,10,1,3,5,6,8,15,2");
        assertThat(totoForm.getAmount()).isEqualTo(10);
		assertTrue(Arrays.stream(totoForm.getGames()).allMatch(Objects::isNull));
        assertFalse(totoForm.isValidCheckboxCount());
    }

    @Test
    void testWrongAmount(){
        var totoForm = new TotoBetForm(-10.0, "-1,10,1,3,5,6,8");
        assertTrue(Arrays.stream(totoForm.getGames()).allMatch(Objects::isNull));
        assertFalse(totoForm.isValidCheckboxCount());
    }

	@Test
	void testCreateTotoBetFromForm(){
		var totoForm = new TotoBetForm(10.0, "-1,10,1,3,11,20,4");
		Integer[] matches = {10, 1, 3, 11, 20, 4};
		var totoBet = totoForm.newTotoBet(50L, 50L, 50L);
		assertThat(totoBet.getGames()).isEqualTo(matches);
		assertThat(totoBet.getPrice()).isEqualTo(10);
	}
}
