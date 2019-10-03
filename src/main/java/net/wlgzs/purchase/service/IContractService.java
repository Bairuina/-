package net.wlgzs.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.wlgzs.purchase.entity.Contract;
import net.wlgzs.purchase.util.Result;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 胡亚星
 * @since 2019-09-30
 */
public interface IContractService extends IService<Contract> {
    //添加合同
    Result addContract(Contract contract);
    //查询合同
    Result checkContract(String ddbh);
    //删除合同
    Result delContract(String ddbh);
    //查询所有合同
    Result allContract();
    //更新合同
    Result upDateContract(Contract contract);


    /**
    /**
     * 首先通过本地数据库去查询,不存在则远程查询
     * 通过订单编号查看合同
     * @param ddbh 订单编号
     * @return
     */
    Result queryContract(String ddbh);

    /**
     * 更新合同信息
     * @param ddbh 订单编号
     * @return
     */
    Result updateContract(String ddbh);

}
