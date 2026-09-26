package cn.iocoder.yudao.framework.common.result;

/**
 * @program: intellect-callcenter
 * @description: 分页相关
 * @author: huangyx
 * @create: 2024-01-23 13:53
 **/

public class Pager {
    /**
     * 当前页
     */
    private Long currPage;

    /**
     * 分页条数
     */
    private Long pageSize;

    /**
     * 总数
     */
    private Long total;

    private Long pages;

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }

    public Long getCurrPage() {
        return currPage;
    }

    public void setCurrPage(Long currPage) {
        this.currPage = currPage;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
