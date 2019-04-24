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
public class Value {
    /**
     * 自增主键
     */
    private Long aid;

    /**
     * 附加属性id
     */
    private String attributeId;

    /**
     * 附加属性值
     */
    private String value;

}