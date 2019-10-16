$(".yes_order").on('click', function () {
    var orderReference = $("#ddbhDataWord").text();
    console.log(orderReference);
    var inform = "您确定要接受订单号为" + orderReference + "的订单？";
    var r = confirm(inform);
    if (r) {
        $.ajax({
            type: "post",
            url: "/order-data/ensureORefuseOrder",
            dataType:"json",
            data: {
                'ddbh':orderReference,
                'qrzt':'0'
            },
            success: function (data) {
                console.log(data);
                if(data.code=='0'){
                    alert("确认订单成功！")
                    window.onload;
                }
                else{
                    alert("确认订单失败！")
                }
            },
            error: function (msg) {
                alert("确认订单失败");
            }
        })
    }
    else {
        location.reload(true);
    }
});

$(".no_order").on('click', function () {
    var orderReference = $("#ddbhDataWord").text()
    var inform = "您确定要拒绝订单号为" + orderReference + "的订单？";
    var ztDataWord=$("#ztDataWordLiWord").data('ztdata');
    var urlData;
    console.log(ztDataWord);
    if(ztDataWord=='2'){
        urlData='/order-data/ensureORefuseOrder'
    }
    if(ztDataWord=='3'){
        urlData='/order-data/deletEnsureOrder'
    }
    var r = confirm(inform);
    if (r) {
        $.ajax({
            type: "post",
            url: urlData,
            dataType: "JSON",
            data: {
                ddbh:orderReference,
                qrzt:'1'
            },
            success: function (data) {
                console.log(data);
                if(data.code=='0'){
                    alert("拒绝订单成功！")
                    window.onload;
                }
                else{
                    alert("拒绝订单失败！")
                }
            },
            error: function (msg) {
                alert("拒绝订单失败");
            }
        })
    }
    else {
    }
});


$(".lookContract").on('click', function () {
    var orderReference = $("#ddbhData").text();
    console.log(123)
    console.log(orderReference);
    $.ajax({
        type: "GET",
        url: "/contract/queryContract",
        data: {
            'ddbh': orderReference,
        },
        contentType:"application/json",
        dataType:"json",
        success: function (data) {
            console.log(data);
            if(data.code=='0'){
                alert("查看合同成功！")
                window.location.href = data.data.contractUrl;
            }
            else{
                alert("查看合同失败！")
            }
        },
        error: function (msg) {
            alert("查看合同失败！");
        }
    })
});



//提交更改
$("#submitMoney").on('click',function update() {
    //获取模态框数据
    var ddbh = $('#ddbhDataWord').text();
    var skbz = $('#skbzDataSelect').val();
    console.log(ddbh);
    console.log(skbz);
    var skje = $('#skjeDataSelect').val();
    var sksj = $("#sksjDataSelect").val();
    console.log(skje);
    console.log(sksj);
    var firstSignTime = sksj.replace(/-/g, '');
    var secSignTime = firstSignTime.replace(/:/g, '');
    var thirdSignTime = secSignTime.replace(/\s+/g, '');
    var endTimetest = /\d{14}/;
    var skbzNum;
    if (skbz == "已收款") {
        skbzNum = '1';
    } else if (skbz == "未收款") {
        skbzNum ='2';
    }
    console.log(thirdSignTime);
    console.log(skbzNum);
    if (endTimetest.test(thirdSignTime)) {
        console.log("信息接入成功")
        $.ajax({
            type: "post",
            url: "/order-data/selectDataNumberSubmit",
            dataType: "json",
            data: {
                'ddbh': ddbh,
                'skbz': skbzNum,
                'skje': skje,
                'sksj': thirdSignTime
            },
            success: function (data) {
                console.log(data);
                if (data.code == '0') {
                    alert("发送信息成功！")
                    location.href = "date";
                } else {
                    alert("发送信息失败！")
                }
            },
            error: function (data) {
                alert("发送信息失败！");
            }
        })
    }
});
