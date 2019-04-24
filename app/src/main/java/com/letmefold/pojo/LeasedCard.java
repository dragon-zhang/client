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
public class LeasedCard {
    private String id;

    /**
     * 发行卡id
     */
    private String cardId;

    /**
     * 0表示次数卡,1表示时间卡
     */
    private Integer type;

    /**
     * 可用次数,时间卡的可用次数为-1
     */
    private Integer availableTimes;

    /**
     * 过期时间,次数卡的过期时间为null
     */
    private Date expirationDate;

    /**
     * 租金
     */
    private BigDecimal rent;

    /**
     * 租户id,来源于user表
     */
    private String tenantId;

    /**
     * 租赁时间
     */
    private Date rentalTime;

}