package com.ittry.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ittry.entity.TryProcurementPlan;
import com.ittry.entity.TryProcurementDetail;
import com.ittry.service.TryProcurementPlanService;
import com.ittry.service.TryProcurementDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/procurement-plans")
public class TryProcurementPlanController {
    @Autowired
    private TryProcurementPlanService planService;
    @Autowired
    private TryProcurementDetailService detailService;

    @GetMapping("/page")
    public Object page(@RequestParam int pageNum, @RequestParam int pageSize, @RequestParam(required = false) String planName, @RequestParam(required = false) String status, @RequestParam(required = false) String createUserId) {
        QueryWrapper<TryProcurementPlan> qw = new QueryWrapper<>();
        if (planName != null && !planName.isEmpty()) qw.like("plan_name", planName);
        if (status != null && !status.isEmpty() && !"不限".equals(status)) qw.eq("status", status);
        if (createUserId != null && !createUserId.isEmpty()) qw.eq("create_user_id", createUserId);
        qw.eq("is_deleted", 0);
        Page<TryProcurementPlan> page = new Page<>(pageNum, pageSize);
        Page<TryProcurementPlan> result = planService.page(page, qw);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("msg", "success");
        resp.put("data", data);
        return resp;
    }

    @GetMapping("/{id}")
    public Object detail(@PathVariable String id) {
        TryProcurementPlan plan = planService.getById(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("msg", "success");
        resp.put("data", plan);
        return resp;
    }

    @PostMapping
    public Object save(@RequestBody TryProcurementPlan plan) {
        if (plan.getPlanName() == null || plan.getPlanName().trim().isEmpty()) {
            throw new IllegalArgumentException("采购计划名称不能为空");
        }
        if (plan.getCompany() == null || plan.getCompany().trim().isEmpty()) {
            plan.setCompany("默认单位");
        }
        if (plan.getDept() == null || plan.getDept().trim().isEmpty()) {
            plan.setDept("默认部门");
        }
        if (plan.getCreator() == null || plan.getCreator().trim().isEmpty()) {
            plan.setCreator("系统用户");
        }
        if (plan.getCreateTime() == null) {
            plan.setCreateTime(new java.util.Date());
        }
        planService.save(plan);
        Map<String, Object> resp = new HashMap<>();
        resp.put("code", 0);
        resp.put("msg", "success");
        return resp;
    }

    @PutMapping
    public Object update(@RequestBody Map<String, Object> param) {
        // 1. 解析主表
        TryProcurementPlan plan = new TryProcurementPlan();
        plan.setId((String) param.get("id"));
        plan.setPlanName((String) param.get("planName"));
        plan.setYear((String) param.get("year"));
        plan.setCompany((String) param.get("company"));
        plan.setDept((String) param.get("dept"));
        plan.setCreator((String) param.get("creator"));
        plan.setAttachment((String) param.get("attachment"));
        plan.setStatus((String) param.get("status"));
        plan.setCreateUserId((String) param.get("createUserId"));
        plan.setCreateDate(param.get("createDate") != null ? new java.util.Date() : null);
        plan.setUpdateUserId((String) param.get("updateUserId"));
        plan.setUpdateDate(param.get("updateDate") != null ? new java.util.Date() : null);
        boolean updated = planService.updateById(plan);
        // 2. 解析明细并保存
        if (param.get("details") instanceof List) {
            List<Map<String, Object>> details = (List<Map<String, Object>>) param.get("details");
            detailService.remove(new QueryWrapper<TryProcurementDetail>().eq("plan_id", plan.getId()));
            for (Map<String, Object> d : details) {
                TryProcurementDetail detail = new TryProcurementDetail();
                detail.setPlanId(plan.getId());
                // 字段映射健壮处理
                detail.setItemName(d.get("name") != null ? d.get("name").toString() : null);
                detail.setCategory(d.get("category") != null ? d.get("category").toString() : null);
                detail.setMethod(d.get("method") != null ? d.get("method").toString() : null);
                if (d.get("estimatedAmount") != null && !"".equals(d.get("estimatedAmount"))) {
                    detail.setEstimate(Double.valueOf(d.get("estimatedAmount").toString()));
                }
                if (d.get("plannedTime") != null && !"".equals(d.get("plannedTime"))) {
                    String dateStr = d.get("plannedTime").toString();
                    if (dateStr.length() > 10) dateStr = dateStr.substring(0, 10);
                    detail.setPlanTime(java.sql.Date.valueOf(dateStr));
                }
                detail.setFundSource(d.get("fundSource") != null ? d.get("fundSource").toString() : null);
                detail.setRemark(d.get("remark") != null ? d.get("remark").toString() : null);
                detailService.save(detail);
            }
        }
        Map<String, Object> resp = new HashMap<>();
        if (updated) {
            resp.put("code", 0);
            resp.put("msg", "success");
        } else {
            resp.put("code", 1);
            resp.put("msg", "更新失败，数据不存在或已被删除");
        }
        return resp;
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable String id) {
        boolean removed = planService.removeById(id);
        Map<String, Object> resp = new HashMap<>();
        if (removed) {
            resp.put("code", 0);
            resp.put("msg", "success");
        } else {
            resp.put("code", 1);
            resp.put("msg", "删除失败，数据不存在或已被删除");
        }
        return resp;
    }

    @PostMapping("/import")
    public Object importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return planService.importExcel(file);
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response, 
                          @RequestParam(required = false) String planName,
                          @RequestParam(required = false) String status) throws IOException {
        planService.exportExcel(response, planName, status);
    }

    @PostMapping("/upload")
    public Object uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "文件为空";
        }
        try {
            // 这里可以添加文件类型校验
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && !originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls")) {
                return "请上传Excel文件";
            }
            
            // 处理文件上传逻辑
            // TODO: 这里需要添加具体的Excel解析和数据处理逻辑
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "文件上传成功");
            result.put("filename", originalFilename);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "文件上传失败：" + e.getMessage();
        }
    }
}
