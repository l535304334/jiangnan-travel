import { ref } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 统一操作反馈 composable
 * 提供 loading 状态管理和操作反馈的标准化方法
 *
 * @returns {{ loading: import('vue').Ref<boolean>, withLoading: Function, notifySuccess: Function, notifyError: Function, notifyWarning: Function }}
 */
export function useFeedback() {
  const loading = ref(false)

  /**
   * 在 loading 状态下执行异步操作
   * @param {Function} asyncFn - 异步操作函数
   * @returns {Promise<*>} 操作结果
   */
  const withLoading = async (asyncFn) => {
    if (loading.value) return
    loading.value = true
    try {
      return await asyncFn()
    } finally {
      loading.value = false
    }
  }

  /** 成功提示 */
  const notifySuccess = (msg) => ElMessage.success(msg)

  /** 错误提示 */
  const notifyError = (msg) => ElMessage.error(msg)

  /** 警告提示 */
  const notifyWarning = (msg) => ElMessage.warning(msg)

  return { loading, withLoading, notifySuccess, notifyError, notifyWarning }
}
