// 登陆模态框
function b() {
    //创建遮罩层div并插入body
    var mask = document.createElement("div");
    mask.id = "mask";
    //窗口可视区域高度
    var cheight = document.documentElement.clientHeight || document.body.clientHeight;
    mask.style.height = cheight + "px";
    //宽度直接用100%在样式里
    document.body.appendChild(mask);
    //创建登录层div并插入body
    var login = document.createElement("div");
    login.id = "login";
    login.innerHTML = '<div class="title" id="title">登录账号' + '<a href="#" class="close">×</a>' + '</div>' + '<div class="content">' + '<div class="user">' + '<input type="text" class="pt" placeholder="手机/邮箱/用户名" value="" name="">' + '</div>' + '<div class="password">' + '<input type="password" class="pt" placeholder="请输入密码" value="" name="">' + '</div>' + '<div class="submit">' + '<input type="button" class="sm" value="登陆">' + '</div>' + '</div>';
    document.body.appendChild(login);
    //窗口可视区域宽度
    var cwidth = document.documentElement.clientWidth || document.body.clientWidth;
    //登录框宽度
    var lwidth = login.offsetWidth;
    //登录框高度
    var lheight = login.offsetHeight;
    //设置登录框的居中显示
    login.style.left = (cwidth - lwidth) / 2 + "px";
    login.style.top = (cheight - lheight) / 2 + "px";
    //设置遮罩层的高度
    mask.style.height = cheight + "px";
    //改变窗口大小后依然居中显示
    //  onresize 事件会在窗口或框架被调整大小时发生。
    window.onresize = function() {
            if (document.compatMode == "CSS1Compat") {
                cwidth = document.documentElement.clientWidth;
                cheight = document.documentElement.clientHeight;
            } else {
                cwidth = document.body.clientWidth;
                cheight = document.body.clientHeight;
            }
            login.style.left = (cwidth - lwidth) / 2 + "px";
            login.style.top = (cheight - lheight) / 2 + "px";
            mask.style.height = cheight + "px";
        }
        //获取拖拽容器
    var title = document.getElementById("title");
    var isDraging = false; //以判断是可以进行拖拽
    var mouseOffsetX; //存放鼠标相对于登录框的位置
    var mouseOffsetY;
    //鼠标按下事件
    title.onmousedown = function(e) {
            var e = e || window.event;
            //鼠标相对于登录框的位置
            mouseOffsetX = e.pageX - login.offsetLeft;
            // e.pageX 是文档坐标而非窗口坐标
            // offsetLeft 鼠标相对于事件源元素（srcElement）的X,Y坐标
            mouseOffsetY = e.pageY - login.offsetTop;
            //鼠标摁下时为true
            isDraging = true;
        }
        //鼠标移动事件
    document.onmousemove = function(e) {
            var e = e || window.event;
            //鼠标移动时的坐标
            var newMX = e.pageX;
            var newMY = e.pageY;
            //判断为true时可以拖拽，
            if (isDraging === true) {
                //登录框的偏移值=当前位置-鼠标到登录框的距离
                var loginL = newMX - mouseOffsetX;
                var loginT = newMY - mouseOffsetY;
                //如果left top值超过边缘时就让他等于边缘
                if (loginL < 0) {
                    loginL = 0;
                } else if (loginL > (cwidth - lwidth)) {
                    loginL = cwidth - lwidth;
                }
                if (loginT < 0) {
                    loginT = 0;
                } else if (loginT > (cheight - lheight)) {
                    loginT = cheight - lheight;
                }
                login.style.left = loginL + "px";
                login.style.top = loginT + "px";
            }
        }
        //鼠标弹起时设置为不可拖拽
    document.onmouseup = function() {
            isDraging = false;
        }
        //点击X关闭登录框和弹出层
    var close = login.getElementsByClassName("close")[0];
    close.onclick = function() {
        document.body.removeChild(mask);
        document.body.removeChild(login);
    }
}
//点击登录弹出登录框和弹出层
window.onload = function() {
    var btn = document.getElementById("btn");
    btn.onclick = function() {
        b();
    }
}