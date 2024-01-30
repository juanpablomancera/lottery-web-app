package lottery.pool;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@AutoConfigureMockMvc
class PoolFormTests {

	@Test
	void testCreateReadValues() throws Exception {
		var poolForm = new PoolForm("Test", "123", "chef@chef");
		assertThat(poolForm.getName()).isEqualTo("Test");
		assertThat(poolForm.getPassword()).isEqualTo("123");
		assertThat(poolForm.getPoolChef()).isEqualTo("chef@chef");
}
}