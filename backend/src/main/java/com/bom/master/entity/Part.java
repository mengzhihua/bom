package com.bom.master.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bom_part")
public class Part extends BaseEntity {
    private String partNo;
    private String revision;
    private String partName;
    private String partNameEn;
    private String partType;
    private String category;
    private String uom;
    private String material;
    private String makeBuy;
    private String drawingNo;
    private String drawingRev;
    private String color;
    private String surfaceTreatment;
    private String lifecycle;
    private String sapMaterial;
    private String remark;
    private BigDecimal weightKg;
    private BigDecimal unitCost;
    private Integer supplierId;
    private Integer leadTimeDays;
    private Boolean isPhantom;
    private Boolean safetyPart;
    private Boolean regulatoryPart;
}
