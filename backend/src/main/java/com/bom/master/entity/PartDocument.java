package com.bom.master.entity;
import com.baomidou.mybatisplus.annotation.TableName; import com.bom.common.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode;
@Data @EqualsAndHashCode(callSuper=true) @TableName("bom_part_document")
public class PartDocument extends BaseEntity { private Long partId; private String docType,docNo,version,url,remark; }
