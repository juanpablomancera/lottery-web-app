package lottery.bet;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLottoBet is a Querydsl query type for LottoBet
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLottoBet extends EntityPathBase<LottoBet> {

    private static final long serialVersionUID = -1126201657L;

    public static final QLottoBet lottoBet = new QLottoBet("lottoBet");

    public final QBet _super = new QBet(this);

    //inherited
    public final NumberPath<Double> amountWon = _super.amountWon;

    //inherited
    public final StringPath date = _super.date;

    //inherited
    public final NumberPath<Long> event = _super.event;

    public final DateTimePath<java.time.LocalDateTime> eventDate = createDateTime("eventDate", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final ArrayPath<Integer[], Integer> lotteryNumbers = createArray("lotteryNumbers", Integer[].class);

    public final ArrayPath<Integer[], Integer> lotterynumbers = createArray("lotterynumbers", Integer[].class);

    //inherited
    public final NumberPath<Long> poolId = _super.poolId;

    public final NumberPath<Double> price = createNumber("price", Double.class);

    //inherited
    public final EnumPath<BetState> state = _super.state;

    public final NumberPath<Integer> supernumber = createNumber("supernumber", Integer.class);

    //inherited
    public final NumberPath<Long> user = _super.user;

    public QLottoBet(String variable) {
        super(LottoBet.class, forVariable(variable));
    }

    public QLottoBet(Path<? extends LottoBet> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLottoBet(PathMetadata metadata) {
        super(LottoBet.class, metadata);
    }

}

