package net.wlgzs.purchase.service;

import com.sun.org.apache.xpath.internal.operations.Mod;
import net.wlgzs.purchase.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 胡亚星
 * @since 2019-09-28
 */

public interface IProductService extends IService<Product> {

    /**
     * 查询页
     * 需要遍历lbmc，pmmc，ppmc
     * 还有全部商品列表
     */
    public ModelAndView findallProduct(String lbbh,String pmbh,String ppbh,String nr);

    public ModelAndView getProductByXhbh(String xhbh);
}
