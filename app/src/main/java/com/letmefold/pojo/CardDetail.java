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
public class CardDetail {
    /**
     * 发行卡id
     */
    private String id;

    /**
     * 发行人id
     */
    private String userId;

    /**
     * 发行人人名
     */
    private String name;

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
     * 店铺名
     */
    private String sname;

    /**
     * 店铺地点
     */
    private String location;

    /**
     * 经营范围
     */
    private String scope;

    public Card toCard() {
        Card card = new Card();
        card.setIssueVersion(this.getIssueVersion());
        card.setGrade(this.getGrade());
        return card;
    }

}