package lottery.lottery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LotteryControllerWebIntegrationTests {

	@Autowired
	MockMvc mvc;
	@Autowired
	LotteryController controller;

	@Test
	void lotteryMvcIntegrationTest() throws Exception {

		mvc.perform(get("/lottery")) //
			.andExpect(status().isOk()) //
			.andExpect(model().attributeExists("events")) //
			.andExpect(model().attribute("events", is(not(emptyIterable()))));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void allowsAuthenticatedAccessToAddLottoEvents() throws Exception {

		mvc.perform(get("/lotto")) //
			.andExpect(status().isOk());//
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void allowsAuthenticatedAccessToAddTotoEvents() throws Exception {

		mvc.perform(get("/toto")) //
			.andExpect(status().isOk());//
	}

	@Test
	@WithMockUser(roles = "USER")
	void allowsUnauthenticatedAccessToAddLottoEvents() throws Exception {

		mvc.perform(get("/lotto")) //
			.andExpect(status().isForbidden());//
	}

	@Test
	@WithMockUser(roles = "USER")
	void allowsUnauthenticatedAccessToAddTotoEvents() throws Exception {

		mvc.perform(get("/toto")) //
			.andExpect(status().isForbidden());//
	}


}
