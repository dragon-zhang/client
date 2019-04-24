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
public class Card {
    /**
     * 自增主键
     */
    private Long aid;

    /**
     * 发行卡id
     */
    private String id;

    /**
     * 发行人id
     */
    private String userId;

    /**
     * 发行时间
     */
    private Date issueTime;

    /**
     * 发行版本
     */
    private String issueVersion;

    /**
     * 发行卡等级，如:金,银,铜
     */
    private String grade;

    /**
     * 1逻辑删除，0不删除
     */
    private Boolean dr;
}