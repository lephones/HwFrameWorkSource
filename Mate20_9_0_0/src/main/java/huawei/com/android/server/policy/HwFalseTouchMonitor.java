package huawei.com.android.server.policy;

import android.hardware.display.DisplayManagerGlobal;
import android.os.SystemProperties;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.SparseArray;
import android.view.DisplayInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants.PointerEventListener;
import com.android.server.gesture.GestureNavConst;
import com.android.server.policy.WindowManagerPolicy.WindowState;

public class HwFalseTouchMonitor {
    private static final int EDGE_WIDTH = 100;
    private static final int FALSE_TOUCH_REPORTE_CODE = 907400020;
    private static final float FLOAT_PRECISION = 1.0E-7f;
    private static final int MAX_RECORDS = 200;
    private static final float PI = 3.1415927f;
    private static final long QUICK_QUIT_WINDOW_TIMEOUT = 800;
    public static final int STATE_HAPPENED = 1;
    public static final int STATE_NOT_HAPPENED = 2;
    public static final int STATE_UNKOWN = 0;
    private static final long STATISTIC_PERIOD = 3600000;
    private static final String TAG = "HwFalseTouchMonitor";
    private static final long TAP_RESPOND_MAX_TIME = 500;
    private static final long TAP_RESPOND_MIN_TIME = 50;
    private static final int TREMBLE_MOVE_RANGE = 20;
    private static final int TYPE_CHINA_BETA = 3;
    private static final int TYPE_NO_EFFECT_CLICK = 2;
    private static final int TYPE_QUICK_QUIT = 1;
    private static final int TYPE_TREMBLE = 3;
    private static final int WINDOW_NAME_MAX_LEN = 64;
    private DisplayInfo mDisplayInfo = new DisplayInfo();
    private boolean mEnabled = false;
    private MotionEventListener mEventListener = null;
    private boolean mFocusedChecked = false;
    private long mFocusedTime = 0;
    private WindowState mFocusedWindow = null;
    private MonitorPoint mLastUpPoint = new MonitorPoint();
    private NoEffectClickChecker mNECChecker = new NoEffectClickChecker();
    private long mStatisticCount = 0;
    private Object mStatisticLock = new Object();
    private long mStatisticStartTime = 0;
    private SparseArray<MonitorPoint> mTouchingPoints = new SparseArray();
    private TrembleChecker mTrembleChecker = new TrembleChecker();

    public class FalseTouchChecker {
        public int state = 0;

        public boolean reportData() {
            if (this.state == 1 && HwFalseTouchMonitor.this.checkStatistic()) {
                return true;
            }
            return false;
        }

        public boolean processActionDown(MonitorPoint point) {
            reset();
            String str;
            StringBuilder stringBuilder;
            if (HwFalseTouchMonitor.this.edgePoint(point)) {
                str = HwFalseTouchMonitor.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append(point);
                stringBuilder.append(" nearby the edge, keep checking it");
                Log.d(str, stringBuilder.toString());
                return true;
            }
            str = HwFalseTouchMonitor.TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append(point);
            stringBuilder.append(" faraway from the edge, ignore it");
            Log.d(str, stringBuilder.toString());
            this.state = 2;
            return false;
        }

        public void reset() {
        }

        public void setState(int state_) {
            this.state = state_;
        }
    }

    public static class MonitorPoint {
        public int id = -1;
        public int orientation = 0;
        public long time = 0;
        public float x = -1.0f;
        public float y = -1.0f;

        public MonitorPoint(int id_, float x_, float y_, int orientation_, long time_) {
            this.id = id_;
            this.x = x_;
            this.y = y_;
            this.orientation = orientation_;
            this.time = time_;
        }

        public void set(int id_, float x_, float y_, int orientation_, long time_) {
            this.id = id_;
            this.x = x_;
            this.y = y_;
            this.orientation = orientation_;
            this.time = time_;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("point(");
            stringBuilder.append(this.x);
            stringBuilder.append(",");
            stringBuilder.append(this.y);
            stringBuilder.append("), orientation:");
            stringBuilder.append(this.orientation);
            return stringBuilder.toString();
        }
    }

    public class MotionEventListener implements PointerEventListener {
        public void onPointerEvent(MotionEvent motionEvent) {
            HwFalseTouchMonitor.this.handleMotionEvent(motionEvent);
        }
    }

