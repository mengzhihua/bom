package com.bom.change.entity;
import com.baomidou.mybatisplus.annotation.TableName; import com.bom.common.BaseEntity; import lombok.Data; import lombok.EqualsAndHashCode; import java.time.LocalDate;
@Data @EqualsAndHashCode(callSuper=true) @TableName("bom_ecn") public class Ecn extends BaseEntity {private String ecnNo,title,changeType,effectiveType,effectiveVin,status;private Long ecrId,bomId,implementedBomId;private LocalDate effectiveDate;}
