<template>
  <div class="plan-add">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>新增采购计划</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="goBack">返回</el-button>
      </div>
      
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="计划名称" prop="planName">
          <el-input v-model="form.planName" placeholder="请输入计划名称"></el-input>
        </el-form-item>
        
        <el-form-item label="所属年度" prop="year">
          <el-date-picker
            v-model="form.year"
            type="year"
            placeholder="选择年份"
            value-format="yyyy">
          </el-date-picker>
        </el-form-item>
        
        <el-form-item label="附件">
          <el-upload
            class="upload-demo"
            action="/api/attachment/upload"
            :on-success="handleUploadSuccess"
            :file-list="fileList"
            :limit="5"
            :on-exceed="handleExceed"
            :before-upload="beforeUpload">
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip">只能上传jpg/png/pdf文件，且不超过100MB，最多5个</div>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="计划明细">
          <div class="detail-toolbar">
            <el-button type="primary" size="small" @click="handleAddDetail">添加明细</el-button>
            <el-button type="success" size="small" @click="showImportDialog = true">导入明细</el-button>
            <el-button type="warning" size="small" @click="handleExport">导出明细</el-button>
          </div>
          
          <el-table :data="form.details" border style="width: 100%">
            <el-table-column type="index" width="50"></el-table-column>
            <el-table-column label="采购名称" prop="name">
              <template slot-scope="scope">
                <el-input v-model="scope.row.name" placeholder="请输入采购名称"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="采购类别" prop="category">
              <template slot-scope="scope">
                <el-select v-model="scope.row.category" placeholder="请选择">
                  <el-option label="货物" value="GOODS"></el-option>
                  <el-option label="服务" value="SERVICE"></el-option>
                  <el-option label="工程" value="PROJECT"></el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="采购方式" prop="method">
              <template slot-scope="scope">
                <el-select v-model="scope.row.method" placeholder="请选择">
                  <el-option label="公开招标" value="OPEN"></el-option>
                  <el-option label="邀请招标" value="INVITE"></el-option>
                  <el-option label="竞争性谈判" value="NEGOTIATION"></el-option>
                  <el-option label="询价" value="INQUIRY"></el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="拟采购估价" prop="estimatedAmount">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.estimatedAmount" :min="0"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="计划采购时间" prop="plannedTime">
              <template slot-scope="scope">
                <el-date-picker
                  v-model="scope.row.plannedTime"
                  type="date"
                  placeholder="选择日期"
                  value-format="yyyy-MM-dd">
                </el-date-picker>
              </template>
            </el-table-column>
            <el-table-column label="资金来源" prop="fundSource">
              <template slot-scope="scope">
                <el-select v-model="scope.row.fundSource" placeholder="请选择">
                  <el-option label="财政资金" value="FISCAL"></el-option>
                  <el-option label="自筹资金" value="SELF"></el-option>
                  <el-option label="其他" value="OTHER"></el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="备注" prop="remark">
              <template slot-scope="scope">
                <el-input v-model="scope.row.remark" placeholder="请输入备注"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  type="danger"
                  @click="handleDeleteDetail(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <import-dialog
      v-if="showImportDialog"
      :value="showImportDialog"
      :import-url="`/api/excel/import/${form.id}`"
      @close="showImportDialog = false"
      @imported="onImportDetail"
    />
  </div>
</template>

<script>
import { createPlan, importPlanDetails, exportPlanDetails } from '@/api/plan'
import ImportDialog from './ImportDialog.vue'

export default {
  name: 'PlanAdd',
  components: { ImportDialog },
  data() {
    return {
      form: {
        planName: '',
        year: '',
        attachment: '',
        details: []
      },
      rules: {
        planName: [
          { required: true, message: '请输入计划名称', trigger: 'blur' }
        ],
        year: [
          { required: true, message: '请选择所属年度', trigger: 'change' }
        ]
      },
      fileList: [],
      uploadUrl: process.env.VUE_APP_BASE_API + '/procurement-plans/attachments',
      uploadHeaders: {
        // 如果需要认证token
        // Authorization: 'Bearer ' + getToken()
      },
      showImportDialog: false
    }
  },
  methods: {
    handleAddDetail() {
      this.form.details.push({
        name: '',
        category: '',
        method: '',
        estimatedAmount: 0,
        plannedTime: '',
        fundSource: '',
        remark: ''
      })
    },
    handleDeleteDetail(index) {
      this.form.details.splice(index, 1)
    },
    onImportDetail() {
      // 导入成功后刷新明细
      this.getDetail()
    },
    handleExport() {
      const id = this.form.id
      if (!id) {
        this.$message.error('请先保存主表后再导出明细')
        return
      }
      exportPlanDetails(id).then(res => {
        const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = '计划明细导出.xlsx'
        a.click()
        window.URL.revokeObjectURL(url)
      })
    },
    handleUploadSuccess(response, file, fileList) {
      // response: {originName, filename}
      this.form.attachment = fileList
        .map(f => {
          const r = f.response || {};
          return r.originName && r.filename ? `${r.originName}:${r.filename}` : '';
        })
        .filter(Boolean)
        .join(',');
      this.$message.success('上传成功')
    },
    handleUploadError() {
      this.$message.error('上传失败')
    },
    handleExceed(files, fileList) {
      this.$message.warning('最多只能上传5个文件')
    },
    beforeUpload(file) {
      const isLt100M = file.size / 1024 / 1024 < 100
      if (!isLt100M) {
        this.$message.error('单个文件不能超过100M')
      }
      return isLt100M
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          createPlan(this.form).then(() => {
            this.$message.success('保存成功')
            this.goBack()
          })
        }
      })
    },
    goBack() {
      this.$router.push('/plan/list')
    }
  }
}
</script>

<style lang="scss" scoped>
.plan-add {
  padding: 20px;
}
.box-card {
  margin-bottom: 20px;
}
.detail-toolbar {
  margin-bottom: 20px;
}
.el-input__inner {
  text-align: center;
}
</style>