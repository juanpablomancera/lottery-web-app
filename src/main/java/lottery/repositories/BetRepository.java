package lottery.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import lottery.bet.Bet;

public interface BetRepository extends JpaRepository<Bet, Long>{ 
}
