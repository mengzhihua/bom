package com.bom.change.entity;
import com.baomidou.mybatisplus.annotation.TableName; import com.bom.common.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode; import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper=true) @TableName("bom_ecr") public class Ecr extends BaseEntity {private String ecrNo,title,reason,priority,affectedPartIds,description,status,requester,approvedBy,rejectReason;private Long vehicleModelId;private LocalDateTime approvedAt;}
