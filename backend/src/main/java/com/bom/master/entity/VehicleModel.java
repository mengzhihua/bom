package com.bom.master.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
@Data @EqualsAndHashCode(callSuper=true) @TableName("bom_vehicle_model")
public class VehicleModel extends BaseEntity { private String modelCode,modelName,platform,program,status; private LocalDate sopDate; }
