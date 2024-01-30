package lottery.event;
import jakarta.persistence.Entity;
import lottery.bet.BetService;
import lottery.bet.BetState;
import lottery.bet.LottoBet;
import lottery.pool.PoolService;
import lottery.user.UserService;
import java.text.DecimalFormat;

import java.util.*;

/**
 * This class represents the LottoEvent itself. It is responsible for managing the Lotto Bets, drawing them and updating
 * the financial situation of the lottery.
 * @author Jorge Andres Picon Colmenares, Jakub Gawroński
 */
@Entity
public class LottoEvent extends Event {

	private String drawDate;
	private String type;

	private Integer price;

	double eventExpenses = 0;

	double eventIncome = 0;

	double eventTotal = 0;
	int superNumber = -1;

	public LottoEvent(String drawDate, Integer price) {
		this.drawDate = drawDate;
		this.price = price;
		// So that the event type can be displayed in the admin overview
		setType(this.getClass().getSimpleName());
	}

	public LottoEvent() {
	}

	/**
	 *
	 * @return Draw Date for this Event
	 */
	public String getDrawDate() {
		return drawDate;
	}

	/**
	 *
	 * @return The price for each Bet belonging to this event
	 */
	public Integer getPrice() {
		return price;
	}

	public Integer getSupernumber(){
		return superNumber;
	}
  /**
	 * Function to generate draw for a Lotto type Event. Calculate the financial situation of the Lotto,
	 * send money to winners and after the draw calculate profits and losses
	 * @param userService
	 * @param betService
	 * @param poolService
	 * @return returns the financial situation after the draw in a Map with the Key "income" and "expenses"
	 */

	@Override
	public Map<String, Double> drawEvent(UserService userService, BetService betService, PoolService poolService) {
		if (userService == null || betService == null || poolService == null){
			throw new NullPointerException("Arguments must not be <<null>> in LottoEvent.drawEvent()");
		}

		// Get all the open bets for this event
		@SuppressWarnings("unchecked")
		List<LottoBet> bets = (List<LottoBet>)(List<?>) betService.getBetsByEvent(this.getId()).stream()
			.filter(bet -> bet.getState() == BetState.OPEN).toList();

		// Total amount of money generated in that event (100%)
		this.eventTotal = 0;

		// Here the random or selected by Admin winning lotto numbers
		int[] winningNumbers;
		if (this.adminWinningNumbers.equals("")) {
			winningNumbers = this.getWinningNumbers();
		} else {
			winningNumbers = getAdminWinningNumbers();
		}

		// Pick the winners and assign them to winning classes
		HashMap<Integer, List<LottoBet>> winningClasses = assignToWinningClass(bets, winningNumbers, this.superNumber);

		// A hashmap with the pool id and the amount of money that will be added to the pool
		Map<Long, Double> poolWinningBets = new HashMap<>();

		// Event income is calculated by 10% of the total plus of the bets of people that lost
		// Expenses are just all the reward payments summed up. So 90% from the total
		this.eventExpenses += this.eventTotal*0.9;
		this.eventIncome += this.eventTotal*0.1;

		// Divide the winnings between the winners
		// and update the poolWinningBets
		poolWinningBets = divideWinnings(winningClasses, userService, betService, poolWinningBets);

		// Draw the event in the poolWinningBets
		drawEventInPools(poolWinningBets, poolService);

		// Calculate the new financial situation and give the prizes to the winners
		Map<String, Double> eventFinance = new HashMap<>();

		this.eventExpenses = this.eventTotal - this.eventIncome;

		// Add the event income and expanses to the event finance situation and return it, so it can be added to the
		// lottery total
		DecimalFormat df = new DecimalFormat("0.00");

		// If user's system automatically parses floats with a comma
		eventFinance.put("income", Double.valueOf(df.format(this.eventIncome).replaceAll(",", ".")));
		eventFinance.put("expenses", Double.valueOf(df.format(this.eventExpenses).replaceAll(",", ".")));

		// Set Manual Winning Numbers so that the numbers can be displayed in the UI
		setAdminWinningNumbers(Arrays.toString(winningNumbers));

		return eventFinance;
	}

	/**
	 * Function to generate the winning numbers of this Event in a Random way
	 * @return Integer type array with the winning numbers (6 Random numbers) from 1 to 49
	 */
	public int[] getWinningNumbers() {
		List<Integer> generatedRandomNums = new ArrayList<>();
		Random rand = new Random();
		while(generatedRandomNums.size() < 6) {
			int randomInt = rand.nextInt(49) + 1;
			if (!generatedRandomNums.contains(randomInt)) {
				generatedRandomNums.add(randomInt);
			}
		}

		int[] winningNumbers = new int[6];
		for (int i = 0; i < winningNumbers.length; i++) {
			winningNumbers[i] = generatedRandomNums.get(i); // Gets a unique random integer
		}
		Random randSuperNumber = new Random();
		this.superNumber = randSuperNumber.nextInt(10);

		return winningNumbers;
	}

