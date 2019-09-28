package net.wlgzs.purchase.service.impl;

import net.wlgzs.purchase.entity.Product;
import net.wlgzs.purchase.mapper.ProductMapper;
import net.wlgzs.purchase.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 胡亚星
 * @since 2019-09-28
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

}
