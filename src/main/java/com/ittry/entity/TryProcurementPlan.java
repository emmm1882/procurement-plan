package com.ittry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("try_procurement_plan")
public class TryProcurementPlan {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("plan_name")
    private String planName;
    private String year;
    private String company;
    private String dept;
    private String creator;
    private Date createTime;
    private String attachment;
    private String status;
    private String createUserId;
    private Date createDate;
    private String updateUserId;
    private Date updateDate;
    @TableLogic
    private Integer isDeleted;
}
