package lottery.user;

import jakarta.persistence.*;
import org.salespointframework.useraccount.UserAccount;
import java.text.DecimalFormat;

import java.util.Objects;

/**
 * Basic class for User, wraps salespoint UserAccount
 * @author Konrad Löhr, Juan Pablo Mancera
 */

@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//User class wrapps around UserAcoount from salespoint
	@OneToOne
	private UserAccount userAccount;


	private Double accountState = 0.0;

	/**
	 * Number of warnings given to a user. That means how many times the user didn't have enough money
	 * in his account when a toto bet was drawn, and he had bought a ticket.
	 * If givenNotices is 3, the user is deactivated
	 */
	private int givenNotices = 0;


	public User(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	public User() {
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public Long getId() {
		return id;
	}

	public Double getAccountState() {
		DecimalFormat df = new DecimalFormat("0.00");
		return Double.valueOf(df.format(accountState).replaceAll(",", "."));
	}

	public void addMoney(Double amount) {
		accountState += amount;
	}

	public void substractMoney(Double amount) {
		accountState -= amount;
	}

	public void giveNotice(){
		this.givenNotices++;
	}

	public int getGivenNotices(){
		return givenNotices;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o){
			return true;
		}
		if (o == null || getClass() != o.getClass()){
			return false;
		}
		User user = (User) o;
		return Objects.equals(id, user.id) &&
			Objects.equals(userAccount, user.userAccount) &&
			Objects.equals(accountState, user.accountState);
	}

	@Override
	public String toString() {
		return "User{" +
			"id=" + id +
			", userAccount=" + userAccount.getEmail() + "-" + userAccount.getPassword() +
			'}';
	}
}
