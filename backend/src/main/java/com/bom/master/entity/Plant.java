package com.bom.master.entity;
import com.baomidou.mybatisplus.annotation.TableName; import com.bom.common.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode;
@Data @EqualsAndHashCode(callSuper=true) @TableName("bom_plant")
public class Plant extends BaseEntity { private String plantCode,plantName,sapPlant,address; }
