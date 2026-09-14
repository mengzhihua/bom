package com.bom.master.entity;
import com.baomidou.mybatisplus.annotation.TableName; import com.bom.common.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode;
@Data @EqualsAndHashCode(callSuper=true) @TableName("bom_feature")
public class Feature extends BaseEntity { private Long vehicleModelId; private String feature,name; }
