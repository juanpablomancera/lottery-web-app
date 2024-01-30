package lottery.pool;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import lottery.event.Event;
import lottery.user.User;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.hibernate.collection.spi.PersistentMap;

@Entity
public class Pool {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Double balance;

	private String name;
	private String password;
	//private List<Integer> amounts = new ArrayList<Integer>();
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable()
	private	Map<Long, Integer> amounts;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	private List<Event> events = new ArrayList<Event>();

	@ManyToOne(optional = true)
	private User poolChef;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	private List<User> poolMembers = new ArrayList<>();

	@SuppressWarnings("unused")
	private Pool() {
		
	}

	/**
	 * Creates a new {@link Pool}
	 * @param name
	 * @param password
	 * @param poolChef
	 */


	public Pool(String name, String password, User poolChef) {
		this.name = name;
		this.password = password;
		this.poolChef = poolChef;
		this.amounts =  new HashMap<>();
		this.balance = 0.0;
	}


	/**
	 * Add a {@link User} to {@link Pool} as PoolMember
	 * @param user
	 */
	public void addPoolMember(User user) {
		poolMembers.add(user);
	}

	/**
	* Remove a PoolMember from {@link Pool} 
	* @param user
	*/

	public void removePoolMember(User user) {
		poolMembers.remove(user);
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public User getPoolChef() {
		return this.poolChef;
	}

	public List<User> getPoolMembers() {
		return this.poolMembers;
	}

	public Double getBalance() {
		System.out.println("Pool balance: " + balance);
		return balance;
	}

	public void setBalance(Double balance) {
		System.out.println("Pool balance set to: " + balance);
		this.balance = balance;
	}

	public void subBalance(Double amount) {
		System.out.println("Pool balance subbed by: " + amount);
		this.balance = this.balance - amount;
	}

	public void addBalance(Double amount) {
		System.out.printf("Pool balance added by: " + amount + "\n");
		this.balance = this.balance + amount;
	}

	/**
	 * Validates a given password
	 * @param password
	 * @return boolean
	 */

	public boolean isValidPassword(String password) {
		return this.password.hashCode() == password.hashCode();
	}

	public void addEvent(Event event, Integer amount) {
		events.add(event);
		amounts.put(event.getId(), amount);
	}

	public List<Event> getEvents() {
		return this.events;
	}

	public void setAmount(Long eventId, Integer amount) {
		this.amounts.put(eventId, amount);
	}

	public Integer getAmount(Long eventId) {
		return this.amounts.get(eventId);
	}

	public boolean isMember(User user) {
		for (var member : getPoolMembers()) {
			if(member == user) {
				return true;
			}
		}
		return false;
	}
}
