<script setup>
import {reactive, ref} from "vue";
import router from "@/router";
import {logout, post} from "@/net";
import {ElMessage} from "element-plus";
import {Plus, Switch} from "@element-plus/icons-vue";
import CreateSubAccount from "@/component/CreateSubAccount.vue";

const formRef = ref()
const valid = ref(false)
const onValidate = (prop, isValid) => valid.value = isValid

const form = reactive({
  password: '',
  new_password: '',
  new_password_repeat: '',
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.new_password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const rules = {
  password: [
    {required: true, message: '请输入原来的密码', trigger: 'blur'},
  ],
  new_password: [
    {required: true, message: '请输入新的密码', trigger: 'blur'},
    {min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur']}
  ],
  new_password_repeat: [
    {required: true, message: '请重复输入新的密码', trigger: 'blur'},
    {validator: validatePassword, trigger: ['blur', 'change']},
  ],
  email: [
    {required: true, message: '请输入邮件地址', trigger: 'blur'},
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ]
}

function resetPassword() {
  formRef.value.validate(isValid => {
    if (isValid) {
      post('/api/user/reset-password', form, () => {
        ElMessage.success('密码修改成功，即将重新登录!')
        logout(() => router.push('/'))
      })
    }
  })
}

const simpleList = ref([]);

const createAccount = ref(false);

</script>

<template>
  <div style="display: flex;gap: 10px">
    <div style="flex: 50%">
      <!--左边-->
      <div class="info-card">
        <div class="title"><i class="fa-solid fa-lock"></i> 修改密码</div>
        <el-divider style="margin: 10px 0"/>
        <el-form @validate="onValidate" :model="form" :rules="rules"
                 ref="formRef" style="margin: 20px" label-width="100">
          <el-form-item label="当前密码" prop="password">
            <el-input type="password" v-model="form.password"
                      :prefix-icon="Lock" placeholder="当前密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="新密码" prop="new_password">
            <el-input type="password" v-model="form.new_password"
                      :prefix-icon="Lock" placeholder="新密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="重复新密码" prop="new_password_repeat">
            <el-input type="password" v-model="form.new_password_repeat"
                      :prefix-icon="Lock" placeholder="重复新密码" maxlength="16"/>
          </el-form-item>
          <div style="text-align: center">
            <el-button :icon="Switch" @click="resetPassword"
                       type="success" :disabled="!valid">立即重置密码
            </el-button>
          </div>
        </el-form>
      </div>
    </div>
    <!--右边-->
    <div class="info-card" style="flex: 50%">
      <div class="title"><i class="fa-solid fa-users"></i> 子用户管理</div>
      <el-divider style="margin: 10px 0"/>
      <el-empty :image-size="100" description="无任何子账户">
        <el-button :icon="Plus" type="primary" plain @click="createAccount = true">添加子账户</el-button>
      </el-empty>
    </div>
    <el-drawer v-model="createAccount" size="520" :with-header="false">
      <CreateSubAccount/>
    </el-drawer>
  </div>
</template>

<style scoped>
.info-card {
  border-radius: 7px;
  padding: 15px 20px;
  background-color: var(--el-bg-color);
  height: fit-content;

  .title {
    font-size: 18px;
    font-weight: bold;
    color: dodgerblue;
  }
}

.account-card {
  border-radius: 5px;
  background-color: var(--el-bg-color-page);
  padding: 10px;
  display: flex;
  align-items: center;
  text-align: left;
  margin: 10px 0;
}

:deep(.el-drawer) {
  margin: 10px;
  height: calc(100% - 20px);
  border-radius: 10px;
}

:deep(.el-drawer__body) {
  padding: 0;
}
</style>