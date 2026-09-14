package com.bom.bom.entity;
import com.baomidou.mybatisplus.annotation.TableName; import com.bom.common.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode; import java.math.BigDecimal; import java.time.LocalDate;
@Data @EqualsAndHashCode(callSuper=true) @TableName("bom_item")
public class BomItem extends BaseEntity {private Long bomId,parentPartId,childPartId; private Integer findNo,operationSeq,alternatePriority; private BigDecimal qty; private String uom,usageType,usageCondition,stationCode,alternateGroup,positionDesc,remark; private LocalDate effectiveFrom,effectiveTo;}
