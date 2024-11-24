package com.example.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFeeds is a Querydsl query type for Feeds
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeeds extends EntityPathBase<Feeds> {

    private static final long serialVersionUID = 1736200143L;

    public static final QFeeds feeds = new QFeeds("feeds");

    public final QBasicEntity _super = new QBasicEntity(this);

    public final NumberPath<Long> fno = createNumber("fno", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modDate = _super.modDate;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regDate = _super.regDate;

    public final StringPath title = createString("title");

    public QFeeds(String variable) {
        super(Feeds.class, forVariable(variable));
    }

    public QFeeds(Path<? extends Feeds> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFeeds(PathMetadata metadata) {
        super(Feeds.class, metadata);
    }

}

