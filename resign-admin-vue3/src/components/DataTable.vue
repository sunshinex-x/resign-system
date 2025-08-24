<template>
  <div class="data-table-container">
    <!-- 表格容器 -->
    <div class="table-wrapper" :style="{ height: tableHeight }">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="data"
        :stripe="stripe"
        :border="border"
        :size="size"
        :max-height="maxHeight"
        :row-key="rowKey"
        :default-expand-all="defaultExpandAll"
        :expand-row-keys="expandRowKeys"
        :tree-props="treeProps"
        :lazy="lazy"
        :load="load"
        :indent="indent"
        :show-header="showHeader"
        :show-summary="showSummary"
        :sum-text="sumText"
        :summary-method="summaryMethod"
        :row-class-name="rowClassName"
        :row-style="rowStyle"
        :cell-class-name="cellClassName"
        :cell-style="cellStyle"
        :header-row-class-name="headerRowClassName"
        :header-row-style="headerRowStyle"
        :header-cell-class-name="headerCellClassName"
        :header-cell-style="headerCellStyle"
        :highlight-current-row="highlightCurrentRow"
        :current-row-key="currentRowKey"
        :empty-text="emptyText"
        :default-sort="defaultSort"
        :tooltip-effect="tooltipEffect"
        :span-method="spanMethod"
        :select-on-indeterminate="selectOnIndeterminate"
        :scrollbar-always-on="true"
        class="custom-table"
        @select="handleSelect"
        @select-all="handleSelectAll"
        @selection-change="handleSelectionChange"
        @cell-mouse-enter="handleCellMouseEnter"
        @cell-mouse-leave="handleCellMouseLeave"
        @cell-click="handleCellClick"
        @cell-dblclick="handleCellDblclick"
        @row-click="handleRowClick"
        @row-contextmenu="handleRowContextmenu"
        @row-dblclick="handleRowDblclick"
        @header-click="handleHeaderClick"
        @header-contextmenu="handleHeaderContextmenu"
        @sort-change="handleSortChange"
        @filter-change="handleFilterChange"
        @current-change="handleTableCurrentChange"
        @header-dragend="handleHeaderDragend"
        @expand-change="handleExpandChange"
      >
        <!-- 动态渲染列 -->
        <template v-for="column in columns" :key="column.prop || column.type">
          <!-- 选择列 -->
          <el-table-column
            v-if="column.type === 'selection'"
            :type="column.type"
            :width="column.width || 55"
            :fixed="column.fixed"
            :selectable="column.selectable"
            :reserve-selection="column.reserveSelection"
          />
          
          <!-- 索引列 -->
          <el-table-column
            v-else-if="column.type === 'index'"
            :type="column.type"
            :label="column.label || '序号'"
            :width="column.width || 60"
            :fixed="column.fixed"
            :index="column.index"
          />
          
          <!-- 展开列 -->
          <el-table-column
            v-else-if="column.type === 'expand'"
            :type="column.type"
            :width="column.width || 50"
            :fixed="column.fixed"
          >
            <template #default="scope">
              <slot name="expand" :row="scope.row" :$index="scope.$index" />
            </template>
          </el-table-column>
          
          <!-- 普通列 -->
          <el-table-column
            v-else
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
            :min-width="column.minWidth || (column.fixed === 'right' ? undefined : 120)"
            :fixed="column.fixed"
            :render-header="column.renderHeader"
            :sortable="column.sortable"
            :sort-method="column.sortMethod"
            :sort-by="column.sortBy"
            :sort-orders="column.sortOrders"
            :resizable="column.resizable !== false"
            :formatter="column.formatter"
            :show-overflow-tooltip="column.showOverflowTooltip !== false"
            :align="column.align || 'left'"
            :header-align="column.headerAlign || column.align || 'left'"
            :class-name="column.className"
            :label-class-name="column.labelClassName"
            :filters="column.filters"
            :filter-placement="column.filterPlacement"
            :filter-multiple="column.filterMultiple"
            :filter-method="column.filterMethod"
            :filtered-value="column.filteredValue"
          >
            <template #default="scope">
              <!-- 自定义插槽 -->
              <slot
                v-if="column.slot"
                :name="column.slot"
                :row="scope.row"
                :column="column"
                :$index="scope.$index"
              />
              <!-- 默认显示 -->
              <span v-else>{{ scope.row[column.prop] }}</span>
            </template>
            
            <!-- 表头插槽 -->
            <template v-if="column.headerSlot" #header="scope">
              <slot
                :name="column.headerSlot"
                :column="scope.column"
                :$index="scope.$index"
              />
            </template>
          </el-table-column>
        </template>
      </el-table>
    </div>
    
    <!-- 分页组件 -->
    <div v-if="showPagination" class="pagination-container">
      <el-pagination
          v-if="showPagination"
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="pageSizes"
          :total="total"
          :layout="paginationLayout"
          :background="paginationBackground"
          :small="paginationSmall"
          :disabled="paginationDisabled"
          :hide-on-single-page="hideOnSinglePage"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          @prev-click="handlePrevClick"
          @next-click="handleNextClick"
        />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

