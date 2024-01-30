package lottery.event;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lottery.bet.Bet;
import lottery.bet.BetService;
import lottery.bet.BetState;
import lottery.bet.TotoBet;
import lottery.pool.Pool;
import lottery.user.UserService;
import lottery.pool.PoolService;
import java.text.DecimalFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.*;

/**
 * This class represents the FootballEvent itself. It is responsible for managing the matches, drawing them and updating the
 * financial situation of the lottery.
 *
 * @Author Jakub Gawroński, Jorge Andres Picon Colmenares
 */
@Entity
public class FootballEvent extends Event {
	private String drawDate;
	private String firstMatchDate;
	private String type;

	private double eventIncome = 0;
	private double eventExpenses = 0;
	private double eventTotal = 0;

	@OneToMany(cascade = CascadeType.PERSIST)
	private List<Match> matches;

	public FootballEvent(String drawDate, String startDate) {
		this.drawDate = drawDate;
		this.firstMatchDate = startDate;
		setType(this.getClass().getSimpleName());
		this.matches = generateMatches();
	}

	public FootballEvent() {
		this.drawDate = LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE);
		this.firstMatchDate = LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE);
		this.matches = generateMatches();
	}

	/**
	 * This function retuns the generated matches
	 * @return List<Match> matches
	 */
	public List<Match> getMatches() {
		return matches;
	}

	/**
	 * This function generates the all matches for the event. In total 18 matches are
	 *  generated with random scores and teams.
	 * @return List<Match> matches
	 */
	private List<Match> generateMatches() {
		List<Match> matches = new ArrayList<>();
		List<Match> bundesligaTeams = getBundesligaMatches();
		List<Match> secondBundesligaTeams = getSecondBundesligaMatches();

		for (int i = 0; i < bundesligaTeams.size(); i++) {
			matches.add(bundesligaTeams.get(i));
			matches.add(secondBundesligaTeams.get(i));
		}
		for (int i = 0; i < matches.size(); i++) {
			matches.get(i).setMatchNumber(i + 1);
		}
		return matches;
	}

	/**
	 * This function returns the type of the event
	 * @return String type
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * This function sets the type of the event
	 * @param String type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * This function returns the draw date of the event
	 * @return String drawDate
	 */
	public String getDrawDate() {
		return drawDate;
	}
	
	public String convertDateFormat(String inputDate) {
        try {
            DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputDateFormat.parse(inputDate);

            DateFormat outputDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            return outputDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

	public String getFirstMatchDate() {
		return firstMatchDate;
	}

	public boolean isStarted(){
		boolean result = false;
		
		for(Match match : matches) {
			if (match.wasMatchPlayed()){
				result = true;
			}
		}

		return result;
	}

	/**
	 * This function draws the event, distributes the prizes, and returns the update financial situation of the lottery
	 * @param UserService userService
	 * @param BetService betService
	 * @param PoolService poolService
	 * @return Map<String, Double> event_finance
	 */
	@Override
	public Map<String, Double> drawEvent(UserService userService, BetService betService, PoolService poolService) {

		//checking if users have enough money in their account
		for (Bet bet : betService.getBetsByEvent(this.id).stream().filter(bet -> bet.getState() == BetState.OPEN).toList()) {
			if (userService.getUserById(bet.getUser()).isPresent()) {
				if (userService.getUserById(bet.getUser()).get().getAccountState() < bet.getPrice()) {
					bet.setToCanceled();
					System.out.println("UPPER");
					//subtracting fee
					userService.getUserById(bet.getUser()).get().substractMoney(3.0);
					//give user Notice
					userService.getUserById(bet.getUser()).get().giveNotice();
					removeUserFromPoolsHelper(bet, userService, poolService);
				} else {
					//user has enough money
					userService.getUserById(bet.getUser()).get().substractMoney(bet.getPrice());
				}
			} else {
				//there is no user for this bet
				System.out.println("BOTTOM");
				bet.setToCanceled();
			}
		}

		List<TotoBet> bets = new ArrayList<>();
		for (Bet bet : betService.getBetsByEvent(this.id)) {
			if (bet.getState() == BetState.OPEN) {
				bets.add((TotoBet) bet);
			}
		}

		// The six (or less) winning match numbers
		List<Integer> winningMatchesNumbers = calculateWinningMatches();

		HashMap<Integer, List<TotoBet>> winning_classes = assignToWinningClass(bets, winningMatchesNumbers);

		Map<Long, Double> poolWinningBets = new HashMap<>();

		// Divide the winnings between the winners
		poolWinningBets = divideWinnings(winning_classes, userService, betService, poolWinningBets);


		// Draw the event in the poolWinningBets
		drawEventInPools(poolWinningBets, poolService);

		// Calculate the new financial situation and give the prizes to the winners
		Map<String, Double> eventFinance = new HashMap<>();
		// Add the event income and expanses to the event finance situation and return it, so it can be added to the
		// lottery total
		DecimalFormat df = new DecimalFormat("0.00");

		// If user's system automatically parses floats with a comma
		eventFinance.put("income", Double.valueOf(df.format(eventIncome).replaceAll(",", ".")));
		eventFinance.put("expenses", Double.valueOf(df.format(eventExpenses).replaceAll(",", ".")));

		return eventFinance;
	}

	//helper function for drawEvent
	private void removeUserFromPoolsHelper(Bet bet, UserService userService, PoolService poolService){
		if (userService.getUserById(bet.getUser()).get().getGivenNotices() >= 10) {
			for (Pool pool : poolService.findAll()) {
				if (pool.getPoolMembers().contains(userService.getUserById(bet.getUser()).get())) {
					pool.removePoolMember(userService.getUserById(bet.getUser()).get());
				}
			}
		}
	}


	/**
	 * This function calculates the winning matches of the event, meaning at max six matches that ended in a draw,
	 * with the six being the ones with the highest total goals scored. If there are less than six matches that ended
	 * in a draw, all the ones that tied are returned.
	 * @return List<Integer> winningMatchesNumbers
	 */
	public List<Integer> calculateWinningMatches() {
		// The six (or less) winning match numbers
		List<Integer> winningMatchesNumbers = new ArrayList<>();

		// All the games that tied in this match day, with the goal total as key (so if the game was 2:2 then its under
		// the key 4)
		Map<Integer, List<Match>> tieGames = new HashMap<>();

		int numberOfGamesThatTied = 0;

		// Find all the games that tied and add them to their respective lists
		for (Match match : this.matches) {
			if (match.wasATie()) {
				if (tieGames.get(match.tieResultTotal()) == null) {
					List<Match> matches_with_this_result = new ArrayList<>();

					matches_with_this_result.add(match);

					tieGames.put(match.tieResultTotal(), matches_with_this_result);
					numberOfGamesThatTied++;
				} else {
					tieGames.get(match.tieResultTotal()).add(match);
					numberOfGamesThatTied++;
				}
			}
		}

		// If there are more than 6 games that tied, we need to pick the 6 games with the highest total goals
		if (numberOfGamesThatTied > 6) {
			// Sort the games by their total goals and reverse that list so the highest total goals are first
			List<Integer> matchesTotalsSorted = new ArrayList<>(tieGames.keySet());
			Collections.sort(matchesTotalsSorted);
			Collections.reverse(matchesTotalsSorted);

			// Iterate over the sorted list and add the match numbers to the winning matches list
			int index = 0;
			while (winningMatchesNumbers.size() < 6) {
				// Get all the games with this total goals
				List<Match> matchesOnIndex = tieGames.get(matchesTotalsSorted.get(index));

				// If there are less games with this total goals than there are places to be filled, just add them all
				if (matchesOnIndex.size() <= 6 - winningMatchesNumbers.size()) {
					for (Match m : matchesOnIndex) {
						winningMatchesNumbers.add(m.matchNumber);
					}
					index++;
				} else {
					// If there are more games with this total goals than there are places to be filled, we need to
					// pick the games with the highest match numbers (the index of the match in the event)
					// so the match that was played last is picked

					int placesToBeFilled = 6 - winningMatchesNumbers.size();
					List<Integer> biggestMatchNumbers = new ArrayList<>();

					for (Match m : matchesOnIndex) {
						biggestMatchNumbers.add(m.matchNumber);
					}

					Collections.sort(biggestMatchNumbers);
					Collections.reverse(biggestMatchNumbers);

					for (int i = 0; i < placesToBeFilled; i++) {
						for (Match m : matchesOnIndex) {
							if (m.matchNumber == biggestMatchNumbers.get(i)) {
								winningMatchesNumbers.add(m.matchNumber);
							}
						}
					}
					index++;
				}
			}
		} else {
			// If there are less than 6 games that tied, just add all the games to the winning matches list
			for (List<Match> match_list : tieGames.values()) {
				for (Match match : match_list) {
					winningMatchesNumbers.add(match.matchNumber);
				}
			}
		}

		// Set this.winningMatches so that the numbers can be displayed in the UI
		setAdminWinningNumbers(winningMatchesNumbers.toString());

		return winningMatchesNumbers;
	}

	/**
	 * This function returns the selected match numbers of a bet
	 * @return Match[] result
	 */
	public Match[] getMatchesByBet(TotoBet bet) {
		Match[] result = new Match[6];
		int index = 0;
		for (int num : bet.getGames()) {
			for (int i = 0; i < matches.size(); i++) {
				if (num - 1 == i) {
					result[index] = matches.get(i);
					index++;
				}
			}
		}

		return result;
	}

	/**
	 * This function assigns the bets to their respective winning classes based on the amount of correctly selected
	 * matches and returns a HashMap with the winning classes as keys and the bets as values
	 * @return HashMap<Integer, List<TotoBet>> winning_classes
	 */
	public HashMap<Integer, List<TotoBet>> assignToWinningClass(List<TotoBet> bets, List<Integer> winningMatchesNumbers) {
		HashMap<Integer, List<TotoBet>> winning_classes = new HashMap<>();

		winning_classes.put(6, new ArrayList<>());
		winning_classes.put(5, new ArrayList<>());
		winning_classes.put(4, new ArrayList<>());
		winning_classes.put(3, new ArrayList<>());
		winning_classes.put(2, new ArrayList<>());
		winning_classes.put(1, new ArrayList<>());

		// Calculation system
		for (TotoBet bet : bets) {
			int numbers_correct = 0;
			for (int num1 : bet.getGames()) {
				for (int num2 : winningMatchesNumbers) {
					if (num1 == num2) {
						numbers_correct++;
					}
				}
			}
			// Pick the winner class
			switch (numbers_correct) {
				case 6:
					winning_classes.get(6).add(bet);
					break;
				case 5:
					winning_classes.get(5).add(bet);
					break;
				case 4:
					winning_classes.get(4).add(bet);
					break;
				case 3:
					winning_classes.get(3).add(bet);
					break;
				case 2:
					winning_classes.get(2).add(bet);
					break;
				case 1:
					winning_classes.get(1).add(bet);
					break;
				default:
					// This bet did not win anything
					this.eventIncome += bet.getPrice();
					bet.setToLose();
					break;
			}
		}
		return winning_classes;
	}

	/**
	 * This function divides the winnings between the winners that were not part of a pool
	 * and returns a HashMap with the pool ids as keys and the
	 * amount of money won by the pool as values (if the bet was not part of a pool, the pool id is -1)
	 * @return Map<Long, Double> pools
	 */
	public Map<Long, Double> divideWinnings(HashMap<Integer, List<TotoBet>> winningClasses, UserService userService, BetService betService, Map<Long, Double> pools) {

		// Iterate over all the win classes and multiply all the amounts from bets with the
		// winning prize quota, then add this prize to expenses
		for (int i = 1; i <= 6; i++) {
			for (TotoBet bet : winningClasses.get(i)) {
				bet.setToWin();
				double prize = (bet.getPrice() * i) * 0.9;
				eventExpenses += prize * 0.9;
				eventIncome += prize * 0.1;

				DecimalFormat df = new DecimalFormat("0.00");
				prize = Double.valueOf(df.format(prize).replaceAll(",", "."));

				// Here we would add the prize to the user account or to the pool
				if (bet.getPoolId() != -1L) {
					if (pools.get(bet.getPoolId()) == null) {
						pools.put(bet.getPoolId(), prize);
					} else {
						pools.put(bet.getPoolId(), pools.get(bet.getPoolId()) + prize);
					}
				} else {
					userService.getUserById(bet.getUser()).orElseThrow().addMoney(prize);
					bet.setAmountWon(prize);
					betService.saveBet(bet);
				}
			}
		}

		return pools;
	}

	/**
	 * This function draws the event in the pools
	 * @param Map<Long, Double> pools
	 * @param PoolService poolService
	 */
	public void drawEventInPools(Map<Long, Double> pools, PoolService poolService) {
		for (Long poolID : pools.keySet()) {
			poolService.drawEventPool(poolID, getId(), pools.get(poolID));
		}
	}

	/**
	 * This function returns the information about an event as a string
	 * @return String eventInfo (type, id, active)
	 */
	public String toString() {
		return getType() + " " + getId() + " " + isActive();
	}

	/**
	 * This function returns a list of matches for the Bundesliga 1
	 * @return List<Match> matches
	 */
	private List<Match> getBundesligaMatches() {
		List<Match> matches = new ArrayList<>();
		List<String> bundesligaTeams = Arrays.asList("Bayer Leverkusen", "Bayern Muenchen", "Borussia Dortmund",
			"Borussia Moenchengladbach", "Eitracht Frankfurt", "FC Augsburg", "FC Koeln", "VfB Stuttgart",
			"Schalke 04", "Hertha Berliner SC", "RB Leipzig", "FsV Mainz 05", "VfL Bochum", "SC Freiburg",
			"FC Union Berlin", "TSG Hoffenheim", "VfL Wolfsburg", "Werder Bremen");

		return produceMatches(matches, bundesligaTeams);
	}

	/**
	 * This function returns a list of matches for the Bundesliga 2
	 * @return List<Match> matches
	 */
	private List<Match> getSecondBundesligaMatches() {
		List<Match> matches = new ArrayList<>();
		List<String> secondBundesligaTeams = Arrays.asList("Eintrach Braunschweig", "FC St. Pauli", "Fortuna Duesseldorf",
			"Greuther Fuerth", "Hamburger SV", "Hannover 96", "Holstein Kiel", "Karlsruher SC",
			"SC Paderborn 07", "SV Darmstadt 98", "SV Sandhausen", "VfL Bochum", "FC Ingolstadt 04",
			"1. FC Heidenheim", "1. FC Nuernberg", "1. FC Saarbruecken", "1. FC Kaiserslautern", "1. FC Magdeburg");

		return produceMatches(matches, secondBundesligaTeams);
	}

	/**
	 * This function produces the matches for the event. It creates a match between teams from the same league
	 * and then assigns a random score to the match
	 * @return List<Match> matches
	 */
	private List<Match> produceMatches(List<Match> matches, List<String> teams) {
		Collections.shuffle(teams);
		LocalDate localDrawDate = LocalDate.parse(drawDate,DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDate localStartDate = LocalDate.parse(firstMatchDate,DateTimeFormatter.ISO_LOCAL_DATE);
		Iterator<String> bundesligaTeamsIterator = teams.iterator();
		for (int i = 0; i < (teams.size() / 2); i++) {
			Random random = new Random();

			LocalDate matchDate = localStartDate.plusDays(random.nextLong(
				localStartDate.datesUntil(localDrawDate).count() + 1));
			int firstScore = random.nextInt(6);
			int secondScore = random.nextInt(6);
			matches.add(
				new Match(bundesligaTeamsIterator.next(), bundesligaTeamsIterator.next(),
					firstScore, secondScore, i, matchDate.toString()));
		}

		return matches;
	}
}
