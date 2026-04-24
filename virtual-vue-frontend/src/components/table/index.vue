<template>
  <div class="system-table-box">
    <el-table
      ref="tableRef"
      v-bind="$attrs"
      class="system-table"
      border
      :data="data"
      @selection-change="handleSelectionChange"
    >
      <el-table-column v-if="showSelection" type="selection" align="center" width="50" />
      <el-table-column v-if="showIndex" label="序号" width="64" align="center">
        <template #default="scope">
          {{ (page.index - 1) * page.size + scope.$index + 1 }}
        </template>
      </el-table-column>
      <slot />
    </el-table>

    <div v-if="showPage" class="system-table-pagination">
      <el-pagination
        v-model:current-page="page.index"
        background
        :layout="pageLayout"
        :total="page.total"
        :page-size="page.size"
        :page-sizes="pageSizes"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onActivated, ref } from 'vue'

export default defineComponent({
  name: 'SystemTable',
  props: {
    data: { type: Array, default: () => [] },
    showIndex: { type: Boolean, default: false },
    showSelection: { type: Boolean, default: false },
    showPage: { type: Boolean, default: true },
    page: {
      type: Object,
      default: () => ({ index: 1, size: 20, total: 0 })
    },
    pageLayout: {
      type: String,
      default: 'total, sizes, prev, pager, next, jumper'
    },
    pageSizes: {
      type: Array,
      default: () => [10, 20, 50, 100]
    }
  },
  emits: ['getTableData', 'selection-change'],
  setup(props, { emit }) {
    const tableRef = ref()
    let sizeChangeLock: string | null = null

    const handleCurrentChange = (value: number) => {
      if (sizeChangeLock) {
        props.page.index = 1
        return
      }
      props.page.index = value
      emit('getTableData')
    }

    const handleSizeChange = (value: number) => {
      sizeChangeLock = 'active'
      window.setTimeout(() => {
        sizeChangeLock = null
      }, 100)
      props.page.size = value
      emit('getTableData', true)
    }

    const handleSelectionChange = (value: unknown[]) => {
      emit('selection-change', value)
    }

    onActivated(() => {
      tableRef.value?.doLayout?.()
    })

    return {
      tableRef,
      handleCurrentChange,
      handleSizeChange,
      handleSelectionChange
    }
  }
})
</script>

<style lang="scss" scoped>
.system-table-box {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.system-table {
  width: 100%;
}

.system-table-pagination {
  display: flex;
  justify-content: flex-end;
}
</style>
