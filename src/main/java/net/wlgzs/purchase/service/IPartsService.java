package net.wlgzs.purchase.service;

import net.wlgzs.purchase.entity.Parts;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 胡亚星
 * @since 2019-09-28
 */
public interface IPartsService extends IService<Parts> {

    public String getPjByPmbhUrl(String pmbh);

}
