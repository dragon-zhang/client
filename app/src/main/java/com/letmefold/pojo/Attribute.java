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
public class Attribute {
    /**
     * 自增主键
     */
    private Long aid;

    /**
     * 模型id
     */
    private String modelId;

    /**
     * 附加属性id
     */
    private String id;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 字段名称
     */
    private String name;

}