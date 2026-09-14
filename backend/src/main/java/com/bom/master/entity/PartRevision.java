package com.bom.master.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bom_part_revision")
public class PartRevision extends BaseEntity {
    private Long partId;
    private String revision;
    private String changeNo;
    private String releasedBy;
    private String description;
    private LocalDateTime releasedAt;
}
