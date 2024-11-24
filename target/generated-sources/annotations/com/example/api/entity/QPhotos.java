package com.example.api.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPhotos is a Querydsl query type for Photos
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPhotos extends EntityPathBase<Photos> {

    private static final long serialVersionUID = -1722995065L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPhotos photos = new QPhotos("photos");

    public final QBasicEntity _super = new QBasicEntity(this);

    public final QFeeds feeds;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modDate = _super.modDate;

    public final StringPath path = createString("path");

    public final StringPath photosName = createString("photosName");

    public final NumberPath<Long> pnum = createNumber("pnum", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> regDate = _super.regDate;

    public final StringPath uuid = createString("uuid");

    public QPhotos(String variable) {
        this(Photos.class, forVariable(variable), INITS);
    }

    public QPhotos(Path<? extends Photos> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPhotos(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPhotos(PathMetadata metadata, PathInits inits) {
        this(Photos.class, metadata, inits);
    }

    public QPhotos(Class<? extends Photos> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.feeds = inits.isInitialized("feeds") ? new QFeeds(forProperty("feeds")) : null;
    }

}

