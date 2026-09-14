package com.bom.change.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bom_ecn")
public class Ecn extends BaseEntity {
    private String ecnNo;
    private String title;
    private String changeType;
    private String effectiveType;
    private String effectiveVin;
    private String status;
    private Long ecrId;
    private Long bomId;
    private Long implementedBomId;
    private LocalDate effectiveDate;

    @TableField(exist = false)
    private String bomNo;

    @TableField(exist = false)
    private String ecrNo;

    @TableField(exist = false)
    private String implementedBomNo;
}
