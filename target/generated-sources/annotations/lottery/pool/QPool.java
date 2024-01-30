package lottery.pool;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPool is a Querydsl query type for Pool
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPool extends EntityPathBase<Pool> {

    private static final long serialVersionUID = 1162658409L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPool pool = new QPool("pool");

    public final MapPath<Long, Integer, NumberPath<Integer>> amounts = this.<Long, Integer, NumberPath<Integer>>createMap("amounts", Long.class, Integer.class, NumberPath.class);

    public final NumberPath<Double> balance = createNumber("balance", Double.class);

    public final ListPath<lottery.event.Event, lottery.event.QEvent> events = this.<lottery.event.Event, lottery.event.QEvent>createList("events", lottery.event.Event.class, lottery.event.QEvent.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final lottery.user.QUser poolChef;

    public final ListPath<lottery.user.User, lottery.user.QUser> poolMembers = this.<lottery.user.User, lottery.user.QUser>createList("poolMembers", lottery.user.User.class, lottery.user.QUser.class, PathInits.DIRECT2);

    public QPool(String variable) {
        this(Pool.class, forVariable(variable), INITS);
    }

    public QPool(Path<? extends Pool> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPool(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPool(PathMetadata metadata, PathInits inits) {
        this(Pool.class, metadata, inits);
    }

    public QPool(Class<? extends Pool> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.poolChef = inits.isInitialized("poolChef") ? new lottery.user.QUser(forProperty("poolChef"), inits.get("poolChef")) : null;
    }

}

