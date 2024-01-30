package lottery.pool;

import jakarta.validation.constraints.NotEmpty;

public class JoinForm {

	@NotEmpty(message = "Name has to be picked")
	private final String name;
	
	@NotEmpty(message = "Password has to be picked")
	private final String password;

	public JoinForm(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}
}
