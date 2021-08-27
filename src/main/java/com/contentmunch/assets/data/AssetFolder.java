package com.contentmunch.assets.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Data
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "assets")
public class AssetFolder extends RepresentationModel<AssetFolder> {
    private String id;
    private String name;
}
