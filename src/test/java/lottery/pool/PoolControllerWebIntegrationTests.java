package lottery.pool;

import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.AuthenticationManagement;
import org.salespointframework.useraccount.UserAccountManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import groovyjarjarantlr4.v4.Tool.Option;
import lottery.user.UserService;
import lottery.lottery.Lottery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import javax.swing.tree.ExpandVetoException;


@SpringBootTest
@AutoConfigureMockMvc
public class PoolControllerWebIntegrationTests {

		@Autowired
		MockMvc mvc;
		@Autowired 
		PoolController controller;

		@Autowired
		UserAccountManagement userAccountManagement;

		@Autowired
		UserService userService;

		@Autowired
		AuthenticationManagement authenticationManagement;

		@Autowired
		PoolService poolService;

		@Autowired
		Lottery lottery;

		@Test
		void notLooggedInPoolHome() throws Exception {
			mvc.perform(get("/poolhome")) //
					.andExpect(status().is3xxRedirection()); //
		}

		@Test
		void loogedInPoolHome() throws Exception {
			authenticationManagement.updateAuthentication(userService.getUserById(1L).get().getUserAccount());

			mvc.perform(get("/poolhome"))
					.andExpect(status().isOk());
					
		}


		@Test
		@WithMockUser(roles = "ADMIN")
		void poolView() throws Exception {
			mvc.perform(get("/pool/1")) //
					.andExpect(status().isOk()) //
					.andExpect(model().attributeExists("pool"));
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		void poolChefView() throws Exception {
			mvc.perform(get("/pool/1/chef")) //
					.andExpect(status().isOk()) //
					.andExpect(model().attributeExists("pool"));
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		void poolAdminView() throws Exception {
			mvc.perform(get("/pool/1/admin")) //
					.andExpect(status().isOk()) //
					.andExpect(model().attributeExists("pool"));
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		void removePool() throws Exception {
			mvc.perform(get("/pool/0/remove")) //
					.andExpect(status().is3xxRedirection());	
		}

		@Test
		void addEventList() throws Exception {
			authenticationManagement.updateAuthentication(poolService.findById(1L).get().getPoolChef().getUserAccount());

			mvc.perform(get("/pool/1/addevent"))
					.andExpect(status().isOk())
					.andExpect(model().attributeExists("events"));
		}

		@Test
		void removePoolMeber() throws Exception {
			authenticationManagement.updateAuthentication(poolService.findById(1L).get().getPoolChef().getUserAccount());
			var userId = poolService.findById(1L).get().getPoolMembers().get(0).getId();
			mvc.perform(get("/pool/1/remove/"+userId))
					.andExpect(status().is3xxRedirection());
		}

		@Test
		void addEventPool() throws Exception {
			var eventId = lottery.getAllActiveEvents().get(0).getId();

			mvc.perform(get("/pool/1/event/"+eventId+"/lotto"))
					.andExpect(status().is3xxRedirection());
		}
		
		@Test
		void openTotoBetfrompool() throws Exception {
			mvc.perform(post("/pool/3/FootballEvent/3"))
					.andExpect(status().is3xxRedirection());
		}

		@Test
		void openLottoBetfrompool() throws Exception {
			mvc.perform(post("/pool/3/LottoEvent/2"))
					.andExpect(status().is3xxRedirection());
		}

		@Test
		@WithMockUser(roles = "ADMIN")
		void create() throws Exception {
			mvc.perform(get("/create"))
					.andExpect(status().isOk());
		}

		@Test
		void modelAttributes() throws Exception {
			var user = Optional.ofNullable(userService.getUserById(1L).get().getUserAccount());
			assertThat(controller.currentUserName(user)).isEqualTo(user.get().getUsername());
			assertThat(controller.currentUserId(user)).isEqualTo(userService.getUserByAccountIdentifier(user.get().getId()).orElseThrow().getId());
			assertThat(controller.currentUserRole(user)).isEqualTo(userService.getUserByAccountIdentifier(user.get().getId()).orElseThrow().getUserAccount().getRoles().toList().get(0).toString());
		}
		
}
