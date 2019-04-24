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
public class Have {
    /**
     * 自增主键
     */
    private Long aid;

    /**
     * 店铺id
     */
    private String storeId;

    /**
     * 实例id
     */
    private String entityId;

}