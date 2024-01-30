package lottery.event;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMatch is a Querydsl query type for Match
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMatch extends EntityPathBase<Match> {

    private static final long serialVersionUID = -1384884692L;

    public static final QMatch match = new QMatch("match");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath matchDay = createString("matchDay");

    public final NumberPath<Integer> matchNumber = createNumber("matchNumber", Integer.class);

    public final StringPath team1 = createString("team1");

    public final NumberPath<Integer> team1Score = createNumber("team1Score", Integer.class);

    public final StringPath team2 = createString("team2");

    public final NumberPath<Integer> team2Score = createNumber("team2Score", Integer.class);

    public QMatch(String variable) {
        super(Match.class, forVariable(variable));
    }

    public QMatch(Path<? extends Match> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMatch(PathMetadata metadata) {
        super(Match.class, metadata);
    }

}

