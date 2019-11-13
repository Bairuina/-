package net.wlgzs.purchase.service.impl;import com.Enxi;import com.alibaba.fastjson.JSON;import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;import com.baomidou.mybatisplus.core.metadata.IPage;import com.baomidou.mybatisplus.extension.plugins.pagination.Page;import net.sf.json.JSONArray;import net.sf.json.JSONObject;import net.wlgzs.purchase.entity.*;import net.wlgzs.purchase.mapper.OrderDataMapper;import net.wlgzs.purchase.service.*;import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;import net.wlgzs.purchase.util.*;import org.codehaus.xfire.client.Client;import org.joda.time.DateTime;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;import java.io.*;import java.math.BigDecimal;import java.math.BigInteger;import java.net.URL;import java.util.ArrayList;import java.util.List;/** * <p> * 服务实现类 * </p> * * @author 王言 * @since 2019-10-03 */@Servicepublic class OrderDataServiceImpl extends ServiceImpl<OrderDataMapper, OrderData> implements IOrderDataService {    IRedis iRedis;    @Autowired    IContractService contractService;    @Autowired    IAccessoryListService iAccessoryListService;    @Autowired    IProductListService iProductListService;    @Autowired    IServiceListService iServiceListService;    @Autowired    ReadProperties properties;    @Autowired    private WebSocketServer webSocketServer;    Logger logger = LoggerFactory.getLogger(OrderDataServiceImpl.class);    //更新订单    @Override    public Result updateOrderDate(Integer pageNum,String kssjData) {        String userName = properties.getUsername();        String pwd = properties.getPwd();        DateTime dateTime = new DateTime();        String enPwd = Enxi.enPwd(userName, pwd);        String jssj = dateTime.toString("yyyyMMddHHmmss");        String kssj = kssjData;        logger.info("kssj(开始时间):" + kssj);        logger.info("jssj(结束时间):" + jssj);        int pageSize = 50;        String result = null;        try {            Client client = new Client(new URL(properties.getUrl()));            String jsonStr;            jsonStr = "{\n" +                    "\"username\":\"" + userName + "\",\n" +                    "\"pwd\":\"" + enPwd + "\",\n" +                    "\"zt\":\"" + "2" + "\",\n" +                    "\"kssj\":\"" + kssj + "\",\n" +                    "\"jssj\":\"" + jssj + "\",\n" +                    "\"pageNum\":\"" + pageNum + "\",\n" +                    "\"pageSize\":\"50\"\n}";            logger.info("\n\n"+jsonStr+"\n\n");            Object[] rets = client.invoke("findOrder", new Object[]{jsonStr});            result = rets[0].toString();            logger.info("\n\nresult="+result+"\n\n");        } catch (Exception e) {            e.printStackTrace();        }        JSONObject jsonObject = JSONObject.fromObject(result);        String flag = jsonObject.get("resultFlag").toString();        if ("N".equals(flag)) {            logger.info("没有订单信息！");            webSocketServer.sendMessage("此次订单更新失败！");            return new Result(ResultCode.FAIL);        }        setUpDataTime(dateTime.toString("yyyyMMddHHmmss"));        int count = Integer.parseInt(jsonObject.get("count").toString());        List<OrderData> orderList = null;        List<ProductList> productLists;        if (jsonObject.get("orderList") == null || jsonObject.get("orderList").equals("")) {            logger.info("没有更新内容!");            webSocketServer.sendMessage("此次订单更新,没有可需更新内容。");            return new Result(ResultCode.FAIL);        }        JSONArray orderListData = jsonObject.getJSONArray("orderList");        List<OrderData> orderDataList = JSON.parseArray(orderListData.toString(), OrderData.class);        logger.info(orderDataList.toString() + "\n\n");        for (OrderData orderData : orderDataList) {            if (!checkddbh(orderData.getDdbh())) {                logger.info("该订单已存在！");                    continue;            }            productLists = orderData.getProductList();            if (productLists != null) {                List<AccessoryList> accessoryLists;                List<ServiceList> serviceLists;                for (ProductList productList : productLists) {                    productList.setDdbh(orderData.getDdbh());                    accessoryLists = productList.getAccessoryList();                    serviceLists = productList.getServiceList();                    if (accessoryLists != null) {                        logger.info("\n\n"+accessoryLists.toString()+"\n\n");                        for (AccessoryList accessoryList : accessoryLists) {                            accessoryList.setXhbh(productList.getXhbh());                            accessoryList.setPpbh(productList.getPpbh());                            accessoryList.setDdbh(orderData.getDdbh());                            logger.info("AccessoryList:" + accessoryList.toString());                            if (iAccessoryListService.save(accessoryList)) {                                logger.info("添加成功！\n\n");                            }                        }                    }                    if (serviceLists != null) {                        logger.info("\n\n"+serviceLists.toString()+"\n\n");                        for (ServiceList serviceList : serviceLists) {                            serviceList.setDdbh(orderData.getDdbh());                            serviceList.setXhbh(productList.getXhbh());                            serviceList.setPpbh(productList.getPpbh());                            logger.info("ServiceList:" + serviceList.toString());                            if (iServiceListService.save(serviceList)) {                                logger.info("添加成功！\n\n");                            }                        }                    }                    logger.info("ProductList:" + productList.toString());                    productList.setAccessoryList(null);                    productList.setServiceList(null);                    if (iProductListService.save(productList)) {                        logger.info("添加成功！\n\n");                    }                }            }            orderData.setProductList(null);            baseMapper.insert(orderData);        }        int totalPageNum = (count + 14) / 15;        if (totalPageNum > pageNum) {            pageNum++;            updateOrderDate(pageNum,kssj);        }        logger.info("更新完成！");        webSocketServer.sendMessage("订单信息更新完成");        return new Result(ResultCode.SUCCESS);    }    //查询所有订单    @Override    public Result selectAllOrder(Integer pageSize, Integer pageNum) {        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();        Page page = new Page(pageNum, pageSize);        IPage<OrderData> iPage = null;        iPage = baseMapper.selectPage(page, queryWrapper);        List orderList = iPage.getRecords();        logger.info("\n\n总页数为：" + iPage.getPages() + "当前页数为：" + iPage.getCurrent() + "\n");        return new Result(ResultCode.SUCCESS, "成功！", orderList, iPage.getPages(), iPage.getCurrent());    }    //查看订单详情    @Override    public Result selectOneOrder(String ddbh) {        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();        queryWrapper.eq("ddbh", ddbh);        OrderData orderData = baseMapper.selectOne(queryWrapper);        if (orderData == null) {            logger.info("没有该订单详情");            return new Result(ResultCode.FAIL);        }        String ddbhData = orderData.getDdbh();        List<ProductList> productLists;        productLists = iProductListService.selectProductList(ddbhData);        List<Object> list = new ArrayList<>();        list.add(orderData);        list.add(productLists);        list.add(CheckAddress.checkOneAddressWordByNumber(orderData.getDeliverycity()));        list.add(CheckAddress.checkOneAddressWordByNumber(orderData.getDeliverycounty()));        return new Result(ResultCode.SUCCESS, list);    }    //确定或拒绝订单    @Override    public Result ensureORefuseOrder(String ddbh, Integer qrzt) {        logger.info("ddbh：" + ddbh + " qrzt：" + qrzt);        if (!"2".equals(checkZt(ddbh))) {            logger.info("该订单状态存在问题！");            return new Result(ResultCode.FAIL);        }        if(qrzt==null){            return new Result(ResultCode.FAIL);        }        String username = properties.getUsername();        String pwd = properties.getPwd();        String enPwd = Enxi.enPwd(username, pwd);        String jsonStr = "{\n" +                "\"username\": \"" + username + "\", \n" +                "\"pwd\": \"" + enPwd + "\", \n" +                "\"ddbh\": \"" + ddbh + "\",\n" +                "\"qrzt\": " + qrzt + "\n" +                "}\n";        System.out.println(jsonStr);        try {            Client client = new Client(new URL(properties.getUrl()));            Object[] rets = client.invoke("execGysOrderQr", new Object[]{jsonStr});            String result = rets[0].toString();            logger.info(result);            JSONObject jsonObject = JSONObject.fromObject(result);            if (jsonObject.get("resultFlag") != null && jsonObject.get("resultFlag").equals("Y")) {                String ddbhData = ddbh;                OrderData orderData = new OrderData();                if (qrzt == 1) {                    orderData.setZt("4");                } else if (qrzt == 0) {                    orderData.setZt("3");                }                return upDateTwo(ddbh, orderData);            } else {                logger.info("操作订单失败！");                return new Result(ResultCode.FAIL);            }        } catch (Exception e) {            e.printStackTrace();        }        logger.info("操作订单失败！");        return new Result(ResultCode.FAIL);    }    //订单签收物流信息推送    @Override    public Result ensureOrderArrive(String ddbh, Integer sfcd, String fczddbh, String kdgs, String kddh, String ms, BigInteger kdsj) {        if(ddbh==null||"".equals(ddbh)){         return new Result(ResultCode.FAIL);        }        if(kdsj==null){            return new Result(ResultCode.FAIL);        }        if(kddh==null||"".equals(kddh)){            return new Result(ResultCode.FAIL);        }        String username = properties.getUsername();        String pwd = properties.getPwd();        String enPwd = Enxi.enPwd(username, pwd);        String jsonStr = "{\n" +                "\"username\": \"" + username + "\", \n" +                "\"pwd\": \"" + enPwd + "\",\n" +                "\"ddbh\": \"" + ddbh + "\", \n" +                "\"sfcd\": \"" + sfcd + "\", \n" +                "\"fczddbh\": \"" + fczddbh + "\", \n" +                "\"kdgs\": \"" + kdgs + "\", \n" +                "\"kddh\": \"" + kddh + "\", \n" +                "\"ms\": \"" + ms + "\",\n" +                "\"kdsj\": \"" + kdsj + "\"\n" +                "}\n";        JSONObject jsonObject = ClientUtil.getJSONObject(properties.getUrl(), "exeLogistics", jsonStr);        if (jsonObject != null && "Y".equals(jsonObject.get("resultFlag"))) {            logger.info("物流消息发送成功！");            OrderData orderData = new OrderData();            orderData.setSfcd(sfcd);            orderData.setFczddbh(fczddbh);            orderData.setKdgs(kdgs);            orderData.setKddh(kddh);            orderData.setMs(ms);            orderData.setKdsj(kdsj);            return upDateTwo(ddbh, orderData);        } else {            logger.info("物流消息发送失败！");            return new Result(ResultCode.FAIL);        }    }    //订单签收时间信息推送    @Override    public Result ensureOrderTimeSubmit(String ddbh, Integer sfcd, String fczddbh, BigInteger shsj) {        if(ddbh==null || "".equals(ddbh)){            return new Result(ResultCode.FAIL);        }        if(sfcd==null){            return new Result(ResultCode.FAIL);        }        if(fczddbh==null || "".equals(fczddbh)){            return new Result(ResultCode.FAIL);        }        if(shsj==null){            return new Result(ResultCode.FAIL);        }        String username = properties.getUsername();        String pwd = properties.getPwd();        String enPwd = Enxi.enPwd(username, pwd);        String jsonStr = "{\n" +                "\"username\": \"" + username + "\", \n" +                "\"pwd\": \"" + enPwd + "\",\n" +                "\"ddbh\": \"" + ddbh + "\", \n" +                "\"shsj\": \"" + shsj + "\",\n" +                "\"sfcd\": \"" + sfcd + "\",\n" +                "\"fczddbh\": \"" + fczddbh + "\"\n" +                "}\n";        JSONObject jsonObject = ClientUtil.getJSONObject(properties.getUrl(), "execQssj", jsonStr);        logger.info(jsonObject.toString());        assert jsonObject != null;        if (jsonObject.get("resultFlag") != null && "Y".equals(jsonObject.get("resultFlag"))) {            QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();            queryWrapper.eq("ddbh", ddbh);            OrderData orderData = new OrderData();            orderData.setSfcd(sfcd);            if (sfcd == 0) {                orderData.setFczddbh(fczddbh);            }            orderData.setShsj(shsj);            orderData.setZt("5");            return upDateTwo(ddbh, orderData);        }        return new Result(ResultCode.FAIL);    }    //订单发票开始开具时间信息推送    @Override    public Result invoiceStaTimeSubmit(String ddbh, BigInteger fpkjsj) {        if (!"5".equals(checkZt(ddbh))) {            logger.info("该订单状态存在问题！");            return new Result(ResultCode.FAIL);        }        if(fpkjsj==null){            return new Result(ResultCode.FAIL);        }        String username = properties.getUsername();        String pwd = properties.getPwd();        DateTime dateTime = new DateTime();        String enPwd = Enxi.enPwd(username, pwd);        String jsonStr = "{\n" +                "\"username\": \"" + username + "\", \n" +                "\"pwd\": \"" + enPwd + "\",\n" +                "\"ddbh\": \"" + ddbh + "\", \n" +                "\"fpkjsj\":\"" + fpkjsj + "\"\n" +                "}\n";        JSONObject jsonObject = ClientUtil.getJSONObject(properties.getUrl(), "execFpkjsjByOrder", jsonStr);        assert jsonObject != null;        if (jsonObject.get("resultFlag") != null && "Y".equals(jsonObject.get("resultFlag"))) {            logger.info("发票开具开始时间已推送！");            OrderData orderData = new OrderData();            orderData.setFpkjsj(fpkjsj);            return upDateTwo(ddbh, orderData);        }        logger.info("发票开具开始时间推送失败！");        return new Result(ResultCode.FAIL);    }    //订单发票开具结束时间信息推送    @Override    public Result invoiceEndTimeSubmit(String ddbh, BigInteger fpsdsj) {        if (!"5".equals(checkZt(ddbh))) {            logger.info("该订单状态存在问题！");            return new Result(ResultCode.FAIL);        }        if(fpsdsj==null){            return new Result(ResultCode.FAIL);        }        String username = properties.getUsername();        String pwd = properties.getPwd();        String enPwd = Enxi.enPwd(username, pwd);        String jsonStr = "{\n" +                "\"username\": \"" + username + "\", \n" +                "\"pwd\": \"" + enPwd + "\",\n" +                "\"ddbh\": \"" + ddbh + "\", \n" +                "\"fpsdsj\": \"" + fpsdsj + "\"\n" +                "}\n";        JSONObject jsonObject = ClientUtil.getJSONObject(properties.getUrl(), "execfpsdsjByorder", jsonStr);        if (jsonObject.get("resultFlag") != null && "Y".equals(jsonObject.get("resultFlag"))) {            logger.info("收到发票时间信息已推送！");            OrderData orderData = new OrderData();            orderData.setFpsdsj(fpsdsj);            return upDateTwo(ddbh, orderData);        }        logger.info("收到发票时间信息推送失败！");        return new Result(ResultCode.FAIL);    }    //电商已经收到采购单位的付款,将收款标志、收款金额、收款时间提交    @Override    public Result getMoneyDataSubmit(String ddbh, Integer skbz, BigDecimal skje, BigInteger sksj) {        if (!"2".equals(checkZt(ddbh))) {            logger.info("该订单状态存在问题！");            return new Result(ResultCode.FAIL);        }        if(skbz==null || skje==null || sksj==null){            return new Result(ResultCode.FAIL);        }        String username = properties.getUsername();        String pwd = properties.getPwd();        String enPwd = Enxi.enPwd(username, pwd);        String jsonStr = "{\n" +                "\"username\": \"" + username + "\", \n" +                "\"pwd\": \"" + enPwd + "\",\n" +                "\"ddbh\": \"" + ddbh + "\", \n" +                "\"skbz\": \"" + skbz + "\",\n" +                "\"skje\": \"" + skje + "\",\n" +                "\"sksj\": \"" + sksj + "\"\n" +                "}\n";        JSONObject jsonObject = ClientUtil.getJSONObject(properties.getUrl(), "execSkqk", jsonStr);        assert jsonObject != null;        if (jsonObject.get("resultFlag") != null && jsonObject.get("resultFlag").equals("Y")) {            logger.info("标志、收款金额、收款时间已提交！");            OrderData orderData = new OrderData();            orderData.setSkbz(skbz);            orderData.setSkje(skje);            orderData.setSksj(sksj);            return upDateTwo(ddbh, orderData);        }        logger.info("标志、收款金额、收款时间提交失败！");        return new Result(ResultCode.FAIL);    }    //取消订单(已经对进行订单确认)    @Override    public Result deletEnsureOrder(String ddbh, String qxyy) {        if (!"3".equals(checkZt(ddbh))) {            logger.info("该订单状态存在问题！");            return new Result(ResultCode.FAIL);        }        if(ddbh==null || "".equals(ddbh)){            return new Result(ResultCode.FAIL);        }        if(ddbh==null || "".equals(ddbh)){            return new Result(ResultCode.FAIL);        }        String username = properties.getUsername();        String pwd = properties.getPwd();        String enPwd = Enxi.enPwd(username, pwd);        String jsonStr = "{\n" +                "\"username\": \"" + username + "\", \n" +                "\"pwd\": \"" + enPwd + "\",\n" +                "\"ddbh\": \"" + ddbh + "\", \n" +                "\"qxyy\":\"" + qxyy + "\"\n" +                "}\n";        JSONObject jsonObject = ClientUtil.getJSONObject(properties.getUrl(), "execDsZfdd", jsonStr);        assert jsonObject != null;        if (jsonObject.get("resultFlag") != null && "Y".equals(jsonObject.get("resultFlag"))) {            OrderData orderData = new OrderData();            orderData.setZt("4");            return upDateTwo(ddbh, orderData);        }        return new Result(ResultCode.FAIL);    }    //查看采购单位对当前订单的验收情况(可以不存入数据库，只展示)    @Override    public Result checkOrderStatus(String ddbh) {        String username = properties.getUsername();        String pwd = properties.getPwd();        String enPwd = Enxi.enPwd(username, pwd);        String jsonStr = "{\n" +                "\"username\": \"" + username + "\", \n" +                "\"pwd\": \"" + enPwd + "\",\n" +                "\"ddbh\": \"" + ddbh + "\", \n" +                "}\n";        JSONObject jsonObject = ClientUtil.getJSONObject(properties.getUrl(), "findYsByOrder", jsonStr);        assert jsonObject != null;        if (jsonObject.get("resultFlag") != null && "Y".equals(jsonObject.get("resultFlag"))) {            OrderData orderData = new OrderData();            orderData.setZt("5");            return upDateTwo(ddbh, orderData);        }        return new Result(ResultCode.FAIL);    }    //更新合同    @Override    public Result contractWork(String ddbh) {        String username = properties.getUsername();        String pwd = properties.getPwd();        String enPwd = Enxi.enPwd(username, pwd);        String jsonStr = "{\n" +                "\"username\": \"" + username + "\", \n" +                "\"pwd\": \"" + enPwd + "\",\n" +                "\"ddbh\": \"" + ddbh + "\", \n" +                "}\n";        logger.info(jsonStr);        JSONObject jsonObject = ClientUtil.getJSONObject(properties.getUrl(), "findOrderHt", jsonStr);        Contract contract = new Contract();        assert jsonObject != null;        contract.setContractUrl(jsonObject.getString("url"));        contract.setDdbh(ddbh);        return null;    }    //查询不同状态的订单 2-供应商待确认3-待验收 4-订单已取消5-验收通过    @Override    public Result selectStatusDataOrder(Integer pageNum, Integer pageSize, String status) {        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();        queryWrapper.eq("zt", status);        Page page = new Page(pageNum, pageSize);        IPage iPage;        iPage = baseMapper.selectPage(page, queryWrapper);        List orderList = iPage.getRecords();        logger.info(orderList.toString());        return new Result(ResultCode.SUCCESS, "成功！", orderList, iPage.getPages(), iPage.getCurrent());    }    //根据订单信息查询订单列表    @Override    public Result selectOrderListByData(String data, String zt, Integer pageSize, Integer pageNum) {        logger.info("dataAll:" + data + " zt:" + zt + " pageSize:" + pageSize + " pageNum:" + pageNum);        QueryWrapper<OrderData> queryWrapperOne = new QueryWrapper<>();        int number = 0;        if (zt != null && !"".equals(zt) && !"all".equals(zt)) {            queryWrapperOne.eq("zt", zt);            number++;        }        String[] dataAll = null;        if (data != null && !"".equals(data)) {            data = data.replace(' ', '+');            dataAll = data.split("\\+");            if (dataAll.length > 1) {                queryWrapperOne.like("ddbh", dataAll[0]).like("cgrmc", dataAll[1]).or().like("ddbh", dataAll[1]).like("cgrmc", dataAll[0]);            } else {                queryWrapperOne.like("ddbh", data).or().like("cgrmc", data);            }            number++;        }        if (number == 0) {            queryWrapperOne = null;        }        Page page = new Page(pageNum, pageSize);        IPage iPage;        iPage = baseMapper.selectPage(page, queryWrapperOne);        List orderList = iPage.getRecords();        logger.info(orderList.toString());        return new Result(ResultCode.SUCCESS, "成功！", orderList, iPage.getPages(), iPage.getCurrent());    }    //根据信息删除单个订单    @Override    public Result delOrderByData(String ddbh) {        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();        queryWrapper.eq("ddbh", ddbh);        String zt = checkZt(ddbh);        if (!("4".equals(zt) || "5".equals(zt))) {            logger.info("订单：" + ddbh + "  订单状态不允许被删除！");            return new Result(ResultCode.FAIL, "该订单不允许被删除！");        }        if ("4".equals(zt)) {            int num = baseMapper.delete(queryWrapper);            if (num >= 0) {                return new Result(ResultCode.SUCCESS, "订单删除成功！");            }        }        if ("5".equals(zt)) {            OrderData orderData = baseMapper.selectOne(queryWrapper);            BigInteger fpsdsj = orderData.getFpsdsj();            BigInteger fpkjsj = orderData.getFpkjsj();            Integer sfcd = orderData.getSfcd();            String fczddbh = orderData.getFczddbh();            BigInteger shsj = orderData.getShsj();            if (fpsdsj != null && fpkjsj != null && sfcd != null && fczddbh != null && shsj != null) {                int num = baseMapper.delete(queryWrapper);                if (num >= 0) {                    return new Result(ResultCode.SUCCESS, "订单删除成功！");                }            }        }        return new Result(ResultCode.FAIL, "该订单删除失败！");    }    //批量删除订单编号    @Override    public Result delOrdersByData(String[] ddbh) {        return null;    }    //更新单个订单    @Override    public Result upDataOneData(String ddbh) {        if(ddbh==null || "".equals(ddbh)){            return new Result(ResultCode.FAIL);        }        logger.info("更新订单信息中。。。");        String username = properties.getUsername();        String pwd = properties.getPwd();        String enPwd = Enxi.enPwd(username, pwd);        String jsonStr = "{\n" +                "\"username\": \"" + username + "\", \n" +                "\"pwd\": \"" + enPwd + "\",\n" +                "\"ddbh\": \"" + ddbh + "\", \n" +                "}\n";        logger.info(jsonStr);        JSONObject data = ClientUtil.getJSONObject(properties.getUrl(), "findDdxxByddbh", jsonStr);        String flag = data.get("resultFlag").toString();        if ("N".equals(flag)) {            logger.info("获取更新单个订单失败");            return new Result(ResultCode.FAIL);        } else if ("Y".equals(flag)) {            OrderData orderData = JSON.parseObject(data.toString(), OrderData.class);            QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();            queryWrapper.eq("ddbh", ddbh);            orderData.setProductList(null);            baseMapper.update(orderData, queryWrapper);            return new Result(ResultCode.SUCCESS);        }        return new Result(ResultCode.FAIL);    }    private Result upDateTwo(String ddbh, OrderData orderData) {        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();        queryWrapper.eq("ddbh", ddbh);        int data = baseMapper.update(orderData, queryWrapper);        if (data >= 0) {            logger.info("操作订单成功！");            return new Result(ResultCode.SUCCESS, "操作订单成功!");        } else {            logger.info("本地数据库操作订单失败！");            return new Result(ResultCode.FAIL, "本地数据库操作订单失败！");        }    }    private boolean checkddbh(String ddbh) {        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();        queryWrapper.eq("ddbh", ddbh);        OrderData data = baseMapper.selectOne(queryWrapper);        return data == null;    }    private String checkZt(String ddbh) {        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();        queryWrapper.eq("ddbh", ddbh);        OrderData data = baseMapper.selectOne(queryWrapper);        if (data == null) {            return "4";        }        return data.getZt();    }    private String checkUpDataTime() {        File file = new File(System.getProperty("user.dir") + "/upDataTime.txt");        if (!file.exists()) {            return "暂无更新时间！";        }        String upDataTime = "";        try {            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));            upDataTime = br.readLine();        } catch (IOException e) {            e.printStackTrace();        }        return upDataTime;    }    private void setUpDataTime(String upDataTime) {        String path = System.getProperty("user.dir");        File file;        try {            file = new File(path, "upDataTime.txt");            if (!file.exists()) {                file.createNewFile();            }            FileWriter fw = new FileWriter(file);            fw.write(upDataTime);            fw.flush();            fw.close();        } catch (IOException e) {            e.printStackTrace();        }    }}