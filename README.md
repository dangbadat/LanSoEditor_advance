## LanSoEditor_advance android  video editor  advance sdk . filter, overlay,remark,mixer and so on安卓平台视频编辑专业版本，基于【容器图层】架构， 类似UI布局中的各种控件和Layout的关系，类似Photoshop中的图层思路，搭积木一样的编辑您个性化d 视频。
## 版本是2.7.8
*  1,增加粒子的炫酷演示.
*  2,增加图层的Animation动画类,有MoveAnimation,ScaleAnimation,RotateAnimation,AlphaAnimation;
*  3,增加图层布局, 两个视频布局,滤镜预览,手动滑动等各种客户要求的一些activity演示.
*  4,增加opencv的人脸检测apk

[更多版本日志](https://github.com/LanSoSdk/LanSoEditor_advance/blob/master/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E.md)

## 蓝松SDK,您或许应该知道:
* 1,  我们提供的是架构,不是功能. 
			比喻则是: 架构是给您很多画笔, 你自己作画;    而功能则是直接给你一副画,您拷贝过来就可以了.

* 2, 用我们SDK, 还需要您开发一两周的时间, 当然思路和UI开发完全一致. 一样的用容器和控件图层.
* 3, 如果您对项目有独立的见解, 创新意识,想做各种创意的视频画面,不想千篇一律,  想做爆款, 则用我们的就对了, 因为我们SDK可以任意个性化.
* 4, 我们提供QQ对QQ的技术支持, 90%的问题24小时内可以解决. 
* 5, 我们的方向是做手机版的AE, 类似美工用的AE一样,当然后期也会直接兼容AE文件.
* 6, 我们SDK大概三周更新一次到github上, 但在合作后,您的小细节定制, 当天就可以发给您.
* 7, 如果您是多年开发人员或CTO,我们很乐意您去做性价比测试.


## 核心架构
*   图层和容器.  图层是所有素材的集合, 容器是处理的线程; 所有的画面分成一层一层的来处。
*   容器：用来处理各种素材的线程，分为 [容器前台线程] 和 [容器后台线程]。
*   图层： 所有素材的载体。对视频编辑，则把向容器里增加一个视频层；想在上面放图片，就再放一个图片图层； 想放效果视频，则放一个mv层；想增加滤镜则每个图层都支持滤镜实时切换。
*   和UI布局的对比： UI布局是这样的：先增加一个layout，然后放按钮、文本、图片等；则我们SDK则是：先设置DrawPad，然后放视频层，图片层，文字层等；

## 当前具有的图层种类有(11种):
*  视频图层     VideoLayer
*  摄像头图层   CameraLayer
*  图片图层     BitmapLayer
*  MV图层       MVLayer
*  UI图层       ViewLayer
*  Canvas图层   CanvasLayer
*  Data图层     DataLayer
*  Gif图层      GifLayer
*  YUV图层      YUVLayer
*  双视频图层   TwoVideoLayer
*  纹理图层     TextureLayer
*  注1：可多种图层混合叠加，也可以同时增加多个相同类型的图层。 如VideoLayer+BitmapLayer+ViewLayer或多个BitmapLayer叠加。
*  注2：可关于每个图层的功能,可联系我们,索取更多技术文档.
			
## 三步调用（3 Step）
*	1， 创建一个容器(DrawPad容器):  设置容器的宽度和高度,刷新率,码率,设置进度监听,结束监听 各种Listener等
* 2,  开启这个容器,开启后,增加各种图层来实现你的处理效果, 并在处理监听中,来调节图层的各种变化,从而有各种效果.
* 3,  结束容器的执行.
   

## 更仅一步说:
*	1， 您想给视频增加滤镜，则可以在开启Drawpad后,增加一个视频图层,并给图层设置滤镜效果即可. 也可以设置压缩,缩放或其他功能.
* 2,  您想给视频增加图片,文字. 则可以用 视频图层+Canvas图层+ bitmap图层,三个叠加一起,即可完成.当然也可以在指定位置,指定时间增加,也可以增加后旋转移动缩放等操作.
* 3, 您想把多种图层合成视频. 则可以 增加多个 [图片图层], 可以设置图片的各种飞入飞出,旋转,移动等效果.
*	4，你用 【UI图层  ViewLayer或CanvasLayer】在容器上作画， 就是把精美的UI界面转换为视频， 当然我们的设计，也可以后台处理。
* 5， 你用  【视频图层】 + 【MV图层】 在容器上作画， 就是在视频中叠加MV的效果。
* 6， 你用  【视频图层】 + 【Gif图层】 在容器上作画， 就是在视频中叠加Gif动画的效果。
* 7， 我们针对每个图层都做了举例，您可以在我们Demo中找到；
* 8， 可以在前台工作， 也可以在后台处理。
* 9， 此SDK采用为收费授权,公司性质的合作,为了您项目更好的进行,欢迎和我们联系.谢谢!


## 下载地址: 
*  https://github.com/LanSoSdk/LanSoEditor_advance

## 我们有基本视频编辑,以方便您项目中基本需求：
*	https://github.com/LanSoSdk/LanSoEditor_common

## 我们的IOS版本， 欢迎您的使用：
*	https://github.com/LanSoSdk/LanSongEditor_IOS

## 联系方式:
*   QQ 1852600324 
*   Email:support@lansongtech.com
*   网址: www.lansongtech.com
*  如果您下载过慢, 可联系我们, 索取最新的工程包 或向我们索取演示APK安装包.

## 使用案例
*   我们从事的是：商业SDK开发、更新和维护；
*   当前包括500强 大公司在内的大约90多个上线APP在使用，行业涉及 社交、微商、广场舞、直播、工具、母婴、舞蹈、厨艺、金融、炫酷等多种行业
*   欢迎联系我们，索取相关案例信息和授权说明
