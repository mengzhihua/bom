package com.bom.bom.entity;
import com.baomidou.mybatisplus.annotation.TableName; import com.bom.common.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode; import java.time.*; 
@Data @EqualsAndHashCode(callSuper=true) @TableName("bom_header")
public class BomHeader extends BaseEntity {private String bomNo,bomType,status,description,releasedBy,sapBomNo; private Long rootPartId,vehicleModelId,plantId,sourceBomId; private Integer version; private LocalDate effectiveFrom,effectiveTo; private LocalDateTime releasedAt;}
