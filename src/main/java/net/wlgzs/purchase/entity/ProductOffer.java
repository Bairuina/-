package net.wlgzs.purchase.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author 胡亚星
 * @since 2019-10-07
 */
@TableName("product_offer")
public class ProductOffer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品报价Id
     */
    @TableId(value = "product_offer_id", type = IdType.AUTO)
    private Integer productOfferId;

    /**
     * 商品Id
     */
    private String xhbh;

    /**
     * 商品报价
     */
    private BigDecimal price;

    /**
     * 1-上架  2-下架 3-审核中
     */
    private String zt;

    /**
     *
     * 审核中的价格
     */
    private BigDecimal shjg;

    private String fwcn;   //服务承诺

    private String productlink;  //商品在自有网站的的绝对地址

    private String area;

    /**
     *商品名称
     */
    private String xhmc;

    private String lbmc;

    private String pmmc;

    private String ppmc;

    public String getXhmc() {
        return xhmc;
    }

    public void setXhmc(String xhmc) {
        this.xhmc = xhmc;
    }

    public String getLbmc() {
        return lbmc;
    }

    public void setLbmc(String lbmc) {
        this.lbmc = lbmc;
    }

    public String getPmmc() {
        return pmmc;
    }

    public void setPmmc(String pmmc) {
        this.pmmc = pmmc;
    }

    public String getPpmc() {
        return ppmc;
    }

    public void setPpmc(String ppmc) {
        this.ppmc = ppmc;
    }

    public ProductOffer() {
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getFwcn() {
        return fwcn;
    }

    public void setFwcn(String fwcn) {
        this.fwcn = fwcn;
    }

    public String getProductlink() {
        return productlink;
    }

    public void setProductlink(String productlink) {
        this.productlink = productlink;
    }

    public BigDecimal getShjg() {
        return shjg;
    }

    public void setShjg(BigDecimal shjg) {
        this.shjg = shjg;
    }



    public Integer getProductOfferId() {
        return productOfferId;
    }

    public void setProductOfferId(Integer productOfferId) {
        this.productOfferId = productOfferId;
    }
    public String getXhbh() {
        return xhbh;
    }

    public void setXhbh(String xhbh) {
        this.xhbh = xhbh;
    }
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public String getZt() {
        return zt;
    }

    public void setZt(String zt) {
        this.zt = zt;
    }

    @Override
    public String toString() {
        return "ProductOffer{" +
        "productOfferId=" + productOfferId +
        ", xhbh=" + xhbh +
        ", price=" + price +
        ", zt=" + zt +
        "}";
    }
}
