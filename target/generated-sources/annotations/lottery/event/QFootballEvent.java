package lottery.event;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFootballEvent is a Querydsl query type for FootballEvent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFootballEvent extends EntityPathBase<FootballEvent> {

    private static final long serialVersionUID = -1724671852L;

    public static final QFootballEvent footballEvent = new QFootballEvent("footballEvent");

    public final QEvent _super = new QEvent(this);

    //inherited
    public final StringPath adminWinningNumbers = _super.adminWinningNumbers;

    public final StringPath drawDate = createString("drawDate");

    public final NumberPath<Double> eventExpenses = createNumber("eventExpenses", Double.class);

    public final NumberPath<Double> eventIncome = createNumber("eventIncome", Double.class);

    public final NumberPath<Double> eventTotal = createNumber("eventTotal", Double.class);

    public final StringPath firstMatchDate = createString("firstMatchDate");

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final BooleanPath isActive = _super.isActive;

    public final ListPath<Match, QMatch> matches = this.<Match, QMatch>createList("matches", Match.class, QMatch.class, PathInits.DIRECT2);

    public final StringPath type = createString("type");

    public QFootballEvent(String variable) {
        super(FootballEvent.class, forVariable(variable));
    }

    public QFootballEvent(Path<? extends FootballEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFootballEvent(PathMetadata metadata) {
        super(FootballEvent.class, metadata);
    }

}

