package com.bom.change.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bom_ecr")
public class Ecr extends BaseEntity {
    private String ecrNo;
    private String title;
    private String reason;
    private String priority;
    private String affectedPartIds;
    private String description;
    private String status;
    private String requester;
    private String approvedBy;
    private String rejectReason;
    private Long vehicleModelId;
    private LocalDateTime approvedAt;
}
