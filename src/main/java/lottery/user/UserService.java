package lottery.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import lottery.repositories.UserRepository;

/**
 * class for Services with User and User repository
 *
 * @author Konrad Löhr, Juan Pablo Mancera
 */

@Service
public class UserService {

	private final UserRepository repository;

	private final UserAccountManagement userAccounts;

	public UserService(UserRepository repository, UserAccountManagement userAccounts) {
		this.repository = repository;
		this.userAccounts = userAccounts;
	}

	public User registerUser(RegistrationForm form, Role role) {
		Assert.notNull(form, "Registration form must not be null!");

		var password = Password.UnencryptedPassword.of(form.getPassword());
		var userAccount = userAccounts.create(form.getName(), password, form.getEmail(), role);
		return repository.save(new User(userAccount));
	}

	public void saveUser(User user) {
		repository.save(user);
	}

	public List<User> getAllUsers() {
		return repository.findAll();
	}

	public Optional<User> getUserByEmail(String email) {
		for (User user : repository.findAll()
		) {
			if (user.getUserAccount().getEmail().equals(email)) {
				return Optional.of(user);
			}
		}
		return Optional.empty();
	}

	public boolean isUserRegistered(String email, String username) {
		for (User user : repository.findAll()) {
			if (user.getUserAccount().getEmail().equals(email) || user.getUserAccount().getUsername().equals(username)) {
				return true;
			}
		}
		for(UserAccount userAccount : userAccounts.findDisabled()){
			if(userAccount.getEmail().equals(email) || userAccount.getUsername().equals(username)){
				return true;
			}
		}
		return false;
	}

	public boolean deleteUserByID(long id) {
		if (repository.findById(id).isPresent()) {
			userAccounts.disable(Objects.requireNonNull(repository.findById(id).get().getUserAccount().getId()));
			repository.deleteById(id);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Because of the use of Salespoint, sometimes the user is found by his UserAccountIdentifier that
	 * is inside the attribute UserAccount of the User
	 * @see User
	 * @param id
	 * @return optional with the user if the user is found
	 */
	public Optional<User> getUserByAccountIdentifier(UserAccount.UserAccountIdentifier id) {
		for (User user : repository.findAll()
		) {
			if (Objects.equals(user.getUserAccount().getId(), id)) {
				return Optional.of(user);
			}
		}
		return Optional.empty();
	}

	public Optional<User> getUserById(Long id) {
		for (User user : repository.findAll()
		) {
			if (Objects.equals(user.getId(), id)) {
				return Optional.of(user);
			}
		}
		return Optional.empty();
	}

	/**
	 *
	 * @param id
	 * @return true if the user is the last admin because it is not possible to delete the last admin
	 */
	public boolean isLastAdmin(Long id) {
		List<User> admins = new ArrayList<>();
		for (User user : repository.findAll()) {
			if (user.getUserAccount().getRoles().toList().get(0).equals(Role.of("ADMIN"))) {
				admins.add(user);
			}
		}
		if (admins.size() == 1) {
			return admins.get(0).getId().equals(id);
		}
		return false;
	}
}
