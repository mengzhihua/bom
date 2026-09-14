package com.bom.change.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bom_ecn_item")
public class EcnItem extends BaseEntity {
    private Long ecnId;
    private Long parentPartId;
    private Long oldChildPartId;
    private Long newChildPartId;
    private String action;
    private String usageCondition;
    private String stationCode;
    private String remark;
    private BigDecimal oldQty;
    private BigDecimal newQty;
    private Integer findNo;

    @TableField(exist = false)
    private String parentPartNo;

    @TableField(exist = false)
    private String oldChildPartNo;

    @TableField(exist = false)
    private String newChildPartNo;
}
