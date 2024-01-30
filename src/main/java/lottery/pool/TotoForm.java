package lottery.pool;

import jakarta.validation.constraints.Min;

public class TotoForm {

    @Min(value = 1, message = "Amount must be greater than or equal to 1")
	private final int amount;

    public TotoForm(int amount) {
        this.amount =  amount;
    }

    public int getAmount() {
        return amount;
    }
}
