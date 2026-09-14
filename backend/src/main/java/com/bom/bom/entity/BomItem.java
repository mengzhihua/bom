package com.bom.bom.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bom_item")
public class BomItem extends BaseEntity {
    private Long bomId;
    private Long parentPartId;
    private Long childPartId;
    private Integer findNo;
    private Integer operationSeq;
    private Integer alternatePriority;
    private BigDecimal qty;
    private String uom;
    private String usageType;
    private String usageCondition;
    private String stationCode;
    private String alternateGroup;
    private String positionDesc;
    private String remark;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