    public class NoEffectClickChecker extends FalseTouchChecker {
        public static final int CLICK_INTERVAL_TIMEOUT = 1000;
        public static final int CLICK_TIME = 100;
        public static final int NEARBY_CHECK_RANGE = 20;
        public MonitorPoint centralClickPoint = null;
        public MonitorPoint lastPointerDown = new MonitorPoint();
        private WindowState mClickDownWindow = null;
        public MonitorPoint nearbyEdgePoint = null;
        public int repeatClickCount = 0;

        public NoEffectClickChecker() {
            super();
        }

        /* JADX WARNING: Missing block: B:11:0x009e, code skipped:
            return false;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean reportData() {
            if (this.state != 1 || !HwFalseTouchMonitor.this.checkStatistic() || this.nearbyEdgePoint == null || this.centralClickPoint == null) {
                return false;
            }
            EventStream eStream = IMonitor.openEventStream(HwFalseTouchMonitor.FALSE_TOUCH_REPORTE_CODE);
            eStream.setParam((short) 0, 2);
            String edgePoint = new StringBuilder();
            edgePoint.append(this.nearbyEdgePoint.x);
            edgePoint.append(",");
            edgePoint.append(this.nearbyEdgePoint.y);
            edgePoint = edgePoint.toString();
            String centerPoint = new StringBuilder();
            centerPoint.append(this.centralClickPoint.x);
            centerPoint.append(",");
            centerPoint.append(this.centralClickPoint.y);
            centerPoint = centerPoint.toString();
            eStream.setParam((short) 2, edgePoint);
            eStream.setParam((short) 3, centerPoint);
            IMonitor.sendEvent(eStream);
            IMonitor.closeEventStream(eStream);
            String str = HwFalseTouchMonitor.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("NoEffectClickChecker, report data nearbyEdgePoint:");
            stringBuilder.append(this.nearbyEdgePoint);
            stringBuilder.append(",centralClickPoint:");
            stringBuilder.append(this.centralClickPoint);
            stringBuilder.append(",falseTouch window:");
            stringBuilder.append(HwFalseTouchMonitor.this.getWindowTitle(this.mClickDownWindow));
            Log.i(str, stringBuilder.toString());
            return true;
        }

        public void reset() {
            this.state = 0;
            this.centralClickPoint = null;
            this.nearbyEdgePoint = null;
            this.repeatClickCount = 0;
            this.lastPointerDown = null;
        }

        public boolean processActionDown(MonitorPoint point) {
            if (!super.processActionDown(point)) {
                return false;
            }
            this.nearbyEdgePoint = point;
            this.mClickDownWindow = HwFalseTouchMonitor.this.mFocusedWindow;
            return true;
        }

        public void processPointerDown(MotionEvent event) {
            MotionEvent motionEvent = event;
            if (this.state == 0) {
                if (HwFalseTouchMonitor.this.mTouchingPoints.size() > 2) {
                    Log.d(HwFalseTouchMonitor.TAG, "more than one point touch down, no longer check no-effect click");
                    HwFalseTouchMonitor.this.mNECChecker.setState(2);
                    return;
                }
                long interval = 0;
                if (this.lastPointerDown != null) {
                    interval = event.getEventTime() - this.lastPointerDown.time;
                }
                if (interval == 0 || interval <= 1000) {
                    float offsetX = event.getRawX() - event.getX();
                    float offsetY = event.getRawY() - event.getY();
                    int actionIndex = event.getActionIndex();
                    float x = motionEvent.getX(actionIndex) + offsetX;
                    float y = motionEvent.getY(actionIndex) + offsetY;
                    long time = event.getEventTime();
                    int id = motionEvent.getPointerId(actionIndex);
                    int orientation = HwFalseTouchMonitor.this.transformOrientation(event.getOrientation());
                    String str = HwFalseTouchMonitor.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("NoEffectClickChecker processPointerDown check the no-primary point[");
                    stringBuilder.append(actionIndex);
                    stringBuilder.append("]:x=");
                    stringBuilder.append(x);
                    stringBuilder.append(",y=");
                    stringBuilder.append(y);
                    stringBuilder.append(",id=");
                    stringBuilder.append(id);
                    Log.d(str, stringBuilder.toString());
                    if (HwFalseTouchMonitor.this.mTouchingPoints.get(id) == null) {
                        int id2 = id;
                        MonitorPoint curPoint = new MonitorPoint(id, x, y, orientation, time);
                        HwFalseTouchMonitor.this.mTouchingPoints.put(id2, curPoint);
                        this.lastPointerDown = curPoint;
                        if (HwFalseTouchMonitor.this.edgePoint(curPoint) || this.centralClickPoint != null) {
                            if (this.centralClickPoint == null) {
                                Log.d(HwFalseTouchMonitor.TAG, "NoEffectClickChecker no central point click before");
                            } else if (pointNearby(this.centralClickPoint, curPoint)) {
                                Log.d(HwFalseTouchMonitor.TAG, "NoEffectClickChecker current point click down nearby the last clicked central point");
                            } else {
                                Log.d(HwFalseTouchMonitor.TAG, "NoEffectClickChecker current point click is far from the last clicked central point!");
                                this.state = 2;
                            }
                            return;
                        }
                        this.centralClickPoint = curPoint;
                        Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker found the first central point!");
                        return;
                    }
                    return;
                }
                this.state = 2;
                Log.d(HwFalseTouchMonitor.TAG, "NoEffectClickChecker click time interval exceeds the limits form last click!");
            }
        }

        public void processPointerUp(MotionEvent event) {
            MotionEvent motionEvent = event;
            float offsetX = event.getRawX() - event.getX();
            float offsetY = event.getRawY() - event.getY();
            int actionIndex = event.getActionIndex();
            float x = motionEvent.getX(actionIndex) + offsetX;
            float y = motionEvent.getY(actionIndex) + offsetY;
            long time = event.getEventTime();
            int id = motionEvent.getPointerId(actionIndex);
            HwFalseTouchMonitor.this.mTouchingPoints.remove(id);
            if (this.state == 0) {
                String str = HwFalseTouchMonitor.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("NoEffectClickChecker processPointerUp point[");
                stringBuilder.append(actionIndex);
                stringBuilder.append("]:x=");
                stringBuilder.append(x);
                stringBuilder.append(",y=");
                stringBuilder.append(y);
                stringBuilder.append(",id=");
                stringBuilder.append(id);
                Log.d(str, stringBuilder.toString());
                if (this.nearbyEdgePoint == null || this.nearbyEdgePoint.id == id) {
                    this.state = 2;
                    Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker processPointerUp edgePoint is up!");
                } else if (this.lastPointerDown == null || this.lastPointerDown.id != id) {
                    Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker pointer up id not matched");
                } else {
                    if (pointNearby(this.lastPointerDown.x, this.lastPointerDown.y, x, y)) {
                        long clickTime = time - this.lastPointerDown.time;
                        if (clickTime > 0 && clickTime <= 100) {
                            this.repeatClickCount++;
                            str = HwFalseTouchMonitor.TAG;
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("NoEffectClickChecker ,repeatClickCount=");
                            stringBuilder2.append(this.repeatClickCount);
                            Log.i(str, stringBuilder2.toString());
                            if (this.repeatClickCount > 2) {
                                Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker ,repeatClickCount happend");
                                this.state = 1;
                            }
                        }
                    } else {
                        Log.i(HwFalseTouchMonitor.TAG, "NoEffectClickChecker , central point swipe, not an click operation");
                        this.state = 2;
                    }
                }
            }
        }

        private boolean pointNearby(float x1, float y1, float x2, float y2) {
            return Math.abs(x1 - x2) < 20.0f || Math.abs(y1 - y2) < 20.0f;
        }

        private boolean pointNearby(MonitorPoint p1, MonitorPoint p2) {
            return pointNearby(p1.x, p1.y, p2.x, p2.y);
        }
    }

    public class TrembleChecker extends FalseTouchChecker {
        public static final float MOVE_PRECISION = 0.1f;
        public static final int TREMBLE_MIN_COUNT = 10;
        public int aboveCount = 0;
        public int belowCount = 0;
        public int leftCount = 0;
        public MonitorPoint mDownPoint;
        private SparseArray<MonitorPoint> mLastMovePoints = new SparseArray();
        public int rightCount = 0;

        public TrembleChecker() {
            super();
        }

        public boolean reportData() {
            if (this.state == 2 || !HwFalseTouchMonitor.this.checkStatistic()) {
                return false;
            }
            String str = HwFalseTouchMonitor.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("TrembleChecker check rightCount=");
            stringBuilder.append(this.rightCount);
            stringBuilder.append(" leftCount=");
            stringBuilder.append(this.leftCount);
            stringBuilder.append(" belowCount=");
            stringBuilder.append(this.belowCount);
            stringBuilder.append(" aboveCount=");
            stringBuilder.append(this.aboveCount);
            Log.d(str, stringBuilder.toString());
            if ((this.leftCount >= 10 && this.rightCount >= 10) || (this.belowCount >= 10 && this.aboveCount >= 10)) {
                HwFalseTouchMonitor.this.updateStatistic();
                EventStream eStream = IMonitor.openEventStream(HwFalseTouchMonitor.FALSE_TOUCH_REPORTE_CODE);
                eStream.setParam((short) 0, 3);
                String point = new StringBuilder();
                point.append(HwFalseTouchMonitor.this.mLastUpPoint.x);
                point.append(",");
                point.append(HwFalseTouchMonitor.this.mLastUpPoint.y);
                eStream.setParam((short) 2, point.toString());
                IMonitor.sendEvent(eStream);
                IMonitor.closeEventStream(eStream);
                String str2 = HwFalseTouchMonitor.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("TrembleChecker tremble happened, down at");
                stringBuilder.append(this.mDownPoint);
                Log.i(str2, stringBuilder.toString());
            }
            return true;
        }

        public void reset() {
            this.state = 0;
            this.mLastMovePoints.clear();
            this.mDownPoint = null;
            this.aboveCount = 0;
            this.belowCount = 0;
            this.rightCount = 0;
            this.leftCount = 0;
        }

        public boolean processActionDown(MonitorPoint point) {
            if (super.processActionDown(point)) {
                this.mLastMovePoints.put(point.id, point);
                String str = HwFalseTouchMonitor.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("point(");
                stringBuilder.append(point.x);
                stringBuilder.append(",");
                stringBuilder.append(point.y);
                stringBuilder.append(") nearby the edge, add it");
                Log.i(str, stringBuilder.toString());
                this.mDownPoint = new MonitorPoint(point.id, point.x, point.y, point.orientation, point.time);
                return true;
            }
            Log.d(HwFalseTouchMonitor.TAG, "tremble super.processActionDown return");
            return false;
        }

        public void processActionMove(MotionEvent event) {
            MotionEvent motionEvent = event;
            if (this.state == 0) {
                int i;
                if (event.getPointerCount() != 1 || this.mDownPoint == null) {
                    i = 2;
                    Log.d(HwFalseTouchMonitor.TAG, "tremble not happened");
                    this.state = i;
                    return;
                }
                float x = motionEvent.getX(0) + (event.getRawX() - event.getX());
                float y = motionEvent.getY(0) + (event.getRawY() - event.getY());
                int id = motionEvent.getPointerId(0);
                int orientation = HwFalseTouchMonitor.this.transformOrientation(motionEvent.getOrientation(0));
                long eventTime = event.getEventTime();
                float f;
                int i2;
                if (Math.abs(x - this.mDownPoint.x) > 20.0f) {
                    i = 2;
                    f = y;
                    i2 = id;
                } else if (Math.abs(y - this.mDownPoint.y) > 20.0f) {
                    i = 2;
                    f = y;
                    i2 = id;
                } else {
                    MonitorPoint lastPosition = (MonitorPoint) this.mLastMovePoints.get(id);
                    if (lastPosition != null) {
                        String str = HwFalseTouchMonitor.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("change to ");
                        stringBuilder.append(x);
                        stringBuilder.append(",");
                        stringBuilder.append(y);
                        Log.d(str, stringBuilder.toString());
                        if (lastPosition.id == this.mDownPoint.id && HwFalseTouchMonitor.this.pointMoved(lastPosition.x, lastPosition.y, x, y)) {
                            if (x > lastPosition.x) {
                                this.rightCount++;
                            } else if (x < lastPosition.x) {
                                this.leftCount++;
                            }
                            if (y > lastPosition.y) {
                                this.belowCount++;
                            } else if (y < lastPosition.y) {
                                this.aboveCount++;
                            }
                        }
                        lastPosition.x = x;
                        lastPosition.y = y;
                        MonitorPoint monitorPoint = lastPosition;
                        f = y;
                        lastPosition = id;
                    } else {
                        this.mLastMovePoints.put(id, new MonitorPoint(id, x, y, orientation, eventTime));
                    }
                    return;
                }
                this.state = i;
                Log.d(HwFalseTouchMonitor.TAG, "point move out of the tremble range!");
            }
        }
    }

    public MotionEventListener getEventListener() {
        return this.mEventListener;
    }

    public boolean isFalseTouchFeatureOn() {
        return this.mEnabled;
    }

    public HwFalseTouchMonitor() {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        this.mEnabled = z;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HwFalseTouchMonitor enabled?");
        stringBuilder.append(this.mEnabled);
        Log.d(str, stringBuilder.toString());
        if (this.mEnabled) {
            this.mEventListener = new MotionEventListener();
            this.mDisplayInfo = DisplayManagerGlobal.getInstance().getDisplayInfo(0);
            if (this.mDisplayInfo != null) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("DisplayInfo logicalHeight=");
                stringBuilder2.append(this.mDisplayInfo.logicalHeight);
                stringBuilder2.append(",logicalWidth=");
                stringBuilder2.append(this.mDisplayInfo.logicalWidth);
                Log.d(str2, stringBuilder2.toString());
            }
        }
    }

    public void handleFocusChanged(WindowState lastFocus, WindowState newFocus) {
        if (this.mEnabled && newFocus != null) {
            synchronized (this) {
                this.mFocusedTime = System.currentTimeMillis();
                this.mFocusedChecked = false;
                this.mFocusedWindow = newFocus;
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:0x0039, code skipped:
            if (r0 <= 0) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:22:0x003f, code skipped:
            if (r0 >= QUICK_QUIT_WINDOW_TIMEOUT) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:24:0x0045, code skipped:
            if (r2 <= TAP_RESPOND_MIN_TIME) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:26:0x004b, code skipped:
            if (r2 >= 500) goto L_0x005e;
     */
    /* JADX WARNING: Missing block: B:27:0x004d, code skipped:
            android.util.Log.i(TAG, "enter current window and quit it quickly");
     */
    /* JADX WARNING: Missing block: B:28:0x0058, code skipped:
            if (checkStatistic() == false) goto L_0x0085;
     */
    /* JADX WARNING: Missing block: B:29:0x005a, code skipped:
            reportQuickQuitWindow();
     */
    /* JADX WARNING: Missing block: B:30:0x005e, code skipped:
            r4 = TAG;
            r5 = new java.lang.StringBuilder();
            r5.append("handleKeyEvent current window enter ");
            r5.append(r0);
            r5.append("ms ago, last motion up before");
            r5.append(r2);
            r5.append(" ms ago from current window entered");
            android.util.Log.d(r4, r5.toString());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleKeyEvent(KeyEvent key) {
        if (this.mEnabled && key.getKeyCode() == 4 && key.getAction() == 0) {
            synchronized (this) {
                if (this.mFocusedChecked) {
                } else if (edgePoint(this.mLastUpPoint)) {
                    long interval = System.currentTimeMillis() - this.mFocusedTime;
                    long tapRespondTime = this.mFocusedTime - this.mLastUpPoint.time;
                    this.mFocusedChecked = true;
                }
            }
        }
    }

    private void reportQuickQuitWindow() {
        EventStream eStream = IMonitor.openEventStream(FALSE_TOUCH_REPORTE_CODE);
        eStream.setParam((short) 0, 1);
        String windowName = getWindowTitle(this.mFocusedWindow);
        String point = new StringBuilder();
        point.append(this.mLastUpPoint.x);
        point.append(",");
        point.append(this.mLastUpPoint.y);
        point = point.toString();
        if (windowName.length() > 64) {
            windowName = windowName.substring(0, 64);
        }
        eStream.setParam((short) 1, windowName);
        eStream.setParam((short) 2, point);
        IMonitor.sendEvent(eStream);
        IMonitor.closeEventStream(eStream);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("reportQuickQuitWindow point(");
        stringBuilder.append(this.mLastUpPoint.x);
        stringBuilder.append(",");
        stringBuilder.append(this.mLastUpPoint.y);
        stringBuilder.append(") click up, orientation=");
        stringBuilder.append(this.mLastUpPoint.orientation);
        stringBuilder.append(",window:");
        stringBuilder.append(getWindowTitle(this.mFocusedWindow));
        Log.i(str, stringBuilder.toString());
        updateStatistic();
    }

    private void handleMotionEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case 0:
                handleActionDown(event);
                return;
            case 1:
                handleActionUp(event);
                return;
            case 2:
                handleActionMove(event);
                return;
            case 5:
                handlePointerDown(event);
                return;
            case 6:
                handlePointerUp(event);
                return;
            default:
                return;
        }
    }

    private void handleActionDown(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        long time = event.getEventTime();
        int id = event.getPointerId(event.getActionIndex());
        MonitorPoint downPoint = new MonitorPoint(id, x, y, transformOrientation(event.getOrientation()), time);
        this.mTouchingPoints.put(id, downPoint);
        this.mTrembleChecker.processActionDown(downPoint);
        this.mNECChecker.processActionDown(downPoint);
    }

    private void handleActionUp(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        long time = System.currentTimeMillis();
        int id = event.getPointerId(event.getActionIndex());
        int orientation = transformOrientation(event.getOrientation());
        this.mTouchingPoints.clear();
        this.mLastUpPoint.set(id, x, y, orientation, time);
        this.mTrembleChecker.reportData();
        this.mNECChecker.reportData();
    }

    private boolean pointMoved(float x1, float x2, float y1, float y2) {
        return Math.abs(x1 - x2) > 0.1f || Math.abs(y1 - y2) > 0.1f;
    }

    private void handleActionMove(MotionEvent event) {
        this.mTrembleChecker.processActionMove(event);
    }

    private void handlePointerDown(MotionEvent event) {
        Log.d(TAG, "more than one point touch down, no longer check tremble");
        this.mTrembleChecker.setState(2);
        this.mNECChecker.processPointerDown(event);
    }

    private void handlePointerUp(MotionEvent event) {
        this.mNECChecker.processPointerUp(event);
    }

    private int transformOrientation(float orientation) {
        if (Math.abs(orientation - 1.5707964f) < FLOAT_PRECISION) {
            return 1;
        }
        if (Math.abs(1.5707964f + orientation) < FLOAT_PRECISION) {
            return 3;
        }
        return 0;
    }

    private boolean edgePoint(MonitorPoint point) {
        boolean edge;
        int width = this.mDisplayInfo.logicalWidth;
        int height = this.mDisplayInfo.logicalHeight;
        float x = point.x;
        boolean z = false;
        if (point.orientation == 0) {
            if ((x >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && x <= 100.0f) || (x >= ((float) (width - 100)) && x <= ((float) width))) {
                z = true;
            }
            edge = z;
        } else {
            if ((x >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && x <= 100.0f) || (x >= ((float) (height - 100)) && x <= ((float) height))) {
                z = true;
            }
            edge = z;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(point);
        stringBuilder.append(edge ? " is" : " not");
        stringBuilder.append(" a edge point");
        Log.i(str, stringBuilder.toString());
        return edge;
    }

    private String getWindowTitle(WindowState window) {
        if (window == null || window.getAttrs() == null) {
            return "unkown";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("");
        stringBuilder.append(this.mFocusedWindow.getAttrs().getTitle());
        return stringBuilder.toString();
    }

    /* JADX WARNING: Missing block: B:17:0x002e, code skipped:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkStatistic() {
        synchronized (this.mStatisticLock) {
            if (this.mStatisticStartTime != 0) {
                if (this.mStatisticCount > 200) {
                    long now = System.currentTimeMillis();
                    if (now - this.mStatisticStartTime > 3600000) {
                        this.mStatisticStartTime = now;
                        this.mStatisticCount = 0;
                        return true;
                    }
                    return false;
                }
            }
        }
    }

    private void updateStatistic() {
        synchronized (this.mStatisticLock) {
            if (this.mStatisticCount == 0) {
                this.mStatisticStartTime = System.currentTimeMillis();
            }
            this.mStatisticCount++;
        }
    }
}
