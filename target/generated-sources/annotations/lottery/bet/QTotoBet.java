package lottery.bet;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTotoBet is a Querydsl query type for TotoBet
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTotoBet extends EntityPathBase<TotoBet> {

    private static final long serialVersionUID = 1105973977L;

    public static final QTotoBet totoBet = new QTotoBet("totoBet");

    public final QBet _super = new QBet(this);

    public final NumberPath<Double> amount = createNumber("amount", Double.class);

    //inherited
    public final NumberPath<Double> amountWon = _super.amountWon;

    //inherited
    public final StringPath date = _super.date;

    //inherited
    public final NumberPath<Long> event = _super.event;

    public final ArrayPath<Integer[], Integer> games = createArray("games", Integer[].class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final NumberPath<Long> poolId = _super.poolId;

    public final NumberPath<Double> price = createNumber("price", Double.class);

    //inherited
    public final EnumPath<BetState> state = _super.state;

    //inherited
    public final NumberPath<Long> user = _super.user;

    public QTotoBet(String variable) {
        super(TotoBet.class, forVariable(variable));
    }

    public QTotoBet(Path<? extends TotoBet> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTotoBet(PathMetadata metadata) {
        super(TotoBet.class, metadata);
    }

}

