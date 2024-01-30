package lottery.pool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@AutoConfigureMockMvc
class JoinFormTests {

	@Test
	void testCreateReadValues(){
		var poolForm = new JoinForm("Test", "123");
		assertThat(poolForm.getName()).isEqualTo("Test");
		assertThat(poolForm.getPassword()).isEqualTo("123");
	}
}