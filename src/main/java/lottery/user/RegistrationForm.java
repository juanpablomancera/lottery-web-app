package lottery.user;

import jakarta.validation.constraints.NotEmpty;

/**
 * Registration form to create new users
 * @author Konrad Löhr, Juan Pablo Mancera
 */

public class RegistrationForm {

	@NotEmpty(message = "Name has to be picked")
	private final String name;

	@NotEmpty(message = "Email has to be picked")
	private final String email;

	@NotEmpty(message = "Password has to be picked")
	private final String password;

	public RegistrationForm(String name, String email, String password) {
		this.name = name;
		this.email = email;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}
}
