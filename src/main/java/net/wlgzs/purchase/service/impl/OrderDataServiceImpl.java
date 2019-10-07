package net.wlgzs.purchase.service.impl;

import com.Enxi;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.wlgzs.purchase.entity.*;
import net.wlgzs.purchase.mapper.OrderDataMapper;
import net.wlgzs.purchase.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.wlgzs.purchase.util.*;
import org.codehaus.xfire.client.Client;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 胡亚星
 * @since 2019-10-03
 */
@Service
public class OrderDataServiceImpl extends ServiceImpl<OrderDataMapper, OrderData> implements IOrderDataService {

    IRedis iRedis;


    @Autowired
    IContractService contractService;

    @Autowired
    IAccessoryListService iAccessoryListService;

    @Autowired
    IProductListService iProductListService;

    @Autowired
    IServiceListService iServiceListService;

    @Autowired
    ReadProperties properties;

    @Autowired
    private WebSocketServer webSocketServer;


    Logger logger = LoggerFactory.getLogger(OrderDataServiceImpl.class);


    //更新订单
    @Override
    public Result updateOrderDate(int pageNum) {
        String userName=properties.getUsername();
        String pwd=properties.getPwd();
        DateTime dateTime=new DateTime();
        String enPwd = Enxi.enPwd(userName, pwd);
        String jssj =dateTime.toString("yyyyMMddHHmmss") ;
        setUpDataTime(dateTime.toString("yyyyMMddHHmmss"));
        String kssj = checkUpDataTime();
        logger.info("kssj(开始时间):" + kssj);
        logger.info("jssj(结束时间):" + jssj);
        int pageSize = 50;
        String result = null;
        try {
            Client client = new Client(new URL("http://222.143.21.205:8091/wsscservices_test/services/wsscWebService?wsdl"));
            String jsonStr;
            jsonStr = "{\n" +
                    "\"username\":\""+userName+"\",\n" +
                    "\"pwd\":\"" + enPwd + "\",\n" +
                    "\"kssj\":\""+kssj+"\",\n" +
                    "\"jssj\":\""+jssj+"\",\n" +
                    "\"pageNum\":\""+pageNum+"\",\n" +
                    "\"pageSize\":\"50\"\n}";
            logger.info(jsonStr);
            Object[] rets = client.invoke("findOrder", new Object[]{jsonStr});
            result = rets[0].toString();
            logger.info(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSONObject.fromObject(result);
        String flag = jsonObject.get("resultFlag").toString();
        if("N".equals(flag)){
            logger.info("没有订单信息！");
            webSocketServer.sendMessage("此次订单更新失败！");
            return new Result(ResultCode.FAIL);
        }
        int count = Integer.parseInt(jsonObject.get("count").toString());
        List<OrderData> orderList = null;
        List<ProductList> productLists;
        List<AccessoryList> accessoryLists;
        List<ServiceList> serviceLists;
        if(jsonObject.get("orderList")==null||jsonObject.get("orderList").equals("")){
            logger.info("没有更新内容!");
            webSocketServer.sendMessage("此次订单更新，没有可需更新内容。");
            return new Result(ResultCode.FAIL);
        }
        JSONArray orderListData = jsonObject.getJSONArray("orderList");
        List<OrderData> orderDataList=JSON.parseArray(orderListData.toString(),OrderData.class);
        logger.info(orderDataList.toString()+"\n\n");
        for(OrderData orderData:orderDataList){
            if(!checkddbh(orderData.getDdbh())){
                logger.info("该订单已存在！");
                return new Result(ResultCode.FAIL);
            }
            productLists=orderData.getProductList();
            accessoryLists=orderData.getAccessoryList();
            serviceLists=orderData.getServiceList();
           if(productLists!=null){
               for(ProductList productList:productLists){
                productList.setDdbh(orderData.getDdbh());
                logger.info("ProductList:"+productList.toString());
                if(iProductListService.save(productList)){
                    logger.info("添加成功！\n\n");
                }
               }
           }
           if(accessoryLists!=null){
               for(AccessoryList accessoryList:accessoryLists){
                    accessoryList.setDdbh(orderData.getDdbh());
                   logger.info("AccessoryList:"+accessoryList.toString());
                    if(iAccessoryListService.save(accessoryList)){
                        logger.info("添加成功！\n\n");
                   }
               }
           }
           if(serviceLists!=null){
               for(ServiceList serviceList:serviceLists){
                    serviceList.setDdbh(orderData.getDdbh());
                   logger.info("ServiceList:"+serviceList.toString());
                    if(iServiceListService.save(serviceList)){
                        logger.info("添加成功！\n\n");
                    }
               }
           }
        }

        int totalPageNum = (count  + 14) / 15;
        if(totalPageNum>pageNum){
            pageNum++;
            updateOrderDate(pageNum);
        }
            logger.info("更新完成！");
            webSocketServer.sendMessage("订单信息更新完成");
            return new Result(ResultCode.SUCCESS);

    }


    //查询所有订单
    @Override
    public Result selectAllOrder(Integer pageSize,Integer pageNum) {
        QueryWrapper<OrderData> queryWrapper=new QueryWrapper<>();
        Page page = new Page(pageNum, 3);
        IPage<OrderData> iPage=null;
        iPage = baseMapper.selectPage(page,queryWrapper);
        List orderList = iPage.getRecords();
        logger.info("\n\n总页数为："+iPage.getPages()+"当前页数为："+iPage.getCurrent()+"\n");
        return new Result(ResultCode.SUCCESS, "成功！", orderList, iPage.getPages(), iPage.getCurrent());
    }



    //查看订单详情
    @Override
    public Result selectOneOrder(String ddbh) {
        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ddbh", ddbh);
        OrderData orderData = baseMapper.selectOne(queryWrapper);
        if(orderData==null){
            logger.info("没有该订单详情");
            return new Result(ResultCode.FAIL);
        }
        String ddbhData=orderData.getDdbh();
        List<ProductList> productLists;
        List<AccessoryList> accessoryLists;
        List<ServiceList> serviceLists;
        productLists=iProductListService.selectProductList(ddbhData);
        serviceLists=iServiceListService.selectServiceList(ddbhData);
        accessoryLists=iAccessoryListService.selectAccessoryList(ddbhData);
        List<Object> list=new ArrayList<>();
        list.add(orderData);
        list.add(productLists);
        list.add(accessoryLists);
        list.add(serviceLists);
        list.add(CheckAddress.checkOneAddressWordByNumber(orderData.getDeliverycity()));
        list.add(CheckAddress.checkOneAddressWordByNumber(orderData.getDeliverycounty()));
        return new Result(ResultCode.SUCCESS,list);
    }


    //确定或拒绝订单
    @Override
    public Result ensureORefuseOrder(String ddbh, int qrzt) {
        logger.info("ddbh："+ddbh+" qrzt："+qrzt);
        if(!"2".equals(checkZt(ddbh))){
            logger.info("该订单状态存在问题！");
            return new Result(ResultCode.FAIL);
        }
        String username=properties.getUsername();
        String pwd=properties.getPwd();
        String enPwd = Enxi.enPwd(username, pwd);
        String jsonStr="{\n" +
                "\"username\": \""+username+"\", \n" +
                "\"pwd\": \""+enPwd+"\", \n" +
                "\"ddbh\": \""+ddbh+"\"，\n" +
                "\"qrzt\": "+qrzt+"\n" +
                "}\n";
        try {
            Client client=new Client(new URL("http://222.143.21.205:8091/wsscservices_test/services/wsscWebService?wsdl"));
            Object[] rets=client.invoke("execGysOrderQr",new Object[]{jsonStr});
            String result=rets[0].toString();
            logger.info(result);
            JSONObject jsonObject=JSONObject.fromObject(result);
            if(jsonObject.get("resultFlag")!=null&&jsonObject.get("resultFlag").equals("Y")){
                String ddbhData=jsonObject.get("ddbh").toString();
                OrderData orderData=new OrderData();
                if(qrzt==1) {
                    orderData.setZt("4");
                }
                else if(qrzt==0){
                    orderData.setZt("3");
                }
                return  upDateTwo(ddbh,orderData);
            }
            else {
                logger.info("操作订单失败！");
                return new Result(ResultCode.FAIL);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("操作订单失败！");
        return new Result(ResultCode.FAIL);
    }




    //订单签收物流信息推送
    @Override
    public Result ensureOrderArrive(String ddbh, int sfcd, String fczddbh, String kdgs, String kddh, String ms, BigInteger kdsj) {
        String username=properties.getUsername();
        String pwd=properties.getPwd();
        String enPwd = Enxi.enPwd(username, pwd);
        String jsonStr="{\n" +
                "\"username\": \""+username+"\", \n" +
                "\"pwd\": \""+enPwd+"\",\n" +
                "\"ddbh\": \""+ddbh+"\", \n" +
                "\"sfcd\": \""+sfcd+"\", \n" +
                "\"fczddbh\": \""+fczddbh+"\", \n" +
                "\"kdgs\": \""+kdgs+"\", \n" +
                "\"kddh\": \""+kddh+"\", \n" +
                "\"ms\": \""+ms+"\",\n" +
                "\"kdsj\": \""+kdsj+"\"\n" +
                "}\n";
        JSONObject jsonObject=ClientUtil.getJSONObject("http://222.143.21.205:8091/wsscservices_test/services/wsscWebService?wsdl","exeLogistics",jsonStr);
        if(jsonObject!=null&& "Y".equals(jsonObject.get("resultFlag"))){
            logger.info("物流消息发送成功！");
            OrderData orderData=new OrderData();
            orderData.setSfcd(sfcd);
            orderData.setFczddbh(fczddbh);
            orderData.setKdgs(kdgs);
            orderData.setKddh(kddh);
            orderData.setMs(ms);
            orderData.setKdsj(kdsj);
            return  upDateTwo(ddbh,orderData);
        }
        else {
            logger.info("物流消息发送失败！");
            return  new Result(ResultCode.FAIL);
        }

    }


    //订单签收时间信息推送
    @Override
    public Result ensureOrderTimeSubmit(String ddbh, int sfcd, String fczddbh, BigInteger shsj) {
        if(!"3".equals(checkZt(ddbh))){
            logger.info("该订单状态存在问题！");
            return new Result(ResultCode.FAIL);
        }
        String username=properties.getUsername();
        String pwd=properties.getPwd();
        String enPwd = Enxi.enPwd(username, pwd);
        String jsonStr="{\n" +
                "\"username\": \""+username+"\", \n" +
                "\"pwd\": \""+enPwd+"\",\n" +
                "\"ddbh\": \""+ddbh+"\", \n" +
                "\"shsj\": \""+shsj+"\",\n" +
                "\"sfcd\": \""+sfcd+"\",\n" +
                "\"fczddbh\": \""+fczddbh+"\"\n" +
                "}\n";
        JSONObject jsonObject=ClientUtil.getJSONObject("http://222.143.21.205:8091/wsscservices_test/services/wsscWebService?wsdl","execQssj",jsonStr);
        assert jsonObject != null;
        if(jsonObject.get("resultFlag")!=null&& "Y".equals(jsonObject.get("resultFlag"))) {
            QueryWrapper<OrderData> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("ddbh",ddbh);
            OrderData orderData=new OrderData();
            orderData.setSfcd(sfcd);
            if(sfcd==0){
                orderData.setFczddbh(fczddbh);
            }
            orderData.setShsj(shsj);
            orderData.setZt("5");
            return  upDateTwo(ddbh,orderData);
        }
        return new Result(ResultCode.FAIL);

    }



    //订单发票开始开具时间信息推送
    @Override
    public Result invoiceStaTimeSubmit(String ddbh, BigInteger fpkjsj) {
        if(!"5".equals(checkZt(ddbh))){
            logger.info("该订单状态存在问题！");
            return new Result(ResultCode.FAIL);
        }
        String username=properties.getUsername();
        String pwd=properties.getPwd();
        DateTime dateTime=new DateTime();
        String enPwd = Enxi.enPwd(username, pwd);
        String jsonStr="{\n" +
                "\"username\": \""+username+"\", \n" +
                "\"pwd\": \""+enPwd+"\",\n" +
                "\"ddbh\": \""+ddbh+"\", \n" +
                "\"fpkjsj\":\""+fpkjsj+"\"\n" +
                "}\n";
        JSONObject jsonObject=ClientUtil.getJSONObject("JSONObject jsonObject=ClientUtil.getJSONObject","execFpkjsjByOrder",jsonStr);
        assert jsonObject != null;
        if(jsonObject.get("resultFlag")!=null&& "Y".equals(jsonObject.get("resultFlag"))) {
            logger.info("发票开具开始时间已推送！");
            OrderData orderData=new OrderData();
            orderData.setFpkjsj(fpkjsj);
            return upDateTwo(ddbh,orderData);
        }
        logger.info("发票开具开始时间推送失败！");
        return new Result(ResultCode.FAIL);
    }


    //订单发票开具结束时间信息推送
    @Override
    public Result invoiceEndTimeSubmit(String ddbh, BigInteger fpsdsj) {
        if(!"5".equals(checkZt(ddbh))){
            logger.info("该订单状态存在问题！");
            return new Result(ResultCode.FAIL);
        }
        String username=properties.getUsername();
        String pwd=properties.getPwd();
        String enPwd = Enxi.enPwd(username, pwd);
        String jsonStr="{\n" +
                "\"username\": \""+username+"\", \n" +
                "\"pwd\": \""+enPwd+"\",\n" +
                "\"ddbh\": \""+ddbh+"\", \n" +
                "\"fpsdsj\": \""+fpsdsj+"\"\n" +
                "}\n";
        JSONObject jsonObject=ClientUtil.getJSONObject("http://222.143.21.205:8091/wsscservices_test/services/wsscWebService?wsdl","execfpsdsjByorder",jsonStr);
        if(jsonObject.get("resultFlag")!=null&& "Y".equals(jsonObject.get("resultFlag"))) {
            logger.info("收到发票时间信息已推送！");
            OrderData orderData=new OrderData();
            orderData.setFpsdsj(fpsdsj);
            return upDateTwo(ddbh,orderData);
        }
        logger.info("收到发票时间信息推送失败！");
        return new Result(ResultCode.FAIL);

    }


    //电商已经收到采购单位的付款,将收款标志、收款金额、收款时间提交
    @Override
    public Result getMoneyDataSubmit(String ddbh, int skbz, Integer skje, BigInteger sksj) {
        if(!"2".equals(checkZt(ddbh))){
            logger.info("该订单状态存在问题！");
            return new Result(ResultCode.FAIL);
        }
        String username=properties.getUsername();
        String pwd=properties.getPwd();
        String enPwd = Enxi.enPwd(username, pwd);
        String jsonStr="{\n" +
                "\"username\": \""+username+"\", \n" +
                "\"pwd\": \""+enPwd+"\",\n" +
                "\"ddbh\": \""+ddbh+"\", \n" +
                "\"skbz\": \""+skbz+"\",\n" +
                "\"skje\": \""+skje+"\"，\n" +
                "\"sksj\": \""+sksj+"\"\n" +
                "}\n";
        JSONObject jsonObject=ClientUtil.getJSONObject("http://222.143.21.205:8091/wsscservices_test/services/wsscWebService?wsdl","execSkqk",jsonStr);
        assert jsonObject != null;
        if(jsonObject.get("resultFlag")!=null&&jsonObject.get("resultFlag").equals("Y")) {
            logger.info("标志、收款金额、收款时间已提交！");
            OrderData orderData=new OrderData();
            orderData.setSkbz(skbz);
            orderData.setSkje(skje);
            orderData.setSksj(sksj);
            return upDateTwo(ddbh,orderData);
        }
        logger.info("标志、收款金额、收款时间提交失败！");
        return new Result(ResultCode.FAIL);

    }




    //取消订单(已经对进行订单确认)
    @Override
    public Result deletEnsureOrder(String ddbh, String qxyy) {
        if(!"3".equals(checkZt(ddbh))){
            logger.info("该订单状态存在问题！");
            return new Result(ResultCode.FAIL);
        }
        String username=properties.getUsername();
        String pwd=properties.getPwd();
        String enPwd = Enxi.enPwd(username, pwd);
        String jsonStr="{\n" +
                "\"username\": \""+username+"\", \n" +
                "\"pwd\": \""+enPwd+"\",\n" +
                "\"ddbh\": \""+ddbh+"\", \n" +
                "\"qxyy\":\""+qxyy+"\"\n" +
                "}\n";
        JSONObject jsonObject=ClientUtil.getJSONObject("http://222.143.21.205:8091/wsscservices_test/services/wsscWebService?wsdl","execDsZfdd",jsonStr);
        assert jsonObject != null;
        if(jsonObject.get("resultFlag")!=null&& "Y".equals(jsonObject.get("resultFlag"))) {
            OrderData orderData=new OrderData();
            orderData.setZt("4");
            return upDateTwo(ddbh,orderData);
        }
        return new Result(ResultCode.FAIL);
    }





    //查看采购单位对当前订单的验收情况(可以不存入数据库，只展示)
    @Override
    public Result checkOrderStatus(String ddbh) {
        String username=properties.getUsername();
        String pwd=properties.getPwd();
        String enPwd = Enxi.enPwd(username, pwd);
        String jsonStr="{\n" +
                "\"username\": \""+username+"\", \n" +
                "\"pwd\": \""+enPwd+"\",\n" +
                "\"ddbh\": \""+ddbh+"\", \n" +
                "}\n";
        JSONObject jsonObject=ClientUtil.getJSONObject("http://222.143.21.205:8091/wsscservices_test/services/wsscWebService?wsdl","findYsByOrder",jsonStr);
        assert jsonObject != null;
        if(jsonObject.get("resultFlag")!=null&& "Y".equals(jsonObject.get("resultFlag"))) {
            OrderData orderData=new OrderData();
            orderData.setZt("5");
            return upDateTwo(ddbh,orderData);
        }
        return new Result(ResultCode.FAIL);
    }


    //更新合同
    @Override
    public Result contractWork(String ddbh) {
        String username=properties.getUsername();
        String pwd=properties.getPwd();
        String enPwd = Enxi.enPwd(username, pwd);
        String jsonStr="{\n" +
                "\"username\": \""+username+"\", \n" +
                "\"pwd\": \""+enPwd+"\",\n" +
                "\"ddbh\": \""+ddbh+"\", \n" +
                "}\n";
        JSONObject jsonObject=ClientUtil.getJSONObject("http://222.143.21.205:8091/wsscservices_test/services/wsscWebService?wsdl","findOrderHt",jsonStr);
        Contract contract=new Contract();
        assert jsonObject != null;
        contract.setContractUrl(jsonObject.getString("url"));
        contract.setDdbh(ddbh);
        return null;
    }

    //查询不同状态的订单 2-供应商待确认3-待验收 4-订单已取消5-验收通过
    @Override
    public Result selectStatusDataOrder(Integer pageNum, Integer pageSize, String status) {
        QueryWrapper<OrderData> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("zt",status);
        Page page = new Page(pageNum, pageSize);
        IPage iPage;
        iPage = baseMapper.selectPage(page,queryWrapper);
        List orderList = iPage.getRecords();
        logger.info(orderList.toString());
        return new Result(ResultCode.SUCCESS, "成功！", orderList, iPage.getPages(), iPage.getCurrent());
    }

    //根据订单信息查询订单列表
    @Override
    public Result selectOrderListByData(String ddbh,String cgrmc,String zt,Integer pageSize,Integer pageNum) {
        logger.info("ddbh:"+ddbh+" cgrmc:"+cgrmc+" zt:"+zt+" pageSize:"+pageSize+" pageNum:"+pageNum);
        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();
            int number=0;
            if (ddbh != null) {
                queryWrapper.like("ddbh", ddbh);
                number++;
            }
            if (cgrmc != null) {
                queryWrapper.like("cgrmc", cgrmc);
                number++;
            }
            if (zt != null) {
                queryWrapper.eq("zt", zt);
                number++;
            }
            if(number==0){
                queryWrapper=null;
            }
            Page page = new Page(pageNum, pageSize);
            IPage iPage;
            iPage = baseMapper.selectPage(page, queryWrapper);
            List orderList = iPage.getRecords();
            logger.info(orderList.toString());
            return new Result(ResultCode.SUCCESS, "成功！", orderList, iPage.getPages(), iPage.getCurrent());
    }


    private Result upDateTwo(String ddbh, OrderData orderData) {
        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ddbh", ddbh);
        int data = baseMapper.update(orderData, queryWrapper);
        if (data >= 0) {
            logger.info("操作订单成功！");
            return new Result(ResultCode.SUCCESS, "操作订单成功!");
        } else {
            logger.info("本地数据库操作订单失败！");
            return new Result(ResultCode.FAIL, "本地数据库操作订单失败！");
        }
    }

    private boolean checkddbh(String ddbh){
        QueryWrapper<OrderData> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("ddbh",ddbh);
        OrderData data=baseMapper.selectOne(queryWrapper);
        return data == null;
    }

    private String checkZt(String ddbh){
        QueryWrapper<OrderData> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("ddbh",ddbh);
        OrderData data=baseMapper.selectOne(queryWrapper);
        if(data==null){
            return "4";
        }
        return data.getZt();
    }

     private String checkUpDataTime(){
        File file=new File(System.getProperty("user.dir")+"/upDataTime.txt");
        if(!file.exists()){
          return "暂无更新时间！";
        }
        String upDataTime="";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            upDataTime=br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return upDataTime;
    }

     private void setUpDataTime(String upDataTime){
        String path=System.getProperty("user.dir");
            File file;
        try {
            file=new File(path,"upDataTime.txt");
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fw =  new FileWriter(file);
            fw.write(upDataTime);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}



