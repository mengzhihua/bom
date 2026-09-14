package com.bom.master.entity;
import com.baomidou.mybatisplus.annotation.TableName; import com.bom.common.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode; import java.math.BigDecimal;
@Data @EqualsAndHashCode(callSuper=true) @TableName("bom_part")
public class Part extends BaseEntity { private String partNo,revision,partName,partNameEn,partType,category,uom,material,makeBuy,drawingNo,drawingRev,color,surfaceTreatment,lifecycle,sapMaterial,remark; private BigDecimal weightKg,unitCost; private Integer supplierId,leadTimeDays; private Boolean isPhantom,safetyPart,regulatoryPart; }
