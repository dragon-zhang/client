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
public class Store {
    /**
     * 自增主键
     */
    private Long aid;

    /**
     * 店铺id
     */
    private String id;

    /**
     * 店铺大小/m2
     */
    private Double size;

    /**
     * 店铺地点
     */
    private String location;

    /**
     * 店面印象
     */
    private String base64image;

    /**
     * 经营范围
     */
    private String scope;

    /**
     * 注册时间
     */
    private Date createTime;

    /**
     * 1逻辑删除，0不删除
     */
    private Boolean dr;

    /**
     * 店铺名
     */
    private String sname;

}