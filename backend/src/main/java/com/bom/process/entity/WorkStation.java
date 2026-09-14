package com.bom.process.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bom_work_station")
public class WorkStation extends BaseEntity {
    private Long plantId;
    private String lineCode;
    private String stationCode;
    private String stationName;
    private Integer seq;
    private Integer takt;
}
