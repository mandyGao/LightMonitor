package com.monitor.main;

import android.os.Parcelable;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyLog {

    private static boolean isInDebugMode = false;
    private static final String LOG_FORMAT_REGULAR_EXPRESSION = "%[a-z](%\\d{1,2})?";
    private static final Pattern LOG_FORMAT_PATTERN = Pattern.compile(LOG_FORMAT_REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
    public static final StackTraceRange STACK_TRACE_RANGE_ANDROID = new StackTraceRange() {
        @Override
        public int getStart() {
            return 4;
        }

        @Override
        public int size() {
            return 1;
        }
    };
    public static final StackTraceRange STACK_TRACE_RANGE_JAVA = new StackTraceRange() {
        @Override
        public int getStart() {
            return 3;
        }

        @Override
        public int size() {
            return 1;
        }
    };

    private static final OutputLogMessage OUTPUT_LOG_ANDROID = new OutputLogMessage() {
        void p() {
        }

        ;

        @Override
        public void output(LogLevel logLevel, String TAG, String msg) {
            switch (logLevel) {
                case debug:
                    Log.d(TAG, msg);
                    break;
                case info:
                    Log.i(TAG, msg);
                    break;
                case warning:
                    Log.w(TAG, msg);
                case error:
                    Log.e(TAG, msg);
                    break;
            }
        }
    };
    private static final OutputLogMessage OUTPUT_LOG_JAVA = new OutputLogMessage() {
        void p() {
        }



        @Override
        public void output(LogLevel logLevel, String TAG, String msg) {
            switch (logLevel) {
                case debug:
                case info:
                case warning:
                    System.out.println(msg);
                    break;
                case error:
                    System.err.println(msg);
                    break;
            }
        }
    };

    private OutputLogMessage outputLogMessage = OUTPUT_LOG_ANDROID;

    private StackTraceRange stackTraceRange = null;
    public boolean showStackTraceElement = false;
    public static boolean DEBUG = false;


    public static String PATTERN_ALL = "scenic_log %d %t%6 %c %l - %m";

    private String pattern = "scenic_log %t%6 %c %l - %m";

    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.CANADA);

    private String internalTag = null;

    public static MyLog buildForJava(Class classz){
        MyLog myLog = new MyLog(classz.getSimpleName());
        myLog.setStackTraceRange(STACK_TRACE_RANGE_JAVA);
        myLog.setOutputLogMessage(OUTPUT_LOG_JAVA);
        myLog.setPattern(PATTERN_ALL);
        return myLog;
    }

    public static MyLog buildForAndroid(Class classz){
        MyLog myLog = new MyLog(classz.getSimpleName());
        myLog.setStackTraceRange(STACK_TRACE_RANGE_ANDROID);
        myLog.setOutputLogMessage(OUTPUT_LOG_ANDROID);
        return myLog;
    }


    public MyLog(String tag) {
        internalTag = tag;
        setPattern("scenic_log %t%6 %l - %m");
        setStackTraceRange(STACK_TRACE_RANGE_ANDROID);
    }


    public void setStackTraceRange(StackTraceRange range) {
        this.stackTraceRange = range;
    }

    public void setOutputLogMessage(OutputLogMessage outputLogMessage) {
        this.outputLogMessage = outputLogMessage;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void debug(Object object) {
        String fullMsg = forEachStackTrackElementAndBuildLog(object.toString());
        internalLogOut(internalTag, fullMsg, LogLevel.debug);
    }

    public void info(Object object) {
        String fullMsg = forEachStackTrackElementAndBuildLog(object.toString());
        internalLogOut(internalTag, fullMsg, LogLevel.info);
    }

    public void error(Object object) {
        String fullMsg = forEachStackTrackElementAndBuildLog(object.toString());
        internalLogOut(internalTag, fullMsg, LogLevel.error);
    }


    private String forEachStackTrackElementAndBuildLog(String msg) {
        Thread thread = Thread.currentThread();
        StackTraceElement[] elements = thread.getStackTrace();

        if (showStackTraceElement) {
            for (int i = 0; i < elements.length; i++) {
                internalPrint("DEBUG", i + "   " + elements[i], LogLevel.info);
            }
        }

        int startIndex = stackTraceRange.getStart();
        int size = stackTraceRange.size();



        String threadName = thread.getName();
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = startIndex; i < startIndex + size; i++) {
            if (i < elements.length) {
                StackTraceElement elem = elements[i];
                buildWithPatten(stringBuffer, elem, threadName, msg);
            }
        }

        return stringBuffer.toString();
    }

    private void buildWithPatten(StringBuilder stringBuffer, StackTraceElement element, String threadName, String msg) {

        int lineNumber = element.getLineNumber();//
        String methodName = element.getMethodName();//
        String className = element.getClassName();
        className = className.substring(className.lastIndexOf(".") + 1);//


        Matcher matcher = LOG_FORMAT_PATTERN.matcher(this.pattern);
        matcher.reset();
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String value = matcher.group();

            internalDebug(value + "  " + matcher.start() + "  " + matcher.end());

            if (value.startsWith("%t")) {
                //线程名称
                threadName = checkLength(threadName, value);
                internalDebug("thread name " + threadName);

                matcher.appendReplacement(buffer, threadName);
            } else if (value.startsWith("%c")) {
                //类名
                //替换的字符串中不能有 $
                className = checkLength(className, value).replace("$", "#");
                internalDebug("class name " + className);
                matcher.appendReplacement(buffer, className);
            }
            if (value.startsWith("%l")) {
                String methodAndLine = checkLength(methodName + ":" + lineNumber, value);
                matcher.appendReplacement(buffer, methodAndLine);
            }

            if (value.startsWith("%d")) {
                String time = dateFormat.format(new Date(System.currentTimeMillis()));
                time = checkLength(time, value);
                matcher.appendReplacement(buffer, time);
            }

            if (value.startsWith("%m")) {
                msg = checkLength(msg, value);
                matcher.appendReplacement(buffer, msg);
            }
        }

        stringBuffer.append(buffer).append("\n");

    }

    private String checkLength(String origin, String value) {
        Pattern pattern = Pattern.compile("%[a-z]%(\\d{1,2})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            int lengthFlag = Integer.parseInt(matcher.group(1));
            if (origin.length() < lengthFlag) {
                StringBuffer tmp = new StringBuffer(origin);
                for (int i = lengthFlag - origin.length(); i > 0; i--) {
                    tmp.append(" ");
                }
                return tmp.toString();
            }
        }

        return origin.replace("$", "#");
    }


    private void internalDebug(String log) {
        if (DEBUG)
            System.out.println("scenic_log " + log);
    }

    public static void setDebugMode(boolean debug) {
        isInDebugMode = debug;
    }

    public static String getInstanceName(Object instance) {
        if (instance instanceof Parcelable) {
            return instance.toString();
        }
        return instance.getClass().getSimpleName() + ":" + Long.toHexString(instance.hashCode()) + " ";
    }

    private void internalLogOut(String TAG, String msg, LogLevel logLevel) {
        if (outputLogMessage != null) {
            outputLogMessage.output(logLevel, TAG, msg);
        }
    }

    public interface OutputLogMessage {

        void output(LogLevel logLevell, String TAGg, String msge);

    }

    public interface StackTraceRange {
        int getStart();

        int size();
    }

    protected enum LogLevel {
        debug,
        info,
        warning,
        error,
    }


    /////////////////////////////////////////////////////////////////////////////////////////


    @Deprecated
    public static boolean useJunitTest = false;

    public static void d(String TAG, String msg) {
        if (isInDebugMode) {
            internalPrint(TAG, msg, LogLevel.debug);
        }
    }

    public static void d(String TAG, String msg, String classNameSuffix) {
        String fullMsg;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        fullMsg = getFullLogMsg(msg, elements, startIndex + 1, size, classNameSuffix);
        internalPrint(TAG, fullMsg, LogLevel.debug);
    }


    private static String getFullLogMsg(String msg, StackTraceElement[] elements, int startIndex, int size) {
        return getFullLogMsg(msg, elements, startIndex, size, "");
    }

    private static String getFullLogMsg(String msg, StackTraceElement[] elements, int startIndex, int size, String classNameSuffix) {

        StringBuffer stringBuffer = new StringBuffer();
        try {
            final String split = ".";
            for (int i = startIndex; i < startIndex + size; i++) {
                StackTraceElement elem = elements[i];
                int lineNumber = elem.getLineNumber();
                String methodName = elem.getMethodName();
                String className = elem.getClassName();
                className = className.substring(className.lastIndexOf(".") + 1);
                stringBuffer.append(className + classNameSuffix).append(split).append(methodName + ":").append(lineNumber + "  ").append(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return msg;
        }
        return stringBuffer.toString();
    }

    @Deprecated
    public static void i(String msg) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        String fullMsg = getFullLogMsg(msg, elements, startIndex, size);
        internalPrint("I", fullMsg, LogLevel.info);
    }

    public static void i(String TAG, String msg) {
        String fullMsg;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        fullMsg = getFullLogMsg(msg, elements, startIndex, size);
        internalPrint(TAG, fullMsg, LogLevel.info);
    }

    public static void i(String TAG, String msg, int traceSize) {
        //0 dalvik.system.VMStack.getThreadStackTrace(Native Method)
        //1 java.lang.Thread.getStackTrace(Thread.java:591)
        //2 MyLog.i(MyLog.java:24)
        //3 com.lenovo.nova.childrencontrol.Config.setCurrentUser(Config.java:82)
        //4 com.lenovo.nova.childrencontrol.MainActivity.goToDesktop(MainActivity.java:69)
        internalPrint(TAG, "=========start=================", LogLevel.info);
        try {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            for (int i = startIndex; i < startIndex + traceSize; i++) {
                if (i < elements.length) {
                    internalPrint(TAG, "" + elements[i], LogLevel.info);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
        }

        i(TAG, msg);
        internalPrint(TAG, "=========end=================", LogLevel.info);
    }

    public static void i(String TAG, String msg, String classNameSuffix) {
        String fullMsg;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        fullMsg = getFullLogMsg(msg, elements, startIndex + 1, size, classNameSuffix);
        internalPrint(TAG, fullMsg, LogLevel.info);
    }

    public static void w(String TAG, String msg) {
        String fullMsg;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        fullMsg = getFullLogMsg(msg, elements, startIndex, size);
        internalPrint(TAG, fullMsg, LogLevel.warning);
    }

    public static void e(String TAG, String msg) {
        String fullMsg;
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        fullMsg = getFullLogMsg(msg, elements, startIndex, size);
        internalPrint(TAG, fullMsg, LogLevel.error);
    }


    @Deprecated
    private static void internalPrint(String TAG, String msg, LogLevel logLevel) {

        switch (logLevel) {
            case debug:
                Log.d(TAG, msg);
                break;
            case info:
                Log.i(TAG, msg);
                break;
            case warning:
                Log.w(TAG, msg);
            case error:
                Log.e(TAG, msg);
                break;
        }

    }

    @Deprecated
    public static void p(String tag, String s) {
        i(tag, s);
    }

    @Deprecated
    public static void p(String s) {
        i("no-tag", s);
    }

    @Deprecated
    public static void e(Object s) {
        e("no-tag", s == null ? "null" : s.toString());
    }

    @Deprecated
    public static void e(Object s, int i) {
        e("no-tag", s == null ? "null" : s.toString());
    }

    @Deprecated
    public static void d(String s) {
        d("no-tag", s);
    }

    @Deprecated
    public static void error(Object object, String s) {
        e(object.toString(), s);
    }


    @Deprecated
    private static final int startIndex = 3;
    @Deprecated
    private static final int size = 1;
//    -X号: X信息输出时左对齐；
//            %p: 输出日志信息优先级，即DEBUG，INFO，WARN，ERROR，FATAL,
//            %d: 输出日志时间点的日期或时间，默认格式为ISO8601，也可以在其后指定格式，比如：%d{yyy MMM dd HH:mm:ss,SSS}，输出类似：2002年10月18日 22：10：28，921
//            %r: 输出自应用启动到输出该log信息耗费的毫秒数
//    %c: 输出日志信息所属的类目，通常就是所在类的全名
//    %t: 输出产生该日志事件的线程名
//    %l: 输出日志事件的发生位置，相当于%C.%M(%F:%L)的组合,包括类目名、发生的线程，以及在代码中的行数。举例：Testlog4.main (TestLog4.java:10)
//            %x: 输出和当前线程相关联的NDC(嵌套诊断环境),尤其用到像java servlets这样的多客户多线程的应用中。
//            %%: 输出一个"%"字符
//    %F: 输出日志消息产生时所在的文件名称
//    %L: 输出代码中的行号
//    %m: 输出代码中指定的消息,产生的日志具体信息
//    %n: 输出一个回车换行符，Windows平台为"/r/n"，Unix平台为"/n"输出日志信息换行
//
//    可以在%与模式字符之间加上修饰符来控制其最小宽度、最大宽度、和文本的对齐方式。
//    如：
//            1)   %20c：指定输出category的名称，最小的宽度是20，如果category的名称小于20的话，默认的情况下右对齐。
//            2)   %-20c:指定输出category的名称，最小的宽度是20，如果category的名称小于20的话，"-"号指定左对齐。
//            3)   %.30c:指定输出category的名称，最大的宽度是30，如果category的名称大于30的话，就会将左边多出的字符截掉，但小于30的话也不会有空格。
//            4)   %20.30c:如果category的名称小于20就补空格，并且右对齐，如果其名称长于30字符，就从左边较远输出的字符截掉。
//
}