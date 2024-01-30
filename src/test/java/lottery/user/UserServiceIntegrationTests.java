package lottery.user;

import lottery.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 * Integretion testing class for
 * @see UserService
 */

@SpringBootTest
public class UserServiceIntegrationTests {
	@Test
	void saveUserShouldCallRepositorySave() {
		UserRepository repository = mock(UserRepository.class);
		UserAccountManagement userAccountManager = mock(UserAccountManagement.class);
		UserService userService = new UserService(repository, userAccountManager);

		User user = new User();
		userService.saveUser(user);

		verify(repository, times(1)).save(eq(user));
	}


	@Test
	void getAllUsersShouldCallRepositoryFindAll() {
		UserRepository repository = mock(UserRepository.class);
		UserAccountManagement userAccountManager = mock(UserAccountManagement.class);
		UserService userService = new UserService(repository, userAccountManager);

		userService.getAllUsers();

		verify(repository, times(1)).findAll();
	}

	//Testing if a UserAccount of Salespoint is correctly associated with User when User is created.
	@Test
	void createUserAccountWhenCreatingUser(){

		UserRepository repository = mock(UserRepository.class);
		when(repository.save(any())).then(i -> i.getArgument(0));

		UserAccountManagement userAccountManager = mock(UserAccountManagement.class);
		UserAccount userAccount = mock(UserAccount.class);
		when(userAccountManager.create( any(), any(), (String) any(), any(Role.class))).thenReturn(userAccount);

		UserService userService = new UserService(repository, userAccountManager);

		RegistrationForm registrationForm = new RegistrationForm("test", "test@email", "password");
		User user = userService.registerUser(registrationForm, Role.of("USER"));

		verify(userAccountManager, times(1))
			.create(eq(registrationForm.getName()),
				eq(Password.UnencryptedPassword.of(registrationForm.getPassword())),
				eq(registrationForm.getEmail()),
				eq(Role.of("USER")));

		// the user has a user account attached
		assertThat(user.getUserAccount()).isNotNull();
	}

}
