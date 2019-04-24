package com.letmefold.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author SuccessZhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Buy {
    private Long aid;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 实体id
     */
    private String entityId;

    /**
     * 使用卡的id,不能用卡或者没有用卡,此字段为空
     */
    private String useLeasedCardId;

    /**
     * 购买日期
     */
    private Date date;

}