	/**
	 *  Function to generate the winning numbers of this Event selected by Admin
	 * @return Integer type array with the winning numbers (6 numbers selected by Admin) from 1 to 49
	 */
	@Override
	public int[] getAdminWinningNumbers(){

		String[] stringArrayMWNs = adminWinningNumbers.split(",");
		int stringArrayMWNsLength = stringArrayMWNs.length;

		int[] integerArrayMWNs = new int[stringArrayMWNsLength-1];

		for (int i = 0; i <stringArrayMWNsLength-1; i++) {
			integerArrayMWNs[i] = Integer.parseInt(stringArrayMWNs[i]);
		}
		this.superNumber = Integer.parseInt(stringArrayMWNs[stringArrayMWNsLength-1]);
		return integerArrayMWNs;
	}

	/**
	 * Function responsible for analyzing all bets on this event and adding the winning bets in their
	 * respective winning class according to correct numbers. There are 6 types of class, according to
	 * the number of correct numbers.Class 1 for 4 numbers, class 2 for 4 numbers + Super number, class 3
	 * for 5 numbers, class 4 for 5numbers + super number, class 5 for 6 numbers and class 6 for 6 numbers + super number
	 * @param bets Bets belonging to this event
	 * @param winningNumbers 6 winning numbers
	 * @param superNumber 1 super winning number
	 * @return a map in which each key is a Integer that represent the number of a winning class (1 to 6) where their
	 * respective values are a List of LottoBets
	 */
	public HashMap<Integer, List<LottoBet>> assignToWinningClass(List<LottoBet> bets, int[] winningNumbers, int superNumber){
		HashMap<Integer, List<LottoBet>> winning_classes = new HashMap<>();

		winning_classes.put(6, new ArrayList<>());
		winning_classes.put(5, new ArrayList<>());
		winning_classes.put(4, new ArrayList<>());
		winning_classes.put(3, new ArrayList<>());
		winning_classes.put(2, new ArrayList<>());
		winning_classes.put(1, new ArrayList<>());

		// Calculation system
		for(LottoBet bet: bets){
			// I add the amount obtained in each bet to the total amount of the event
			this.eventTotal += bet.getPrice();

			int numbers_correct = 0;
			for(int num1 : bet.getLotteryNumbers()){
				for(int num2 : winningNumbers){
					if (num1 == num2) {
						numbers_correct++;
					}
				}
			}
			if(bet.getSupernumber() == superNumber){
				numbers_correct += 10;
			}

			// Pick the winner class
			switch (numbers_correct) {
				case 16:
					winning_classes.get(6).add(bet);
					break;
				case 6:
					winning_classes.get(5).add(bet);
					break;
				case 15:
					winning_classes.get(4).add(bet);
					break;
				case 5:
					winning_classes.get(3).add(bet);
					break;
				case 14:
					winning_classes.get(2).add(bet);
					break;
				case 4:
					winning_classes.get(1).add(bet);
					break;
				default:
					bet.setToLose();
					break;
			}
		}

		return winning_classes;
	}

	public void drawEventInPools(Map<Long, Double> pools, PoolService poolService){
		for(Long poolID : pools.keySet()){
			poolService.drawEventPool(poolID, getId(), pools.get(poolID));
		}
	}

	/**
	 * Function to divide the total money collected by this event, 10% of the amount will be profit for the lottery and
	 * 90% will be distributed among the winners according to their winning class, in case there are no winners in that
	 * winning class the lottery will take that amount money as profit.
	 * @param winning_classes
	 * @param userService
	 * @param betService
	 * @param pools
	 * @return returns map of bets belonging to a pool
	 */
	public Map<Long, Double> divideWinnings(HashMap<Integer, List<LottoBet>> winning_classes, UserService userService, BetService betService, Map<Long, Double> pools){
		// Array with money percentage per winning classes {winning class1,...,winning class6}
		double[] percentage_per_winning_classes = {0.03, 0.07, 0.1, 0.2, 0.25,  0.35};

		// Iterate over all the win classes and divide the prize pool for each of the winners, then add this prize
		// to expenses
		for(int i = 1; i <= 6; i++){
			if (winning_classes.get(i).size() == 0){
				this.eventIncome += this.eventExpenses*percentage_per_winning_classes[i-1];
			}else {
				double win_amount = (this.eventExpenses*percentage_per_winning_classes[i-1]) / winning_classes.get(i).size();

				DecimalFormat df = new DecimalFormat("0.00");
				win_amount = Double.valueOf(df.format(win_amount).replaceAll(",", "."));

				for(LottoBet bet : winning_classes.get(i)){
					// Here we add the prize to the user account or to the pool
					bet.setToWin();
					if(bet.getPoolId() != -1L){
						// Add the money to the pool
						if(pools.get(bet.getPoolId()) == null){
							pools.put(bet.getPoolId(), win_amount);
						} else {
							pools.put(bet.getPoolId(), pools.get(bet.getPoolId()) + win_amount);
						}
					} else {
						// Send the money to the user
						userService.getUserById(bet.getUser()).orElseThrow().addMoney(win_amount);
						bet.setAmountWon(win_amount);
						betService.saveBet(bet);
					}
				}
			}
		}

		return pools;
	}

	@Override
	public String getType() {
		return this.type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String toString(){
		return getType() + " " + getId() + " " + isActive();
	}
}
