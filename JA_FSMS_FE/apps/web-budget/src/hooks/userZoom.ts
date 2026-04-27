import { onMounted, ref, watch } from 'vue';

const zoom = ref<number>(100);
/**
 * 页面缩放功能 Hook
 * 监听偏好设置中的 zoom 值变化，并应用到页面上
 */
export function useZoom(zoomValue: number) {
  /**
   * 应用缩放到页面
   * @param zoomValue - 缩放值，范围 50-200，100为默认大小
   */
  function applyZoom(zoomValue: number) {
    const percentage = zoomValue / 100;
    const root = document.documentElement;

    // 使用 CSS zoom 属性（更好的兼容性）
    root.style.zoom = `${percentage}`;
  }

  /**
   * 设置缩放值
   * @param zoomValue - 缩放值
   */
  function setZoom(zoomValue: number) {
    // 限制缩放范围在 50-200 之间
    zoom.value = zoomValue;
  }

  // 监听 zoom 配置变化
  watch(
    () => zoom.value,
    (newZoom) => {
      applyZoom(newZoom);
    },
    { immediate: true },
  );

  // 组件挂载时应用初始缩放
  onMounted(() => {
    setZoom(zoomValue);
    applyZoom(zoom.value);
  });

  return {
    /** 当前缩放值 */
    zoom: () => zoom.value,
    /** 应用缩放 */
    applyZoom,
    /** 设置缩放值 */
    setZoom,
  };
}
