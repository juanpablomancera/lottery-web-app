package lottery.pool;

import lottery.repositories.PoolRepository;
import lottery.user.*;
import lottery.bet.*;
import lottery.event.Event;

import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class PoolServiceUnitTests {
    
    @Test
    void testCreatingPool() {
        PoolRepository repository = mock(PoolRepository.class);
        when(repository.save(any())).then(i -> i.getArgument(0));

        UserService userService = mock(UserService.class);
        UserAccount userAccount = mock(UserAccount.class);
        User user = mock(User.class);
        when(userAccount.getEmail()).thenReturn("user@user");
        when(userService.getUserByEmail("user@user")).thenReturn(Optional.ofNullable(user));

        BetService betService = mock(BetService.class);

        PoolService poolService = new PoolService(repository, userService, betService);

        PoolForm poolForm = new PoolForm("poolname", "password", "user@user");

        Pool pool1 = poolService.createPoolFormForm(poolForm);
		assertThat(pool1.getPoolChef()).isNotNull();

        Pool pool2 = poolService.createPool("null", "null", "user@user");
		assertThat(pool2.getPoolChef()).isNotNull();
    }

    @Test
    void testPoolNameIsUsed() {
        String testpool = "test"; 
        PoolRepository poolRepository = mock(PoolRepository.class);
        Pool pool = mock(Pool.class);
        when(pool.getName()).thenReturn(testpool);
        when(poolRepository.findAll()).thenReturn(List.of(pool));

        PoolService poolService = new PoolService(poolRepository, null, null);
        boolean result = poolService.poolNameIsUsed(testpool);

        assertTrue(result, "Pool name should be considered used");
        assertFalse(poolService.poolNameIsUsed("NonExistentPool"), "Pool name should not be considered used");
    }

    @Test
    void testDrawEventPool() {
        double total_price = 0;
        double winnings = 50;
        BetService betService = mock(BetService.class);
        Event event = mock(Event.class);
        Pool pool = mock(Pool.class);
        Bet bet1 = mock(Bet.class);
        Bet bet2 = mock(Bet.class);

        when(betService.getBetsByEventAndPool(event.getId(), pool.getId())).thenReturn(List.of(bet1, bet2));
        when(bet1.getPrice()).thenReturn(1.0);
        when(bet2.getPrice()).thenReturn(4.0);
		for (var bet : betService.getBetsByEventAndPool(event.getId(), pool.getId())) {
			total_price += bet.getPrice();
		}
        assertEquals(5, total_price);

        double fac1 = bet1.getPrice()/total_price;
        assertEquals(0.2, fac1);
        double fac2 = bet2.getPrice()/total_price;
        assertEquals(0.8, fac2);
        
        double amount1 = fac1*winnings;
        assertEquals(10, amount1);
        double amount2 = fac2*winnings;
        assertEquals(40, amount2);
    }

    @Test
    void testIsUserInPool() {
        PoolRepository repository = mock(PoolRepository.class);
        when(repository.save(any())).then(i -> i.getArgument(0));
        UserService userService = mock(UserService.class);
        BetService betService = mock(BetService.class);
        PoolService poolService = new PoolService(repository, userService, betService);
        UserAccount userAccount1 = mock(UserAccount.class);
        UserAccount userAccount2 = mock(UserAccount.class);
        
        User poolChef = new User(userAccount1);
        when(userAccount1.getEmail()).thenReturn("user@user");
        when(userService.getUserByEmail(userAccount1.getEmail())).thenReturn(Optional.of(poolChef));
        User member = new User(userAccount2);
        Pool pool = poolService.createPool("null", "null", userAccount1.getEmail());
		assertThat(pool).isNotNull();
        pool.addPoolMember(member);

        assertTrue(pool.getPoolMembers().contains(member), "member should be Poolmember, but is not.");
        assertTrue(pool.getPoolChef().equals(poolChef), "poolChef should be Pool Chef, but is not.");
    }
}
