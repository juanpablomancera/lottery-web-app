package lottery.event;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lottery.lottery.Lottery;

import java.time.LocalDate;

/**
 * This class represents the Matches uses for a Football Event. It is responsible for representing each Game
 * @author Jorge Andres Picon Colmenares, Jakub Gawroński
 */
@Entity
public class Match {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public int matchNumber;
	public String team1;
	public String team2;
	public int team1Score = 0;
	public int team2Score = 0;

	public String matchDay;

	public Match(String team1, String team2, int team1Score, int team2Score, int matchNumber, String matchDay) {
		if(team1 == null || team2 == null){
			throw new IllegalArgumentException("Arguments must not be <<null>> in Match.Match()");
		}

		if (team1.isEmpty() || team2.isEmpty() || team1Score < 0 || team2Score < 0 || matchNumber < 0){
			throw new IllegalArgumentException("Arguments must not be <<negativ>> or Empty in Match.Match()");
		}

		this.team1 = team1;
		this.team2 = team2;
		this.team1Score = team1Score;
		this.team2Score = team2Score;
		this.matchNumber = matchNumber;
		this.matchDay = matchDay;
	}

	protected Match(){}

	/**
	 * Function to calculate if the Match results in a tie
	 * @return Boolean true for tie and false for not tie
	 */
	public boolean wasATie() {
		return this.team1Score == this.team2Score;
	}

	/**
	 * Function to calculate the result (total goals) of the Match
	 * @return Integer of the amount of goals of the Match
	 */
	public int tieResultTotal(){
		return this.team1Score + this.team2Score;
	}

	public void setMatchNumber(int matchNumber) {
		this.matchNumber = matchNumber;
	}

	public String resultToString(){
		return this.team1Score + " : " + this.team2Score;
	}

	public String getMatchDay() {return matchDay;};

	public boolean isMatchDay(){
		return Lottery.getBusinessTime().getTime().toLocalDate().equals(LocalDate.parse(matchDay));
	}

	public boolean wasMatchPlayed(){
		return (Lottery.getBusinessTime().getTime().toLocalDate().isAfter(LocalDate.parse(matchDay)) || isMatchDay());
	}

	@Override
	public String toString(){
		return "First team: " + team1  + " scored " + team1Score +  ". Second team: " + team2 + " scored " + team2Score
				+ ". Match number : " + matchNumber + ". Total: " + tieResultTotal();
	}
}
