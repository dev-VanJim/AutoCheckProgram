package com.autocheckprogram;

import static com.autocheckprogram.MainActivity.MI_GAMING_COMMUNITY_PACKAGE;
import static com.autocheckprogram.MainActivity.QQ_PACKAGE;
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

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoCheckService extends AccessibilityService {

    private static final String TAG = "AccessibilityEventTest";
    // 米游社未成年人模式弹窗类名
    private static final String MI_GAMING_COMMUNITY_MINOR_TIP = "com.mihoyo.hyperion.teenage.ui.a";

    private boolean isCheckPage = false;

    private final static Pattern pattern = Pattern.compile("\\d+");

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        
        if (!MainActivity.isChecking) return;

        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo == null) return;

        CharSequence eventClassName = event.getClassName();
        if (eventClassName == null) return;

        if (MI_GAMING_COMMUNITY_MINOR_TIP.contentEquals(eventClassName)) {
            AccessibilityNodeInfo iKnewNode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
                iKnewNode = nodeInfo.findAccessibilityNodeInfosByText("我知道了").getFirst();
            else
                iKnewNode = nodeInfo.findAccessibilityNodeInfosByText("我知道了").get(0);

            boolean clickResult = iKnewNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (!clickResult) return;
        }

//        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED)
//            Log.d("AccessibilityEventTest:", String.valueOf(nodeInfo));

//        String entireViewId = nodeInfo.getViewIdResourceName();
//        CharSequence text = nodeInfo.getText();
//        CharSequence description = nodeInfo.getContentDescription();
//        String viewId = null;
//        String textToString = null;
//        String descriptionToString = null;
//
//        if (entireViewId != null) viewId = getLastSubstring(entireViewId, ':');
//        String viewName = getLastSubstring(nodeInfo.getClassName().toString(), '.');
//        if (text != null) textToString = text.toString();
//        if (description != null) descriptionToString = description.toString();
//
//        Log.d(TAG, "[" + viewId + "|" + viewName + "|" + textToString + "|" + descriptionToString + "]");
//        Log.d(TAG, nodeInfo.toString());
//
//        Log.d(TAG, getViewTree(nodeInfo).toString());

//        Log.d(TAG, event.getClassName().toString());

        String packageName = event.getPackageName().toString();

        if (MainActivity.MI_GAMING_COMMUNITY_PACKAGE.equals(packageName)) {   // 待优化，写入资源文件中

            SystemClock.sleep(2000);

            if (eventClassName == null) return;

            if (!isCheckPage && "com.mihoyo.hyperion.web2.MiHoYoWebActivity".contentEquals(eventClassName))
                isCheckPage = true;

            if (isCheckPage && "om.mihoyo.hyperion.main.HyperionMainActivity".contentEquals(eventClassName))
                isCheckPage = false;

            toClickTargetViewByText(nodeInfo, "签到福利");

            if (!isCheckPage) return;

            if (findContainTargetTextNode(nodeInfo, "请在此绑定你的游戏账号") != null)
                return;

            AccessibilityNodeInfo targetTextNode = findContainTargetTextNode(nodeInfo, "月已累计签到");
            if (targetTextNode == null) return;

            Matcher matcher = pattern.matcher(targetTextNode.getText());

            String stringCheckedDay = null;
            if (matcher.find() && matcher.find()) {
                stringCheckedDay = matcher.group();
//                Log.d("AccessibilityEventTest:", checkedDay);
            }

            if (stringCheckedDay == null) {
                Log.d(TAG, "签到天数获取失败！");
                return;
            }

            int dayOfTheMonth;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                dayOfTheMonth = LocalDate.now().getDayOfMonth();
            else
                dayOfTheMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

            int checkedDay = Integer.parseInt(stringCheckedDay);
            if (checkedDay == dayOfTheMonth) {
                Log.d(TAG, "今日已签到！");
                MainActivity.isChecking = false;
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
            if (targetTextNode == null) return;

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
                            MainActivity.isChecking = false;
                            Log.d(TAG, "执行成功！");
                        }

                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            super.onCancelled(gestureDescription);
                            Log.d(TAG, "取消执行！");
                        }
                    },
                    null);
            Log.d(TAG, getViewTree(nodeInfo).toString());

//            Log.d(TAG, getViewTree(nodeInfo).toString());

//            nodeInfo = nodeInfo.getChild(0).getChild(0).getChild(0).getChild(1).getChild(0).getChild(2).getChild(5).getChild(2);

//            Log.d(TAG, getViewTree(nodeInfo).toString());

//
//            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
//                Log.d(TAG, "WINDOW_CONTENT_CHANGED");
//            else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
//                Log.d(TAG, "TYPE_WINDOW_STATE_CHANGED");
//            else
//                Log.d(TAG, "其他类型");
//
//            Log.d(TAG, eventClassName.toString());
//            Log.d(TAG, getViewTree(nodeInfo).toString());

//            do {
//                Log.d(TAG, String.valueOf(nodeInfo.getText()));
//                nodeInfo = nodeInfo.getChild(0);
//            } while (nodeInfo != null);
//            Log.d(TAG, "end");

        } else if (MainActivity.QQ_PACKAGE.equals(packageName)) {

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
            if (beFoundText.equals(targetText))
                return nodeInfo;
            else if (beFoundText.toString().contains(targetText))
                return nodeInfo;
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

    private void toClickTargetViewById(AccessibilityNodeInfo beFoundView, String id) {
        List<AccessibilityNodeInfo> viewList = beFoundView.findAccessibilityNodeInfosByViewId(id);

        toClickTargetView(viewList);
    }

    private void toClickTargetViewByText(AccessibilityNodeInfo beFoundView, String text) {
        List<AccessibilityNodeInfo> viewList = beFoundView.findAccessibilityNodeInfosByText(text);

        toClickTargetView(viewList);
    }

    private void toClickTargetView(List<AccessibilityNodeInfo> targetViews) {

        if (targetViews.isEmpty()) {
            Log.d(TAG, "签到失败！");
            return;
        }

        AccessibilityNodeInfo targetView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
            // getFirst()方法要求SDK版本大于35（Android 15）
            targetView = targetViews.getFirst();
        else
            targetView = targetViews.get(1);

        // 点击父控件逻辑待优化，可尝试两层父控件
        if (targetView.isClickable())
            targetView.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        else
            targetView.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
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
            if (childViewTree != null)
                viewTree.append(childViewTree);
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
        if (entireViewId != null)
            viewId = getLastSubstring(entireViewId, ':');

        // 拼接出一个View节点的基本信息，例如：
        // [n层|控件名|id|text内容|对控件的描述信息|是否可点击]
        // 实际效果：
        // [1层|Button|test_button|测试按钮|一个用于测试的按钮|true]
        StringBuilder viewTree = new StringBuilder();
        viewTree.append(" --> [")
                .append(layer)
                .append("层|")
                .append(viewName)
                .append("|")
                .append(viewId)
                .append("|")
                .append(nodeInfo.getText())
                .append("|")
                .append(nodeInfo.getContentDescription())
                .append("|")
                .append(nodeInfo.isClickable())
                .append("]\n");

        // 为不同层次节点前添加制表符（即“\t”符号），能优化显示效果
        for (int i = 0; i < layer; i++) {
            viewTree.insert(0, "\t");
        }

        return viewTree;
    }
}