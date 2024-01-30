package lottery.user;

import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit testing class for
 * @see UserService
 * @author Konrad Löhr, Juan Pablo Mancera
 */

@SpringBootTest
public class UserServiceUnitTests {


	@Autowired UserService userService;


	@Test
	void areDefaultAdmins(){
		List<User> admins = userService.getAllUsers().stream().filter(user ->
				user.getUserAccount().hasRole(Role.of("ADMIN"))).toList();
		assertEquals(admins.size(), 2);
	}

	@Test
	void areDefaultUsers(){
		List<User> admins = userService.getAllUsers().stream().filter(user ->
				user.getUserAccount().hasRole(Role.of("USER"))).toList();
		assertEquals(admins.size(), 8);
	}


	//Tests to check the correct functionality of the interaction to UserRepository


	@Test
	void getUserByEmailShouldReturnUserWhenFound() {
		RegistrationForm registrationForm = new RegistrationForm("test", "test@email", "password");
		User user = userService.registerUser(registrationForm, Role.of("USER"));

		assertThat(userService.getUserByEmail("test@email").orElseThrow()).isEqualTo(user);
	}


	@Test
	void getUserByEmailShouldReturnNullWhenNotFound(){
		assertThat(userService.getUserByEmail("wrong@email")).isEmpty();
	}




	@Test
	void isUserRegisteredShouldReturnTrueForExistingUser() {
		RegistrationForm registrationForm = new RegistrationForm("newTest", "newTest@email", "password");
		userService.registerUser(registrationForm, Role.of("USER"));

		assertThat(userService.isUserRegistered("newTest@email","newTest")).isTrue();
	}

	@Test
	void isUserRegisteredShouldReturnFalseForNonExistingUser() {
		assertThat(userService.isUserRegistered("random@email.com", "random")).isFalse();
	}

	@Test
	void getUserByIdShouldReturnSameUserAsGetUserByAccountIdentifierWhenFound() {
		RegistrationForm registrationForm = new RegistrationForm("pedro", "pedro@email", "password");
		User user = userService.registerUser(registrationForm, Role.of("USER"));

		long userId = user.getId();

		assertThat(userService.getUserById(userId)).isEqualTo(userService.getUserByAccountIdentifier(user.getUserAccount().getId()));
	}

	@Test
	void getUserByAccountIdentifierShouldReturnUserWhenFound() {
		RegistrationForm registrationForm = new RegistrationForm("name", "email@email", "password");
		userService.registerUser(registrationForm, Role.of("USER"));
		User user = userService.getUserByEmail("email@email").orElseThrow();

		UserAccount.UserAccountIdentifier accountIdentifier = user.getUserAccount().getId();

		assertThat(userService.getUserByAccountIdentifier(accountIdentifier).orElseThrow()).isEqualTo(user);
	}

	@Test
	void getUserByIdentifierShouldReturnUserWhenFound(){
		RegistrationForm registrationForm = new RegistrationForm("testtest", "testtestemail@email", "password");
		userService.registerUser(registrationForm, Role.of("USER"));
		User user = userService.getUserByEmail("testtestemail@email").orElseThrow();

		Long userId = user.getId();

		assertThat(userService.getUserById(userId).orElseThrow()).isEqualTo(user);

	}


	@Test
	void deleteUserByIdShouldDeleteUserFromRepositoryAndDisableAccount(){
		RegistrationForm registrationForm = new RegistrationForm("test2", "test2@email", "password");
		userService.registerUser(registrationForm, Role.of("USER"));
		User user = userService.getUserByEmail("test2@email").orElseThrow();

		Long userId = user.getId();
		userService.deleteUserByID(userId);
		assertThat(userService.getUserByEmail("test2@email")).isEmpty();
		assertThat(userService.isUserRegistered("test2@email", "test2")).isTrue();

	}

}
