package net.wlgzs.purchase.service;


import com.baomidou.mybatisplus.extension.service.IService;
import net.wlgzs.purchase.entity.OrderData;
import net.wlgzs.purchase.util.Result;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 胡亚星
 * @since 2019-10-03
 */
public interface IOrderDataService extends IService<OrderData> {
    //定时更新接口
    Result updateOrderDate(Integer pageNum,String kssjData);

    //查询订单
    Result selectAllOrder(Integer pageSize,Integer pageNum);

    //查看单个订单详情
    Result selectOneOrder(String ddbh);

    //确认或取消订单
    Result ensureORefuseOrder(String ddbh,Integer qrzt);

    //订单签收物流信息推送
    Result ensureOrderArrive(String ddbh,Integer sfcd,String fczddbh,String kdgs,String kddh,String ms,BigInteger kdsj);

    //订单签收时间信息推送
    Result ensureOrderTimeSubmit(String ddbh,Integer sfcd,String fczddbh,BigInteger shsj);

    //订单发票开具时间信息推送
    Result invoiceStaTimeSubmit(String ddbh, BigInteger fpkjsj);

    //订单发票收到开具信息推送
    Result invoiceEndTimeSubmit(String ddbh,BigInteger fpsdsj);

    //电商已经收到采购单位的付款,将收款标志、收款金额、收款时间提交
    Result getMoneyDataSubmit(String ddbh, String bz,Integer skbz, BigDecimal skje, BigInteger sksj);

    //取消订单(已经对进行订单确认)
    Result deletEnsureOrder(String ddbh,String qxyy);

    //查看采购单位对当前订单的验收情况(可以不存入数据库，只展示)
    Result checkOrderStatus(String ddbh);

    //订单的合同处理
    Result contractWork(String ddbh);

    //查询不同状态的订单  2-供应商待确认3-待验收 4-订单已取消5-验收通过
    Result selectStatusDataOrder(Integer pageNum,Integer PageSize,String status);

    //根据条件查询订单信息
    Result selectOrderListByData(String data, String zt, Integer pageSize, Integer pageNum, Map<String,String> mapData);

    //删除已经取消的订单
    Result delOrderByData(String ddbh);

    //批量删除订单编号
    Result delOrdersByData(String[] ddbh);

    //更新
    Result upDataOneData(String ddbh);

    //查看验收单
    Result checkShowPage(String ddbh);

}
