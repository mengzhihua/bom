package com.bom.bom.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.*;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bom_header")
public class BomHeader extends BaseEntity {
    private String bomNo;
    private String bomType;
    private String status;
    private String description;
    private String releasedBy;
    private String sapBomNo;
    private Long rootPartId;
    private Long vehicleModelId;
    private Long plantId;
    private Long sourceBomId;
    private Integer version;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private LocalDateTime releasedAt;
}