// Props 定义
const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  // 列配置
  columns: {
    type: Array,
    default: () => []
  },
  // 表格加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 表格高度
  tableHeight: {
    type: String,
    default: 'auto'
  },
  // 表格最大高度
  maxHeight: {
    type: [String, Number],
    default: undefined
  },
  // 是否显示斑马纹
  stripe: {
    type: Boolean,
    default: true
  },
  // 是否显示边框
  border: {
    type: Boolean,
    default: false
  },
  // 表格尺寸
  size: {
    type: String,
    default: 'default'
  },
  // 行数据的 Key
  rowKey: {
    type: [String, Function],
    default: 'id'
  },
  // 是否默认展开所有行
  defaultExpandAll: {
    type: Boolean,
    default: false
  },
  // 可以通过该属性设置 Table 目前的展开行
  expandRowKeys: {
    type: Array,
    default: () => []
  },
  // 渲染嵌套数据的配置选项
  treeProps: {
    type: Object,
    default: () => ({ hasChildren: 'hasChildren', children: 'children' })
  },
  // 是否懒加载子节点数据
  lazy: {
    type: Boolean,
    default: false
  },
  // 加载子节点数据的函数
  load: {
    type: Function,
    default: undefined
  },
  // 相邻级节点间的水平缩进
  indent: {
    type: Number,
    default: 16
  },
  // 是否显示表头
  showHeader: {
    type: Boolean,
    default: true
  },
  // 是否在表尾显示合计行
  showSummary: {
    type: Boolean,
    default: false
  },
  // 合计行第一列的文本
  sumText: {
    type: String,
    default: '合计'
  },
  // 自定义的合计计算方法
  summaryMethod: {
    type: Function,
    default: undefined
  },
  // 行的 className 的回调方法
  rowClassName: {
    type: [String, Function],
    default: undefined
  },
  // 行的 style 的回调方法
  rowStyle: {
    type: [Object, Function],
    default: undefined
  },
  // 单元格的 className 的回调方法
  cellClassName: {
    type: [String, Function],
    default: undefined
  },
  // 单元格的 style 的回调方法
  cellStyle: {
    type: [Object, Function],
    default: undefined
  },
  // 表头行的 className 的回调方法
  headerRowClassName: {
    type: [String, Function],
    default: undefined
  },
  // 表头行的 style 的回调方法
  headerRowStyle: {
    type: [Object, Function],
    default: undefined
  },
  // 表头单元格的 className 的回调方法
  headerCellClassName: {
    type: [String, Function],
    default: undefined
  },
  // 表头单元格的 style 的回调方法
  headerCellStyle: {
    type: [Object, Function],
    default: undefined
  },
  // 是否要高亮当前行
  highlightCurrentRow: {
    type: Boolean,
    default: false
  },
  // 当前行的 key
  currentRowKey: {
    type: [String, Number],
    default: undefined
  },
  // 空数据时显示的文本内容
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  // 默认的排序列的 prop 和顺序
  defaultSort: {
    type: Object,
    default: () => ({})
  },
  // tooltip effect 属性
  tooltipEffect: {
    type: String,
    default: 'dark'
  },
  // 合并行或列的计算方法
  spanMethod: {
    type: Function,
    default: undefined
  },
  // 在多选表格中，当仅有部分行被选中时，点击表头的多选框时的行为
  selectOnIndeterminate: {
    type: Boolean,
    default: true
  },
  
  // 分页相关
  showPagination: {
    type: Boolean,
    default: true
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 20
  },
  total: {
    type: Number,
    default: 0
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  paginationBackground: {
    type: Boolean,
    default: true
  },
  paginationSmall: {
    type: Boolean,
    default: false
  },
  paginationDisabled: {
    type: Boolean,
    default: false
  },
  hideOnSinglePage: {
    type: Boolean,
    default: false
  }
})

