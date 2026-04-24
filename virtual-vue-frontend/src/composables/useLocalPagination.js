import { computed, ref, watch } from 'vue'

export function useLocalPagination(items, options = {}) {
  const currentPage = ref(options.currentPage || 1)
  const pageSize = ref(options.pageSize || 10)
  const pageSizes = options.pageSizes || [10, 20, 50, 100]

  const total = computed(() => items.value.length)
  const pagedItems = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    return items.value.slice(start, start + pageSize.value)
  })

  const resetPage = () => {
    currentPage.value = 1
  }

  watch(total, (value) => {
    const maxPage = Math.max(1, Math.ceil(value / pageSize.value))
    if (currentPage.value > maxPage) {
      currentPage.value = maxPage
    }
  })

  watch(pageSize, () => {
    currentPage.value = 1
  })

  return {
    currentPage,
    pageSize,
    pageSizes,
    total,
    pagedItems,
    resetPage
  }
}
