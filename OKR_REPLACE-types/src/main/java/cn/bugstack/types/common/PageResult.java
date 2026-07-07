package cn.bugstack.types.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页结果
 *
 * @param <T> 列表元素类型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> records;

    /** 符合条件的总记录数 */
    private Long total;

    /** 当前页码（从 1 开始） */
    private Integer page;

    /** 每页大小 */
    private Integer size;

    public static <T> PageResult<T> empty(Integer page, Integer size) {
        return PageResult.<T>builder()
                .records(Collections.emptyList())
                .total(0L)
                .page(page)
                .size(size)
                .build();
    }
}