// Emits 定义
const emit = defineEmits([
  'select',
  'select-all',
  'selection-change',
  'cell-mouse-enter',
  'cell-mouse-leave',
  'cell-click',
  'cell-dblclick',
  'row-click',
  'row-contextmenu',
  'row-dblclick',
  'header-click',
  'header-contextmenu',
  'sort-change',
  'filter-change',
  'table-current-change',
  'header-dragend',
  'expand-change',
  'size-change',
  'current-change',
  'prev-click',
  'next-click'
])

// 表格引用
const tableRef = ref()

// 事件处理函数
const handleSelect = (selection, row) => emit('select', selection, row)
const handleSelectAll = (selection) => emit('select-all', selection)
const handleSelectionChange = (selection) => emit('selection-change', selection)
const handleCellMouseEnter = (row, column, cell, event) => emit('cell-mouse-enter', row, column, cell, event)
const handleCellMouseLeave = (row, column, cell, event) => emit('cell-mouse-leave', row, column, cell, event)
const handleCellClick = (row, column, cell, event) => emit('cell-click', row, column, cell, event)
const handleCellDblclick = (row, column, cell, event) => emit('cell-dblclick', row, column, cell, event)
const handleRowClick = (row, column, event) => emit('row-click', row, column, event)
const handleRowContextmenu = (row, column, event) => emit('row-contextmenu', row, column, event)
const handleRowDblclick = (row, column, event) => emit('row-dblclick', row, column, event)
const handleHeaderClick = (column, event) => emit('header-click', column, event)
const handleHeaderContextmenu = (column, event) => emit('header-contextmenu', column, event)
const handleSortChange = (data) => emit('sort-change', data)
const handleFilterChange = (filters) => emit('filter-change', filters)
const handleTableCurrentChange = (currentRow, oldCurrentRow) => emit('table-current-change', currentRow, oldCurrentRow)
const handleHeaderDragend = (newWidth, oldWidth, column, event) => emit('header-dragend', newWidth, oldWidth, column, event)
const handleExpandChange = (row, expandedRows) => emit('expand-change', row, expandedRows)

// 分页事件处理
const handleSizeChange = (size) => emit('size-change', size)
const handlePageChange = (page) => emit('current-change', page)
const handlePrevClick = (page) => emit('prev-click', page)
const handleNextClick = (page) => emit('next-click', page)

// 暴露表格方法
defineExpose({
  tableRef,
  clearSelection: () => tableRef.value?.clearSelection(),
  getSelectionRows: () => tableRef.value?.getSelectionRows(),
  toggleRowSelection: (row, selected) => tableRef.value?.toggleRowSelection(row, selected),
  toggleAllSelection: () => tableRef.value?.toggleAllSelection(),
  toggleRowExpansion: (row, expanded) => tableRef.value?.toggleRowExpansion(row, expanded),
  setCurrentRow: (row) => tableRef.value?.setCurrentRow(row),
  clearSort: () => tableRef.value?.clearSort(),
  clearFilter: (columnKeys) => tableRef.value?.clearFilter(columnKeys),
  doLayout: () => tableRef.value?.doLayout(),
  sort: (prop, order) => tableRef.value?.sort(prop, order)
})
</script>

<style scoped>
.data-table-container {
  width: 100%;
}

.table-wrapper {
  width: 100%;
  overflow: auto;
}

.custom-table {
  width: 100%;
}

/* 操作列按钮样式优化 */
.custom-table :deep(.el-table__fixed-right) {
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
}

.custom-table :deep(.table-actions) {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.custom-table :deep(.table-actions .el-button) {
  margin: 0;
  padding: 4px 8px;
  font-size: 12px;
  min-width: auto;
}

.custom-table :deep(.table-actions .el-button + .el-button) {
  margin-left: 0;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 16px;
  padding: 16px 0;
}

/* 表格滚动条样式 */
.custom-table :deep(.el-scrollbar__bar) {
  opacity: 1;
}

.custom-table :deep(.el-scrollbar__thumb) {
  background-color: rgba(144, 147, 153, 0.3);
  border-radius: 4px;
}

.custom-table :deep(.el-scrollbar__thumb:hover) {
  background-color: rgba(144, 147, 153, 0.5);
}

/* 表格头部样式 */
.custom-table :deep(.el-table__header-wrapper) {
  background-color: #fafafa;
}

.custom-table :deep(.el-table__header th) {
  background-color: #fafafa;
  color: #606266;
  font-weight: 500;
}

/* 表格行样式 */
.custom-table :deep(.el-table__row:hover > td) {
  background-color: #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .custom-table :deep(.table-actions) {
    flex-direction: column;
    gap: 2px;
  }
  
  .custom-table :deep(.table-actions .el-button) {
    width: 100%;
    justify-content: center;
  }
  
  .pagination-container {
    justify-content: center;
  }
}
</style>