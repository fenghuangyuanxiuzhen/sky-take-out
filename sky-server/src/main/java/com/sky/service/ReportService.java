package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * @author 杨楠
 * @version 1.0
 */
public interface ReportService {
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户数量统计
     * @param begin
     * @param end
     * @return
     */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    /**
     * 一定时间段内的订单数据统计
     * @param begin
     * @param end
     * @return
     */

    OrderReportVO OrderStatistics(LocalDate begin, LocalDate end);

    /**
     * 获得一定时间段销量前十的菜品
     * @param begin
     * @param end
     * @return
     */

    SalesTop10ReportVO getSaleTop10(LocalDate begin, LocalDate end);

    /**
     * 导出运营数据库表
     * @param response
     */

    void exportBusinessData(HttpServletResponse response);
}
