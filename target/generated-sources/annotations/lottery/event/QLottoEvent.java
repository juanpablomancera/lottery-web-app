package lottery.event;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLottoEvent is a Querydsl query type for LottoEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLottoEvent extends EntityPathBase<LottoEvent> {

    private static final long serialVersionUID = 1637524583L;

    public static final QLottoEvent lottoEvent = new QLottoEvent("lottoEvent");

    public final QEvent _super = new QEvent(this);

    public final ArrayPath<int[], Integer> adminWinningNumbers = createArray("adminWinningNumbers", int[].class);

    public final StringPath drawDate = createString("drawDate");

    public final NumberPath<Double> eventExpenses = createNumber("eventExpenses", Double.class);

    public final NumberPath<Double> eventIncome = createNumber("eventIncome", Double.class);

    public final NumberPath<Double> eventTotal = createNumber("eventTotal", Double.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final BooleanPath isActive = _super.isActive;

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final NumberPath<Integer> superNumber = createNumber("superNumber", Integer.class);

    public final NumberPath<Integer> supernumber = createNumber("supernumber", Integer.class);

    public final StringPath type = createString("type");

    public final ArrayPath<int[], Integer> winningNumbers = createArray("winningNumbers", int[].class);

    public QLottoEvent(String variable) {
        super(LottoEvent.class, forVariable(variable));
    }

    public QLottoEvent(Path<? extends LottoEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLottoEvent(PathMetadata metadata) {
        super(LottoEvent.class, metadata);
    }

}

