package lottery.pool;

import jakarta.validation.constraints.NotEmpty;

class PoolForm {

	@NotEmpty(message = "Name has to be picked")
	private final String name; 

	@NotEmpty(message = "Password has to be picked")
	private final String password; 

	@NotEmpty(message = "PoolChef has to be picked")
	private final String poolChef;

	public PoolForm(String name, String password, String poolChef) {
		this.name = name;
		this.password = password;
		this.poolChef = poolChef;
	}

	public String getName() {
		return name;
	}

	public String getPoolChef() {
		return poolChef;
	}

	public String getPassword() {
		return password;
	}
}
