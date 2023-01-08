package com.bowei.community.entity;

/**
 * Encapsulate paging information
 */

public class Page {
    // current page
    private int current = 1;
    // maximum page
    private int limit = 10;
    // total information(for calculating the total pages)
    private int rows;
    // check path(Used to reuse pagination links)
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get current page
     * @return
     */
    public int getOffset() {
        // current * limit - limit;
        return (current - 1) * limit;
    }

    /**
     * Get total page
     * @return
     */
    public int getTotal() {
        // rows / limit
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * Get begin page
     * @return
     */
    public  int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * Get end page
     * @return
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }
}
