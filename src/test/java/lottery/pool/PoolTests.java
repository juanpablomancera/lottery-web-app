package lottery.pool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.salespointframework.useraccount.Role;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.salespointframework.useraccount.Password.UnencryptedPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import lottery.event.Event;
import lottery.user.UserDataInitializer;
import lottery.user.UserService;
import lottery.user.User;


class PoolTests {

	@Test
	void testCreateReadValues() throws Exception {
		var user = mock(User.class);
		when(user.getId()).thenReturn(1L);
		var pool = new Pool("Test", "123", user);
		assertThat(pool.getName()).isEqualTo("Test");
		assertThat(pool.getPoolChef()).isEqualTo(user);
		assertThat(pool.isValidPassword("123"));

		assertThat(pool.getEvents()).isEqualTo(new ArrayList<Event>());
		
		

		

	}

	@Test
	void testMemberFunctions() throws Exception {
		var user = mock(User.class);
		when(user.getId()).thenReturn(1L);
		var pool = new Pool("Test", "123", user);

		assertThat(pool.getPoolMembers()).isEqualTo(new ArrayList<>() );
		pool.addPoolMember(user);
		assertThat(pool.getPoolMembers()).isEqualTo(List.of(user));
	}


	@Test
	void testEventFunctions() throws Exception {
		var user = mock(User.class);
		when(user.getId()).thenReturn(1L);
		var pool = new Pool("Test", "123", user);

		var event = mock(Event.class);
		assertThat(pool.getEvents()).isEqualTo(new ArrayList<>());
		pool.addEvent(event, Integer.valueOf(-1));
		assertThat(pool.getEvents()).isEqualTo(List.of(event));
	}
}