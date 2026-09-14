package com.bom.bom.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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

    @TableField(exist = false)
    private String rootPartNo;

    @TableField(exist = false)
    private String rootPartName;

    @TableField(exist = false)
    private String vehicleModelCode;

    @TableField(exist = false)
    private String vehicleModelName;

    @TableField(exist = false)
    private String plantCode;

    @TableField(exist = false)
    private String plantName;

    @TableField(exist = false)
    private String sourceBomNo;
}
