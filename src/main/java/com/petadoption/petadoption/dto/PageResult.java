
package com.petadoption.petadoption.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    /**
     * 当前页的数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 每页显示条数
     */
    private long size;

    /**
     * 当前页码
     */
    private long current;

    /**
     * 总页数
     */
    private long pages;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, long size, long current, long pages) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = pages;
    }

    /**
     * 从 MyBatis-Plus 的 IPage 转换
     * @param page MyBatis-Plus 分页对象
     * @return 分页结果
     */
    public static <T> PageResult<T> from(IPage<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getSize(),
                page.getCurrent(),
                page.getPages()
        );
    }
}