package com.bom.master.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bom_feature_option")
public class FeatureOption extends BaseEntity {
    private Long featureId;
    private String optionCode;
    private String optionName;
}
