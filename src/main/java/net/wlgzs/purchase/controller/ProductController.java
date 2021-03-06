package net.wlgzs.purchase.controller;


import com.Enxi;


import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

import net.sf.json.JSONObject;
import net.wlgzs.purchase.entity.Parts;
import net.wlgzs.purchase.entity.Product;
import net.wlgzs.purchase.entity.ServiceValue;
import net.wlgzs.purchase.mapper.PartsMapper;
import net.wlgzs.purchase.mapper.ProductMapper;
import net.wlgzs.purchase.mapper.ServiceValueMapper;
import net.wlgzs.purchase.service.IPartsService;
import net.wlgzs.purchase.service.IProductService;
import net.wlgzs.purchase.service.IServiceValueService;
import net.wlgzs.purchase.service.impl.OrderDataServiceImpl;
import net.wlgzs.purchase.util.ClientUtil;
import net.wlgzs.purchase.util.ReadProperties;
import net.wlgzs.purchase.util.Result;
import net.wlgzs.purchase.util.ResultCode;
import org.codehaus.xfire.client.Client;
import org.joda.time.DateTime;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import net.wlgzs.purchase.base.BaseController;
import org.springframework.web.servlet.ModelAndView;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.net.URL;
import java.util.List;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 王耀兴
 * @since 2019-09-28
 */
@RestController
@RequestMapping("/product")
public class ProductController extends BaseController {

    @Resource
    private IProductService productService;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private PartsMapper partsMapper;
    @Resource
    private ServiceValueMapper serviceValueMapper;
    @Resource
    private IPartsService partsService;
    @Resource
    private IServiceValueService serviceValueService;

    @Autowired
    private ReadProperties readProperties;

    Logger logger = LoggerFactory.getLogger(OrderDataServiceImpl.class);


