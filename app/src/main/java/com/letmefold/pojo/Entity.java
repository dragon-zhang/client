package com.letmefold.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author SuccessZhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entity {
    /**
     * 自增主键
     */
    private Long aid;

    /**
     * 模型id
     */
    private String moduleId;

    /**
     * 实体id
     */
    private String id;

    /**
     * 生产日期
     */
    private Date produceDate;

    /**
     * 保修到期时间
     */
    private Date warrantyExpirationDate;

    /**
     * 是否保修
     */
    private Boolean guarantee;

    /**
     * 是否被售出,1被售出,0未被售出
     */
    private Boolean selled;

    /**
     * 被售出时的价格,未被售出时该字段为空
     */
    private BigDecimal selledPrice;

}