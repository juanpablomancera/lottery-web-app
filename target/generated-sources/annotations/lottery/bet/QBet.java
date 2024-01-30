package lottery.bet;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBet is a Querydsl query type for Bet
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBet extends EntityPathBase<Bet> {

    private static final long serialVersionUID = -1605679121L;

    public static final QBet bet = new QBet("bet");

    public final NumberPath<Double> amountWon = createNumber("amountWon", Double.class);

    public final StringPath date = createString("date");

    public final NumberPath<Long> event = createNumber("event", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> poolId = createNumber("poolId", Long.class);

    public final EnumPath<BetState> state = createEnum("state", BetState.class);

    public final NumberPath<Long> user = createNumber("user", Long.class);

    public QBet(String variable) {
        super(Bet.class, forVariable(variable));
    }

    public QBet(Path<? extends Bet> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBet(PathMetadata metadata) {
        super(Bet.class, metadata);
    }

}

