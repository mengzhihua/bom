package com.bom.master.entity;
import com.baomidou.mybatisplus.annotation.TableName; import com.bom.common.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode;
@Data @EqualsAndHashCode(callSuper=true) @TableName("bom_supplier")
public class Supplier extends BaseEntity { private String supplierCode,name,sapVendor,srmCode,status; }
