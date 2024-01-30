package lottery.user;

import jakarta.persistence.Tuple;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Password.UnencryptedPassword;
import org.salespointframework.useraccount.QPassword_EncryptedPassword;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Data initializer to create demo users
 *  @author Konrad Löhr, Juan Pablo Mancera
 */

@Component
@Order(1)
public class UserDataInitializer implements DataInitializer {

	private final UserAccountManagement userAccountManagement;

	private final UserService userService;

	public UserDataInitializer(UserAccountManagement userAccountManagement, UserService userService) {
		this.userAccountManagement = userAccountManagement;
		this.userService = userService;
	}

	/**
	 * Initializes the database with some users
	 */
	@Override
	public void initialize() {
		User user1 = new User(userAccountManagement.create(
			"admin", UnencryptedPassword.of("admin"), "admin@admin", Role.of("ADMIN")));
		user1.getUserAccount().add(Role.of("USER"));
		userService.saveUser(user1);

		User user2 = new User(userAccountManagement.create(
			"ShortAdmin", UnencryptedPassword.of("a"), "a@a", Role.of("ADMIN")));
		user2.getUserAccount().add(Role.of("USER"));
		userService.saveUser(user2);

		var password = UnencryptedPassword.of("123");
		var role = Role.of("USER");


		List.of(
			new User(userAccountManagement.create(
				"Pablo", password, "pablo@pablo", role)),

			new User(userAccountManagement.create(
				"Peter", password, "peter@peter", role)),

			new User(userAccountManagement.create(
				"Carlos", password, "carlos@carlos", role)),

			new User(userAccountManagement.create(
				"Jakub", password, "jakub@jakub", role)),

			new User(userAccountManagement.create(
				"Konrad", password, "konrad@konrad", role)),

			new User(userAccountManagement.create(
				"Jorge", password, "jorge@jorge", role)),

			new User(userAccountManagement.create(
				"Mareike", password, "mareike@mareike", role)),

			new User(userAccountManagement.create(
				"Jacob", password, "jacob@jacob", role))

		).forEach(user -> {
			user.addMoney(40D);
			userService.saveUser(user);
		});


	}
}
