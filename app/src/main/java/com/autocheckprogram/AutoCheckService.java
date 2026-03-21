package com.autocheckprogram;

import static com.autocheckprogram.utils.StringUtils.getLastSubstring;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.autocheckprogram.enums.PageView;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoCheckService extends AccessibilityService {

    private static final String TAG = "AccessibilityEventTest";


    // 《米游社》包名
    private static final String MI_GAMING_COMMUNITY_PACKAGE = "com.mihoyo.hyperion";
    // QQ包名
    private static final String QQ_PACKAGE = "com.tencent.mobileqq";


    // 《米游社》广告页面
    private static final String AD_PAGE = "com.mihoyo.hyperion.splash.SplashActivity";
    // 《米游社》主页
    private static final String HOME_PAGE = "com.mihoyo.hyperion.main.HyperionMainActivity";
    // 《米游社》签到页面
    private static final String CHECK_PAGE = "com.mihoyo.hyperion.web2.MiHoYoWebActivity";

    // 《米游社》未成年人模式弹窗
    private static final String MI_GAMING_COMMUNITY_MINOR_TIP_POPUP = "com.mihoyo.hyperion.teenage.ui.a";
    // 《米游社》活动弹窗
    private static final String MI_GAMING_COMMUNITY_HOME_POPUP = "com.mihoyo.hyperion.main.popup.HomePopupDialogActivity";


    // 当前页面
    public static PageView nowPage = PageView.HOME_PAGE;  // 是否需要加锁

    // 正则表达式，表示一个或多个数字
    private final static Pattern pattern = Pattern.compile("\\d+");

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.d("AccessibilityEventTest:", "进入！");
//        Log.d("AccessibilityEventTest:", String.valueOf(event));
//
//        AccessibilityNodeInfo testNodeInfo = event.getSource();
//        if (testNodeInfo == null) return;
//
//        Log.d("AccessibilityEventTest:", String.valueOf(getViewTree(testNodeInfo)));

        // 自动签到程序未执行，跳过事件捕捉
        if (!MainActivity.isChecking) return;

        AccessibilityNodeInfo nodeInfo = event.getSource();
        Log.d(TAG, "nodeInfo：" + nodeInfo);
        if (nodeInfo == null) return;

        CharSequence eventClassName = event.getClassName();
        if (eventClassName == null) return;

        String pagePackageName = event.getPackageName().toString();

        if (MI_GAMING_COMMUNITY_PACKAGE.equals(pagePackageName)) {


            if (nowPage != PageView.HOME_PAGE && HOME_PAGE.contentEquals(eventClassName))
                nowPage = PageView.HOME_PAGE;

            else if (nowPage != PageView.CHECK_PAGE && CHECK_PAGE.contentEquals(eventClassName))
                nowPage = PageView.CHECK_PAGE;

            else if (nowPage != PageView.AD_PAGE && AD_PAGE.contentEquals(eventClassName))
                nowPage = PageView.AD_PAGE;

            Log.d(TAG, String.valueOf(nowPage));

            // 如果正在《《米游社》》主页
            if (nowPage == PageView.HOME_PAGE) {
                // 未成年人保护弹窗
                if (MI_GAMING_COMMUNITY_MINOR_TIP_POPUP.contentEquals(eventClassName)) {
                    // 记录点击事件结果
                    boolean clickResult = toClickTargetViewByText(nodeInfo, "我知道了");

                    if (!clickResult) return;
                }

                toClickTargetViewByText(nodeInfo, "签到福利");

            } else if (nowPage == PageView.CHECK_PAGE) {
                // 说明当前签到页面尚在加载 ↓
                if (findContainTargetTextNode(nodeInfo, "请在此绑定你的游戏账号") != null)
                    return;

                AccessibilityNodeInfo targetTextNode = findContainTargetTextNode(nodeInfo, "月已累计签到");
                Log.d(TAG, String.valueOf(targetTextNode));
                // 说明当前签到页面尚在加载
                if (targetTextNode == null) return;

                Matcher matcher = pattern.matcher(targetTextNode.getText());

                // 已签到天数
                String stringCheckedDay = null;
                if (matcher.find() && matcher.find()) {
                    stringCheckedDay = matcher.group();
                    Log.d(TAG, "签到天数：" + stringCheckedDay);
                }

                if (stringCheckedDay == null) {
//                    Log.d(TAG, "签到天数获取失败！");
                    return;
                }

                int dayOfThisMonth;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    dayOfThisMonth = LocalDate.now().getDayOfMonth();
                else
                    dayOfThisMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

                // 将已签到天数转为数字，方便比较
                int checkedDay = Integer.parseInt(stringCheckedDay);
                if (checkedDay == dayOfThisMonth) {
                    // 已签到天数等于今天日期，表示今日已签到
//                    Log.d(TAG, "今日已签到！");
                    MainActivity.isChecking = false;

                    // 三连返回
                    for (int i = 0; i < 3; i++) {
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                        SystemClock.sleep(500);
                    }
                    return;
                }

                // 漏签的业务逻辑还需要完善！！！
//            if (checkedDay == 0 && dayOfTheMonth != 1)
//                return;

                for (int i = 0; i < 3; i++) {
                    targetTextNode = targetTextNode.getParent();
                }

                targetTextNode = findContainTargetTextNode(targetTextNode, "第" + (checkedDay + 1) + "天");
                Log.d(TAG, "签到节点：" + targetTextNode);
                if (targetTextNode == null) return;

                // 点击目标位置业务
                Rect rect = new Rect();
                targetTextNode.getBoundsInScreen(rect);

                Path path = new Path();
                path.moveTo(rect.centerX(), rect.centerY());

                GestureDescription.Builder builder = new GestureDescription.Builder();
                GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 50);
                GestureDescription gestureDescription = builder.addStroke(stroke).build();
                dispatchGesture(gestureDescription,
                        new GestureResultCallback() {
                            @Override
                            public void onCompleted(GestureDescription gestureDescription) {
                                super.onCompleted(gestureDescription);
                                Log.d(TAG, "执行成功！");
                            }

                            @Override
                            public void onCancelled(GestureDescription gestureDescription) {
                                super.onCancelled(gestureDescription);
                                Log.d(TAG, "取消执行！");
                            }
                        },
                        null);
            }
        } else if (QQ_PACKAGE.equals(pagePackageName)) {

            // QQ签到功能将在下个版本推出

//            toClickTargetViewById(nodeInfo, "com.tencent.mobileqq:id/ba1");
//            toClickTargetViewByText(nodeInfo, "打卡");

//            CharSequence className = event.getClassName();
//            Log.d(TAG, event.toString());
//            if (className == null) return;

//            Rect rect;

//            if ("com.tencent.mobileqq.activity.QQBrowserActivity".contentEquals(className)) {
//                Log.d(TAG, nodeInfo.toString());

//                nodeInfo = nodeInfo.getChild(0).getChild(0).getChild(0).getChild(7);

//                rect = new Rect();
//                nodeInfo.getBoundsInScreen(rect);
//
//                Log.d(TAG, nodeInfo.toString());

//                Path path = new Path();
//                path.moveTo(rect.centerX(), rect.centerY());
//
//                GestureDescription.Builder builder = new GestureDescription.Builder();
//                GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 50);
//                GestureDescription gestureDescription = builder.addStroke(stroke).build();
//                dispatchGesture(gestureDescription,
//                        new GestureResultCallback() {
//                            @Override
//                            public void onCompleted(GestureDescription gestureDescription) {
//                                super.onCompleted(gestureDescription);
//                                Log.d(TAG, "执行成功！");
//                            }
//
//                            @Override
//                            public void onCancelled(GestureDescription gestureDescription) {
//                                super.onCancelled(gestureDescription);
//                                Log.d(TAG, "取消执行！");
//                            }
//                        },
//                        null);
//            }

//            List<AccessibilityNodeInfo> viewList = nodeInfo.findAccessibilityNodeInfosByText("日签卡");
//            Log.d("AccessibilityEventTest:", String.valueOf(viewList.size()));
        }

