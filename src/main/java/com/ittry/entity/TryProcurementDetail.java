package com.ittry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;
import com.alibaba.excel.annotation.ExcelProperty;

@Data
@TableName("try_procurement_detail")
public class TryProcurementDetail {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String planId;
    @ExcelProperty("序号")
    private Integer seq;
    @ExcelProperty("采购名称")
    private String itemName;
    @ExcelProperty("采购类别")
    private String category;
    @ExcelProperty("采购方式")
    private String method;
    @ExcelProperty("拟采购估价")
    private Double estimate;
    @ExcelProperty("计划采购时间")
    private Date planTime;
    @ExcelProperty("资金来源")
    private String fundSource;
    @ExcelProperty("备注")
    private String remark;
    private String createUserId;
    private Date createDate;
    private String updateUserId;
    private Date updateDate;
    @TableLogic
    private Integer isDeleted;
}
