package net.wlgzs.purchase.mapper;

import net.wlgzs.purchase.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 王耀兴
 * @since 2019-09-28
 */
@org.apache.ibatis.annotations.Mapper
public interface ProductMapper extends BaseMapper<Product> {
    /**
     * 查询部分字段lbmc
     *
     */
    @Select("SELECT lbbh,lbmc FROM product")
    public Set<Product> findlbmc();

    /**
     * 查询部分字段pmmc
     *
     */
    @Select("SELECT pmmc FROM product")
    public Set<String> findpmmc();

    /**
     * 查询部分字段ppmc
     *
     */
    @Select("SELECT ppmc FORM product")
    public Set<String> findppmc();

    /**
     * 返回全部品目编号pmbh
     *
     */
    @Select("SELECT distinct pmbh FROM `product`")
    public List<String> findpmbh();
    /**
     * 返回全部型号编号xhbh
     */
    @Select("SELECT distinct xhbh FROM `product`")
    public List<String> findxhbh();

    /**
     * 根据型号xhbh获取商品
     */
    @Select("SELECT * FROM `product` where xhbh=#{xhbh}")
    public Product findProductByXhbh(String xhbh);

    /**
     * 根据品目编号pmbh获取商品编号xhbh
     */
    @Select("SELECT distinct xhbh FROM `product` where pmbh=#{pmbh}")
    public List<String> findXhbhByPmbh(String pmbh);

    /**
     * 获取商品名称根据商品编号
     */
    @Select("SElECT xhmc FROM `product` where xhbh=#{xhbh}")
    public String findXhmcByXhbh(String xhbh);
    /**
     * 根据xhbh获取商品集合 长度1或者0
     */
    @Select("SELECT * FROM `product` where xhbh=#{xhbh}")
    public List<Product> findProductsByXhbh(String xhbh);
    /**
     * 查询品牌名称根据品牌编号ppbh
     */
    @Select("SELECT distinct ppmc FROM `product` where ppbh=#{ppbh}")
    public String findPpmcByPpbh(String ppbh);
    /**
     * 获取品目名称根据品目编号pmbh
     */
    @Select("SELECT distinct pmmc FROM `product` where pmbh=#{pmbh}")
    public String findPmmcByPmbh(String pmbh);
    /**
     * 获取类别名称根据类别编号lbbh
     */
    @Select("SELECT distinct lbmc FROM `product` where lbbh=#{lbbh}")
    public String findLbmcByLbbh(String lbbh);

    /**
     * 根据类别名称获取该类别下所有品目名称
     */
    @Select("Select distinct pmmc FROM 'product' where lbmc=#{lbmc}")
    public List<String> findPmmcByLbmc(String lbmc);

    /**
     * 根据品目名称获取其下品牌
     */
    @Select("Select distinct ppmc FROM 'product' where pmmc=#{pmmc}")
    public List<String> findPpmcByPmmc(String pmmc);
}