//        List<AccessibilityNodeInfo> testList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/ba1");
//        if (testList != null)
//            Log.d("AccessibilityEventTest:", String.valueOf(testList.size()));
    }

    private AccessibilityNodeInfo findContainTargetTextNode(AccessibilityNodeInfo nodeInfo, String targetText) {
        if (nodeInfo == null) return null;

        CharSequence beFoundText = nodeInfo.getText();
        if (beFoundText != null) {
            if (beFoundText.equals(targetText)) return nodeInfo;
            else if (beFoundText.toString().contains(targetText)) return nodeInfo;
        }

        int childCount = nodeInfo.getChildCount();

        AccessibilityNodeInfo result = null;
        for (int i = 0; i < childCount; i++) {
            result = findContainTargetTextNode(nodeInfo.getChild(i), targetText);
            if (result != null) break;
        }

        return result;
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "自动签到服务运行中止！");
    }

    private boolean toClickTargetViewById(AccessibilityNodeInfo beFoundView, String id) {
        List<AccessibilityNodeInfo> viewList = beFoundView.findAccessibilityNodeInfosByViewId(id);

        if (viewList.isEmpty()) return false;

        AccessibilityNodeInfo targetView = toFindTargetView(viewList);

        return toClickTargetView(targetView);
    }

    private boolean toClickTargetViewByText(AccessibilityNodeInfo beFoundView, String text) {
        List<AccessibilityNodeInfo> viewList = beFoundView.findAccessibilityNodeInfosByText(text);

        if (viewList.isEmpty()) return false;

        AccessibilityNodeInfo targetView = toFindTargetView(viewList);
        return toClickTargetView(targetView);
    }

    private AccessibilityNodeInfo toFindTargetView(List<AccessibilityNodeInfo> targetViews) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
            // getFirst()方法要求SDK版本大于35（Android 15）
            return targetViews.getFirst();
        else
            return targetViews.get(1);
    }

    private boolean toClickTargetView(AccessibilityNodeInfo targetView) {
        // 如果该控件不可点击，尝试点击其父控件
        if (!targetView.isClickable()) {
            targetView = targetView.getParent();
            if (targetView == null) return false;
        }

        // 如果该控件可点击，就直接点击
        return targetView.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    /**
     * 获取到一个布局层次树状图。
     *
     * @param nodeInfo 需要查询布局层次树状图的View的AccessibilityNodeInfo对象。
     * @return 遍历后拼接成的布局层次树状图。
     */
    private StringBuilder getViewTree(AccessibilityNodeInfo nodeInfo) {
        // 在得到的布局层次树图前加上一个换行符（即“\n”符号），显示效果更好
        return getViewTree(nodeInfo, 0).insert(0, "层次结构：\n");
    }

    /**
     * 获取到一个从layer层开始的布局层次树状图。
     *
     * @param nodeInfo 需要查询布局层次树状图的View的AccessibilityNodeInfo对象。
     * @param layer    该View对象所在层。
     * @return 遍历后拼接成的布局层次树状图。
     */
    private StringBuilder getViewTree(AccessibilityNodeInfo nodeInfo, int layer) {
        if (nodeInfo == null) return null;

        StringBuilder viewTree = appendNodeBaseInfo(nodeInfo, layer);

        int childCount = nodeInfo.getChildCount();

        StringBuilder childViewTree;
        for (int i = 0; i < childCount; i++) {
            childViewTree = getViewTree(nodeInfo.getChild(i), layer + 1);
            if (childViewTree != null) viewTree.append(childViewTree);
        }

        return viewTree;
    }

    /**
     * 拼接出一个View节点的基本信息，例如：
     * * [n层|控件名|id|text内容|对控件的描述信息|是否可点击]
     * * 实际效果：
     * * [1层|Button|test_button|测试按钮|一个用于测试的按钮|true]
     *
     * @param nodeInfo 待拼接信息View的AccessibilityNodeInfo对象。
     * @param layer    该View对象所在层。
     * @return 节点基本信息。
     */
    private StringBuilder appendNodeBaseInfo(AccessibilityNodeInfo nodeInfo, int layer) {

        String viewName = getLastSubstring(nodeInfo.getClassName().toString(), '.');
//        String viewName = nodeInfo.getClassName().toString();

        String viewId = null;
        String entireViewId = nodeInfo.getViewIdResourceName();
        if (entireViewId != null) viewId = getLastSubstring(entireViewId, ':');

        // 拼接出一个View节点的基本信息，例如：
        // [n层|控件名|id|text内容|对控件的描述信息|是否可点击]
        // 实际效果：
        // [1层|Button|test_button|测试按钮|一个用于测试的按钮|true]
        StringBuilder viewTree = new StringBuilder();
        viewTree.append(" --> [").append(layer).append("层|").append(viewName).append("|").append(viewId).append("|").append(nodeInfo.getText()).append("|").append(nodeInfo.getContentDescription()).append("|").append(nodeInfo.isClickable()).append("]\n");

        // 为不同层次节点前添加制表符（即“\t”符号），能优化显示效果
        for (int i = 0; i < layer; i++) {
            viewTree.insert(0, "\t");
        }

        return viewTree;
    }
}