    //配件入库
    @Scheduled(cron = "0 0 11 * * ?")
    public void pppp(){
        List<String> pmbh=productMapper.findpmbh();
        System.out.println(pmbh);
        try {
            String username=readProperties.getUsername();
            String pwd=readProperties.getPwd();
            String enPwd1= Enxi.enPwd(username,pwd);
            String url=readProperties.getUrl();
            for (int i = 0; i < pmbh.size(); i++) {
                int nowpage=1;
                int pagesize=1;
                do{
                    String json="{\"username\":\""+username+"\",\"pwd\":\""+enPwd1+"\",\"pmbh\":\""+pmbh.get(i)+"\",\"pageNum\":\""+nowpage+"\",\"pageSize\":\"10\"}";
                    System.out.println(json);
                    JSONObject jsonObject= ClientUtil.getJSONObject(url,readProperties.getFindPjByPmbh(),json);
                    System.out.println(jsonObject);
                    if (jsonObject.getString("resultFlag").equals("Y")&&jsonObject.getString("resultMessage").equals("返回品目配件信息成功")) {
                        String jsonString = jsonObject.getString("accessoryList");
                        System.out.println(jsonString);
                        pagesize=Integer.valueOf(jsonObject.getString("pagecount"));
                        String pmbh1 = jsonObject.getString("pmbh");
                        String pmmc = jsonObject.getString("pmmc");
                        logger.info(jsonString);
                        List<Parts> parts1 = JSON.parseArray(jsonString, Parts.class);
                        for (Parts parts2 : parts1) {
                            parts2.setPmbh(pmbh1);
                            parts2.setPmmc(pmmc);
                            List<Parts> parts = partsMapper.findPartssByPjbh(parts2.getPJBH());
                            if (parts.size() == 0) {
                                partsService.save(parts2);
                            } else {
                                parts2.setPartsId(parts.get(0).getPartsId());
                                partsService.updateById(parts2);
                            }
                        }
                        logger.info(parts1 + "****************************");
                    }
                    nowpage++;
                }while(nowpage<=pagesize);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    //待测试
    //根据品目获取增值服务
    @Scheduled(cron = "0 30 11 * * ?")
    public void getService(){
        List<String> pmbh1=productMapper.findpmbh();
        String username=readProperties.getUsername();
        String pwd=readProperties.getPwd();
        String enPwd1= Enxi.enPwd(username,pwd);
        System.out.println(pmbh1);
        for (String pmbh:pmbh1){
            int pageNum = 1;
            int pageSize = 50;
            int pagecount=1;
            for ( ;pageNum <= pagecount; pageNum++) {
                String jsonstr ="{\"username\":\""+username+"\"," +
                        "\"pwd\":\""+enPwd1+"\"," +
                        "\"pageSize\":\""+pageSize+"\","+
                        "\"pageNum\":\""+pageNum+"\","+
                        "\"pmbh\":\""+pmbh+"\"}";
                JSONObject jsonObject=ClientUtil.getJSONObject(readProperties.getUrl(),readProperties.getFindFwByPmbh(),jsonstr);
                System.out.println(jsonObject.toString());
                if (jsonObject.getString("resultMessage").equals("返回品目增值服务信息成功")&&jsonObject.getString("resultFlag").equals("Y")) {
                    String jsonString = jsonObject.getString("serviceList");
                    pagecount=Integer.valueOf(jsonObject.getString("pagecount"));
                    List<ServiceValue> serviceValues = JSON.parseArray(jsonString, ServiceValue.class);
                    for (ServiceValue serviceValue : serviceValues) {
                        String fwbg=serviceValue.getFWBH();
                        List<ServiceValue> serviceValues1=serviceValueMapper.findServiceValueByFubh(fwbg);
                        if (serviceValues1.size()==0){
                            serviceValue.setPmbh(pmbh);
                            serviceValue.setPmmc(jsonObject.getString("pmmc"));
                            if (serviceValueService.save(serviceValue)){
                                System.out.println("添加新服务");
                            }else {
                                System.out.println("添加失败");
                            }

                        }else{
                            serviceValue.setPmbh(pmbh);
                            serviceValue.setPmmc(jsonObject.getString("pmmc"));
                            serviceValue.setServiceId(serviceValues1.get(0).getServiceId());
                            if(serviceValueService.updateById(serviceValue)){
                                System.out.println("更新服务");
                            }else{
                                System.out.println("更新失败");
                            }

                        }

                    }
                    System.out.println(serviceValues);
                }
            }
        }
    }

    /**
     * 更新商品信息
     * @param StartTime 起始时间，
     * @Param EndTime 终止时间
     */
    @RequestMapping(value = "/update/{StartTime}/{EndTime}",method = RequestMethod.POST)
    @ApiOperation(value = "更新本地库",httpMethod = "POST")
    public Result update(@PathVariable("StartTime")String StartTime,@PathVariable("EndTime")String EndTime){
        String url=readProperties.getUrl();
        String username=readProperties.getUsername();
        String pwd=readProperties.getPwd();
        String enPwd1= Enxi.enPwd(username,pwd);
        String result =null;
        DateTime dateTime=new DateTime();
        //总页数
        int pagecount=1;
        //当前页
        int nowpage=1;
        String sprkJssj=EndTime;
        String sprkkssj=StartTime;
        System.out.println(sprkkssj);
        do {
            try {
                Client client = new Client(new URL(url));
                String jsonStr = "{\n\"username\":\"" + username + "\",\n" +
                        "\"pwd\":\"" + enPwd1 + "\",\n" +
                        "\"sprkkssj\":\"" + sprkkssj + "\",\n" +
                        "\"sprkJssj\":\"" + sprkJssj + "\",\n" +
                        "\"pageNum\":\"" + nowpage + "\",\n" +
                        "\"pageSize\":\"50\"\n}";
                Object[] rets = client.invoke("findSprkandParam", new Object[]{jsonStr});
                result = (String) rets[0];
            } catch (Exception e) {
                return new Result(ResultCode.FAIL,"更新失败");
            }
            JSONObject jso1 = JSONObject.fromObject(result);
            String s = jso1.getString("resultFlag");
            if ("N".equals(s)) {
                System.out.println(jso1.getString("resultMessage"));
            } else {
                pagecount = Integer.valueOf(jso1.getString("pagecount"));
                String jsonString = jso1.getString("spList");
                List<Product> product = JSON.parseArray(jsonString, Product.class);
                for (Product product1 : product) {
                    String Xhbh = product1.getXhbh();
                    List<Product> productList = productMapper.findProductsByXhbh(Xhbh);
                    if (productList.size() == 0) {
                        if (productService.save(product1)) {
                            System.out.println("存入新商品"+product1.getXhmc());
                        }
                    } else {
                        product1.setProductId(productList.get(0).getProductId());
                        if (productMapper.updateById(product1) > 0) {
                            System.out.println(product1.getXhmc()+"更新信息");
                        }
                    }
                }
            }
            System.out.println("当前页"+nowpage+",总页数"+pagecount);
            nowpage++;
        }while(nowpage<=pagecount);
        return new Result(ResultCode.SUCCESS,"成功更新");
    }

    /**
     * 查询页
     * 需要遍历lbmc，pmmc，ppmc
     * 还有全部商品列表
     * @param lbmc 类别名称
     * @param pmmc 品目名称
     * @param ppmc 品牌名称
     * @param nowPage 当前页
     * @param  nr  搜索内容
     */
    @RequestMapping(value = "/{lbmc}/{pmmc}/{ppmc}/{page}",method = RequestMethod.GET)
    @ApiOperation(value = "多条件查询",httpMethod = "GET")
    @ApiImplicitParam(name = "nr",dataType = "内容")
    public ModelAndView findallProduct(HttpServletRequest request,
                                       @PathVariable("lbmc") String lbmc,
                                       @PathVariable("pmmc") String pmmc,
                                       @PathVariable("ppmc") String ppmc,
                                       @RequestParam(name = "nr",defaultValue = "0")String nr,
                                       @PathVariable("page") int nowPage){
        return productService.findallProduct(request,lbmc,pmmc,ppmc,nr,nowPage);
    }


    /**
     * 传入pmbh 返回商品信息
     * @param xhbh 商品编号
     * @return
     */
    @RequestMapping(value = "/product/{xhbh}",method = RequestMethod.GET)
    @ApiOperation(value = "报价商品页",httpMethod = "GET")
    public ModelAndView getProductByXhbh(@PathVariable("xhbh")String xhbh){
        return productService.getProductByXhbh(xhbh);
    }


    /**
     * 更新商品信息（审核，等等）
     * @param xhbh 商品编号
     *
     */
    @RequestMapping(value = "/product/update",method = RequestMethod.GET)
    @ApiOperation(value = "更新一个商品的相关信息",httpMethod = "GET")
    public Result updateProduct(@RequestParam("xhbh")String xhbh){
        String url=readProperties.getUrl();
        String username=readProperties.getUsername();
        String pwd=readProperties.getPwd();
        String enPwd1= Enxi.enPwd(username,pwd);
        String json="{\"username\":\""+username+"\","+
                "\"pwd\":\""+enPwd1+"\","+
                "\"xhbh\":\""+xhbh+"\"}";
        JSONObject jsonObject=ClientUtil.getJSONObject(url,readProperties.getFindSpByXhbh(),json);
//        System.out.println("得到"+jsonObject);
        String message=jsonObject.getString("resultMessage");
        if("N".equals(jsonObject.getString("resultFlag"))){
            return new Result(ResultCode.FAIL,message);
        }else{
            Product product = com.alibaba.fastjson.JSONObject.parseObject(String.valueOf(jsonObject),Product.class);
            Product product1 = productMapper.findProductByXhbh(xhbh);
            product.setProductId(product1.getProductId());
            product.setPrice(product1.getPrice());
            product.setShjg(product1.getShjg());
            if (product.equals(product1)){
                return new Result(ResultCode.SUCCESS,"无变更");
            }else{
                if(productMapper.updateById(product)>0){
                    return new Result(ResultCode.SUCCESS,"更新成功");
                }
                return new Result(ResultCode.FAIL,"更新失败,稍后重试");
            }
        }
    }
}
