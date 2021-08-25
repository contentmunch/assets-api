package com.contentmunch.assets.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Data
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "assets")
public class Asset extends RepresentationModel<Asset> {
    private String id;
    private String name;
    private String description;
    private String folderId;
    private Integer height;
    private Integer width;
}
