package com.letmefold.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author SuccessZhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Belong {
    /**
     * 自增主键
     */
    private Long aid;

    /**
     * 创始人id
     */
    private String userId;

    /**
     * 店铺id
     */
    private String storeId;

}